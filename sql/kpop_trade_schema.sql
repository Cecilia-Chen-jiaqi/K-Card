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
  `role` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '角色 0=买家/卖家',
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
  `status` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '商品状态 1=上架 0=下架',
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
  `seller_id` BIGINT(20) NOT NULL COMMENT '卖家ID',
  `goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `quantity` INT(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `pay_type` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '支付方式 1=支付宝 2=线下面交',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '订单状态 0待支付 1已支付待发货 2已发货 3交易完成 4交易关闭 5退款完成',
  `is_reserved` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否预留订单',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `closed_at` DATETIME DEFAULT NULL COMMENT '关闭时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  KEY `idx_orders_buyer` (`buyer_id`),
  KEY `idx_orders_seller` (`seller_id`),
  KEY `idx_orders_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

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
