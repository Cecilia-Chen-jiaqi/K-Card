-- MySQL 5.7 compatible DDL for K-CARD KPOP小卡交易平台
-- 数据库名：kpop_trade

DROP DATABASE IF EXISTS `kpop_trade`;
CREATE DATABASE IF NOT EXISTS `kpop_trade` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `kpop_trade`;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码（已加密）',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `phone` VARCHAR(32) NOT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `campus` VARCHAR(128) DEFAULT NULL COMMENT '校园',
  `intro` VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
  `role` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '角色 0=普通用户 1=管理员',
  `account_status` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '1=正常 0=禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 地址表
DROP TABLE IF EXISTS `address`;
CREATE TABLE IF NOT EXISTS `address` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '所属用户',
  `receiver` VARCHAR(64) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(32) NOT NULL COMMENT '联系电话',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '市',
  `district` VARCHAR(64) DEFAULT NULL COMMENT '区',
  `detail` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `is_default` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '默认地址',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_address_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地址表';

-- 商品表
DROP TABLE IF EXISTS `goods`;
CREATE TABLE IF NOT EXISTS `goods` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `seller_id` BIGINT(20) NOT NULL COMMENT '卖家用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '商品标题',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品价格',
  `stock` INT(11) NOT NULL DEFAULT '1' COMMENT '库存数量',
  `group_name` VARCHAR(64) DEFAULT NULL COMMENT '团体',
  `idol_name` VARCHAR(64) DEFAULT NULL COMMENT '爱豆',
  `quality` VARCHAR(16) NOT NULL DEFAULT '无暇' COMMENT '品相',
  `trade_type` VARCHAR(32) NOT NULL DEFAULT '仅出售' COMMENT '交易模式',
  `reserve_support` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否支持预留',
  `delivery_mode` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '1=普通邮寄 2=校园同城面交',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图路径',
  `defect_images` VARCHAR(512) DEFAULT NULL COMMENT '瑕疵图地址列表',
  `card_type` VARCHAR(64) DEFAULT NULL COMMENT '小卡类型',
  `album_era` VARCHAR(128) DEFAULT NULL COMMENT '回归时期/专辑',
  `status` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '0=下架 1=上架 2=待审核 3=拒绝',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核拒绝原因',
  `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间',
  `reviewed_by` BIGINT(20) DEFAULT NULL COMMENT '审核管理员ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_goods_seller` (`seller_id`),
  KEY `idx_goods_group` (`group_name`),
  KEY `idx_goods_idol` (`idol_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- KPOP 商品详情表
DROP TABLE IF EXISTS `kpop_goods_detail`;
CREATE TABLE IF NOT EXISTS `kpop_goods_detail` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '详情ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `card_bundle` VARCHAR(128) DEFAULT NULL COMMENT '捆卡说明',
  `exchange_info` VARCHAR(255) DEFAULT NULL COMMENT '换卡说明',
  `reserve_deadline` DATETIME DEFAULT NULL COMMENT '预留到期',
  `extra_info` VARCHAR(512) DEFAULT NULL COMMENT '补充信息',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kpop_goods_detail_goods` (`goods_id`),
  KEY `idx_kpop_goods_detail_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='KPOP商品详情';

-- 购物车表
DROP TABLE IF EXISTS `cart`;
CREATE TABLE IF NOT EXISTS `cart` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '购物车记录ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `quantity` INT(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `selected` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '是否选中',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_user_goods` (`user_id`,`goods_id`),
  KEY `idx_cart_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车表';

-- 订单表
DROP TABLE IF EXISTS `orders`;
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `buyer_id` BIGINT(20) NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT(20) DEFAULT NULL COMMENT '卖家ID(单卖家兼容,多卖家为空)',
  `goods_id` BIGINT(20) DEFAULT NULL COMMENT '商品ID(单商品兼容,多商品为空)',
  `address_id` BIGINT(20) DEFAULT NULL COMMENT '收货地址ID',
  `quantity` INT(11) DEFAULT NULL COMMENT '总数量',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `pay_type` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '支付方式 1=支付宝 2=线下面交',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '订单状态 0待支付 1已支付待发货 2已发货 3交易完成 4交易关闭 5退款完成',
  `is_reserved` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否预留订单',
  `item_count` INT(11) NOT NULL DEFAULT '1' COMMENT '明细行数',
  `express_company` VARCHAR(64) DEFAULT NULL COMMENT '快递公司',
  `tracking_no` VARCHAR(64) DEFAULT NULL COMMENT '快递单号',
  `logistics_note` VARCHAR(255) DEFAULT NULL COMMENT '物流备注',
  `shipped_at` DATETIME DEFAULT NULL COMMENT '发货时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `closed_at` DATETIME DEFAULT NULL COMMENT '关闭时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  KEY `idx_orders_buyer` (`buyer_id`),
  KEY `idx_orders_seller` (`seller_id`),
  KEY `idx_orders_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- 订单明细表（合并订单 / 购物车批量结算）
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `seller_id` BIGINT(20) NOT NULL COMMENT '卖家ID',
  `quantity` INT(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `unit_price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '单价',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '小计',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '0待支付 1已支付 2已发货 3已完成 5已退款',
  `express_company` VARCHAR(64) DEFAULT NULL COMMENT '快递公司',
  `tracking_no` VARCHAR(64) DEFAULT NULL COMMENT '快递单号',
  `logistics_note` VARCHAR(255) DEFAULT NULL COMMENT '物流备注',
  `shipped_at` DATETIME DEFAULT NULL COMMENT '发货时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order_no` (`order_no`),
  KEY `idx_order_item_order_id` (`order_id`),
  KEY `idx_order_item_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细';

-- 支付流水表
DROP TABLE IF EXISTS `payment_log`;
CREATE TABLE IF NOT EXISTS `payment_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '支付日志ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易号',
  `buyer_id` BIGINT(20) NOT NULL COMMENT '买家ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额',
  `status` VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT '支付状态',
  `notify_content` TEXT COMMENT '支付宝回调原文',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_payment_log_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付流水表';

-- 退款流水表
DROP TABLE IF EXISTS `refund_log`;
CREATE TABLE IF NOT EXISTS `refund_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '退款日志ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
  `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '支付宝交易号',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '退款金额',
  `status` VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT '退款状态',
  `notify_content` TEXT COMMENT '支付宝退款回调原文',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_log_refund_no` (`refund_no`),
  KEY `idx_refund_log_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款流水表';

-- 商品收藏表
DROP TABLE IF EXISTS `goods_favorite`;
CREATE TABLE IF NOT EXISTS `goods_favorite` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_goods_favorite_user_goods` (`user_id`,`goods_id`),
  KEY `idx_goods_favorite_user` (`user_id`),
  KEY `idx_goods_favorite_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品收藏';

-- 卖家关注表
DROP TABLE IF EXISTS `seller_follow`;
CREATE TABLE IF NOT EXISTS `seller_follow` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `seller_id` BIGINT(20) NOT NULL COMMENT '卖家用户ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seller_follow_user_seller` (`user_id`,`seller_id`),
  KEY `idx_seller_follow_user` (`user_id`),
  KEY `idx_seller_follow_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='卖家关注';
