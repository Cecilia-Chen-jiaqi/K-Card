package com.kpoptrade.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.kpoptrade.constant.UserRole;
import com.kpoptrade.service.GoodsService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UploadProperties uploadProperties;

    private static final long MIN_COVER_BYTES = 2048L;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!isUserTableExists()) {
                logger.warn("User table does not exist. Please initialize the database schema before starting the application.");
                return;
            }
            ensureAvatarColumnExists();
            ensureGoodsKpopColumnsExist();
            ensureOrdersAddressColumnExists();
            ensureOrdersLogisticsColumnsExist();
            ensureOrderItemSupport();
            ensureFavoriteFollowTables();
            ensureGoodsAuditColumns();
            ensureUserAccountStatus();
            ensureAdminUser();
            fixSoldOutGoodsStillListed();
            delistGoodsWithoutCoverImage();
            repairPrematureDelistFromUnpaidOrders();
            repairPaidOrdersWithoutDelist();
        } catch (Exception ex) {
            logger.error("Failed to initialize database schema", ex);
        }
    }

    private boolean isUserTableExists() {
        String existsSql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = 'user'";
        Integer count = jdbcTemplate.queryForObject(existsSql, Integer.class);
        return count != null && count > 0;
    }

    private void ensureAvatarColumnExists() {
        String columnSql = "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = 'user' AND column_name = 'avatar'";
        Integer count = jdbcTemplate.queryForObject(columnSql, Integer.class);
        if (count == null || count == 0) {
            logger.info("Detected missing `avatar` column in `user` table, adding it now.");
            jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址'");
            logger.info("Successfully added `avatar` column to `user` table.");
        }
    }

    private void ensureGoodsKpopColumnsExist() {
        ensureColumn("goods", "card_type",
                "ALTER TABLE `goods` ADD COLUMN `card_type` VARCHAR(64) DEFAULT NULL COMMENT '小卡类型'");
        ensureColumn("goods", "album_era",
                "ALTER TABLE `goods` ADD COLUMN `album_era` VARCHAR(128) DEFAULT NULL COMMENT '回归时期/专辑'");
    }

    private void ensureOrdersAddressColumnExists() {
        ensureColumn("orders", "address_id",
                "ALTER TABLE `orders` ADD COLUMN `address_id` BIGINT(20) DEFAULT NULL COMMENT '收货地址ID' AFTER `goods_id`");
    }

    private void ensureOrdersLogisticsColumnsExist() {
        ensureColumn("orders", "express_company",
                "ALTER TABLE `orders` ADD COLUMN `express_company` VARCHAR(64) DEFAULT NULL COMMENT '快递公司' AFTER `is_reserved`");
        ensureColumn("orders", "tracking_no",
                "ALTER TABLE `orders` ADD COLUMN `tracking_no` VARCHAR(64) DEFAULT NULL COMMENT '快递单号' AFTER `express_company`");
        ensureColumn("orders", "logistics_note",
                "ALTER TABLE `orders` ADD COLUMN `logistics_note` VARCHAR(255) DEFAULT NULL COMMENT '物流备注' AFTER `tracking_no`");
        ensureColumn("orders", "shipped_at",
                "ALTER TABLE `orders` ADD COLUMN `shipped_at` DATETIME DEFAULT NULL COMMENT '发货时间' AFTER `logistics_note`");
    }

    private void ensureOrderItemSupport() {
        ensureColumn("orders", "item_count",
                "ALTER TABLE `orders` ADD COLUMN `item_count` INT(11) NOT NULL DEFAULT 1 COMMENT '明细行数' AFTER `is_reserved`");
        try {
            jdbcTemplate.execute("ALTER TABLE `orders` MODIFY COLUMN `goods_id` BIGINT(20) DEFAULT NULL COMMENT '商品ID(单商品兼容)'");
            jdbcTemplate.execute("ALTER TABLE `orders` MODIFY COLUMN `seller_id` BIGINT(20) DEFAULT NULL COMMENT '卖家ID(单卖家兼容)'");
            jdbcTemplate.execute("ALTER TABLE `orders` MODIFY COLUMN `quantity` INT(11) DEFAULT NULL COMMENT '总数量'");
        } catch (Exception ex) {
            logger.debug("orders nullable columns may already be applied: {}", ex.getMessage());
        }
        String tableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'order_item'";
        Integer count = jdbcTemplate.queryForObject(tableSql, Integer.class);
        if (count == null || count == 0) {
            logger.info("Creating order_item table for merged checkout");
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS `order_item` (" +
                    "  `id` BIGINT(20) NOT NULL AUTO_INCREMENT," +
                    "  `order_id` BIGINT(20) NOT NULL," +
                    "  `order_no` VARCHAR(64) NOT NULL," +
                    "  `goods_id` BIGINT(20) NOT NULL," +
                    "  `seller_id` BIGINT(20) NOT NULL," +
                    "  `quantity` INT(11) NOT NULL DEFAULT 1," +
                    "  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                    "  `status` TINYINT(4) NOT NULL DEFAULT 0," +
                    "  `express_company` VARCHAR(64) DEFAULT NULL," +
                    "  `tracking_no` VARCHAR(64) DEFAULT NULL," +
                    "  `logistics_note` VARCHAR(255) DEFAULT NULL," +
                    "  `shipped_at` DATETIME DEFAULT NULL," +
                    "  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)," +
                    "  KEY `idx_order_item_order_no` (`order_no`)," +
                    "  KEY `idx_order_item_order_id` (`order_id`)," +
                    "  KEY `idx_order_item_seller` (`seller_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细'");
        }
        migrateLegacyOrdersToItems();
    }

    private void ensureFavoriteFollowTables() {
        String favSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'goods_favorite'";
        Integer favCount = jdbcTemplate.queryForObject(favSql, Integer.class);
        if (favCount == null || favCount == 0) {
            logger.info("Creating goods_favorite table");
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS `goods_favorite` (" +
                    "  `id` BIGINT(20) NOT NULL AUTO_INCREMENT," +
                    "  `user_id` BIGINT(20) NOT NULL," +
                    "  `goods_id` BIGINT(20) NOT NULL," +
                    "  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)," +
                    "  UNIQUE KEY `uk_goods_favorite_user_goods` (`user_id`,`goods_id`)," +
                    "  KEY `idx_goods_favorite_user` (`user_id`)," +
                    "  KEY `idx_goods_favorite_goods` (`goods_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏'");
        }
        String followSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'seller_follow'";
        Integer followCount = jdbcTemplate.queryForObject(followSql, Integer.class);
        if (followCount == null || followCount == 0) {
            logger.info("Creating seller_follow table");
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS `seller_follow` (" +
                    "  `id` BIGINT(20) NOT NULL AUTO_INCREMENT," +
                    "  `user_id` BIGINT(20) NOT NULL," +
                    "  `seller_id` BIGINT(20) NOT NULL," +
                    "  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)," +
                    "  UNIQUE KEY `uk_seller_follow_user_seller` (`user_id`,`seller_id`)," +
                    "  KEY `idx_seller_follow_user` (`user_id`)," +
                    "  KEY `idx_seller_follow_seller` (`seller_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卖家关注'");
        }
    }

    private void ensureGoodsAuditColumns() {
        ensureColumn("goods", "reject_reason",
                "ALTER TABLE `goods` ADD COLUMN `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核拒绝原因'");
        ensureColumn("goods", "reviewed_at",
                "ALTER TABLE `goods` ADD COLUMN `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间'");
        ensureColumn("goods", "reviewed_by",
                "ALTER TABLE `goods` ADD COLUMN `reviewed_by` BIGINT(20) DEFAULT NULL COMMENT '审核管理员ID'");
        try {
            jdbcTemplate.execute("CREATE INDEX idx_goods_status ON goods(status)");
        } catch (Exception ex) {
            logger.debug("idx_goods_status may already exist: {}", ex.getMessage());
        }
    }

    private void ensureUserAccountStatus() {
        ensureColumn("user", "account_status",
                "ALTER TABLE `user` ADD COLUMN `account_status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '1=正常 0=禁用' AFTER `role`");
        jdbcTemplate.update("UPDATE user SET account_status = 1 WHERE account_status IS NULL");
    }

    private void ensureAdminUser() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE role = ?", Integer.class, UserRole.ADMIN);
        if (count != null && count > 0) {
            return;
        }
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE username = 'admin'", Integer.class);
        if (exists != null && exists > 0) {
            jdbcTemplate.update("UPDATE user SET role = ?, account_status = 1 WHERE username = 'admin'",
                    UserRole.ADMIN);
            logger.info("Promoted existing user 'admin' to administrator");
            return;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("admin123");
        jdbcTemplate.update(
                "INSERT INTO user (username, password, phone, campus, role, account_status, nickname, created_at, updated_at) " +
                "VALUES ('admin', ?, '19900000000', 'K-CARD管理', ?, 1, '管理员', NOW(), NOW())",
                hash, UserRole.ADMIN);
        logger.info("Seeded default admin user: admin / admin123 (please change password after first login)");
    }

    private void migrateLegacyOrdersToItems() {
        int inserted = jdbcTemplate.update(
                "INSERT INTO order_item (order_id, order_no, goods_id, seller_id, quantity, unit_price, amount, status, " +
                "express_company, tracking_no, logistics_note, shipped_at, created_at, updated_at) " +
                "SELECT o.id, o.order_no, o.goods_id, o.seller_id, o.quantity, " +
                "  CASE WHEN o.quantity IS NULL OR o.quantity = 0 THEN o.amount ELSE o.amount / o.quantity END, " +
                "  o.amount, o.status, o.express_company, o.tracking_no, o.logistics_note, o.shipped_at, o.created_at, o.updated_at " +
                "FROM orders o WHERE o.goods_id IS NOT NULL " +
                "AND NOT EXISTS (SELECT 1 FROM order_item oi WHERE oi.order_id = o.id)");
        if (inserted > 0) {
            logger.info("Migrated {} legacy orders into order_item", inserted);
        }
        jdbcTemplate.update("UPDATE orders SET item_count = 1 WHERE item_count IS NULL OR item_count < 1");
    }

    private void fixSoldOutGoodsStillListed() {
        int updated = jdbcTemplate.update(
                "UPDATE goods SET status = 0 WHERE status = 1 AND (stock IS NULL OR stock <= 0)");
        if (updated > 0) {
            logger.info("Auto-delisted {} sold-out goods (stock <= 0 but status was still on-shelf)", updated);
        }
    }

    /** 无封面图或封面无效（缺失/过小占位图）的商品不应出现在首页 */
    private void delistGoodsWithoutCoverImage() {
        int emptyUpdated = jdbcTemplate.update(
                "UPDATE goods SET status = 0 WHERE status = 1 " +
                "AND (cover_image IS NULL OR TRIM(cover_image) = '')");
        Path uploadDir = Paths.get(uploadProperties.getPath()).toAbsolutePath().normalize();
        String prefix = uploadProperties.getPrefix() != null ? uploadProperties.getPrefix() : "/upload/";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, cover_image FROM goods WHERE status = 1");
        int invalidUpdated = 0;
        for (Map<String, Object> row : rows) {
            Object idObj = row.get("id");
            if (idObj == null) {
                continue;
            }
            String cover = row.get("cover_image") != null ? row.get("cover_image").toString() : null;
            if (isValidCoverImage(cover, uploadDir, prefix)) {
                continue;
            }
            jdbcTemplate.update("UPDATE goods SET status = 0, updated_at = NOW() WHERE id = ?", idObj);
            invalidUpdated++;
        }
        if (emptyUpdated > 0 || invalidUpdated > 0) {
            logger.info("Auto-delisted goods without valid cover: empty={}, invalid={}", emptyUpdated, invalidUpdated);
        }
    }

    private boolean isValidCoverImage(String cover, Path uploadDir, String prefix) {
        if (cover == null || cover.trim().isEmpty()) {
            return false;
        }
        cover = cover.trim();
        if (cover.startsWith("http://") || cover.startsWith("https://")) {
            return true;
        }
        if (cover.startsWith("data:")) {
            return cover.length() >= 200;
        }
        String fileName = cover;
        if (fileName.startsWith(prefix)) {
            fileName = fileName.substring(prefix.length());
        } else if (fileName.startsWith("/upload/")) {
            fileName = fileName.substring("/upload/".length());
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }
        Path file = uploadDir.resolve(fileName);
        if (!Files.isRegularFile(file)) {
            return false;
        }
        try {
            return Files.size(file) >= MIN_COVER_BYTES;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 修复旧版逻辑：未支付订单创建时误扣库存导致商品提前下架。
     * 仅当该商品没有任何已支付/已完成/已退款订单时才恢复。
     */
    private void repairPrematureDelistFromUnpaidOrders() {
        int updated = jdbcTemplate.update(
                "UPDATE goods g SET " +
                "  g.stock = (SELECT COALESCE(SUM(o.quantity), 0) FROM orders o " +
                "             WHERE o.goods_id = g.id AND o.status = 0), " +
                "  g.status = 1 " +
                "WHERE (g.stock IS NULL OR g.stock <= 0) AND g.status = 0 " +
                "AND EXISTS (SELECT 1 FROM orders o WHERE o.goods_id = g.id AND o.status = 0) " +
                "AND NOT EXISTS (SELECT 1 FROM orders o WHERE o.goods_id = g.id AND o.status IN (1, 2, 3, 5)) " +
                "AND NOT EXISTS (SELECT 1 FROM orders o INNER JOIN payment_log pl ON pl.order_no = o.order_no WHERE o.goods_id = g.id)");
        if (updated > 0) {
            logger.info("Restored {} goods wrongly delisted before payment (unpaid orders only)", updated);
        }
    }

    /** 已支付但库存未扣、商品仍上架的历史数据修复 */
    private void repairPaidOrdersWithoutDelist() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT o.goods_id AS goodsId, o.quantity AS quantity FROM orders o " +
                "INNER JOIN goods g ON g.id = o.goods_id " +
                "WHERE o.status IN (1, 2, 3) AND g.status = 1 AND g.stock IS NOT NULL AND g.stock >= o.quantity " +
                "GROUP BY o.goods_id, o.quantity");
        int repaired = 0;
        for (Map<String, Object> row : rows) {
            Object goodsIdObj = row.get("goodsId");
            Object qtyObj = row.get("quantity");
            if (goodsIdObj == null || qtyObj == null) {
                continue;
            }
            Long goodsId = ((Number) goodsIdObj).longValue();
            Integer quantity = ((Number) qtyObj).intValue();
            goodsService.fulfillPaidOrderStock(goodsId, quantity);
            repaired++;
        }
        if (repaired > 0) {
            logger.info("Repaired {} paid-order goods that were still on shelf", repaired);
        }
    }

    private void ensureColumn(String table, String column, String ddl) {
        String columnSql = "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        Integer count = jdbcTemplate.queryForObject(columnSql, Integer.class, table, column);
        if (count == null || count == 0) {
            logger.info("Adding missing column `{}`.`{}`", table, column);
            jdbcTemplate.execute(ddl);
        }
    }
}
