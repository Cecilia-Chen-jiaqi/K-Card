package com.kpoptrade.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;

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
}
