/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 50543
 Source Host           : localhost:3306
 Source Schema         : game_log

 Target Server Type    : MySQL
 Target Server Version : 50543
 File Encoding         : 65001

 Date: 20/11/2018 17:06:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for charge_log
-- ----------------------------
DROP TABLE IF EXISTS `charge_log`;
CREATE TABLE `charge_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NULL DEFAULT NULL COMMENT '角色id',
  `role_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `charge_id` int(11) NULL DEFAULT NULL COMMENT '充值id',
  `charge_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '充值类型',
  `amount` float(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `channel_id` int(11) NULL DEFAULT NULL COMMENT '区服id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '充值时间',
  `payment_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付方式',
  `server_id` bigint(20) NULL DEFAULT NULL COMMENT '服务器id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for item_log
-- ----------------------------
DROP TABLE IF EXISTS `item_log`;
CREATE TABLE `item_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NULL DEFAULT NULL COMMENT '玩家ID',
  `op` int(11) NULL DEFAULT NULL,
  `count` int(11) NULL DEFAULT NULL COMMENT '数量',
  `type` int(11) NULL DEFAULT NULL COMMENT '类型',
  `goods_id` int(11) NULL DEFAULT NULL COMMENT '道具ID',
  `goods_type` int(11) NULL DEFAULT NULL COMMENT '货币类型',
  `create_time` datetime NULL DEFAULT NULL,
  `server_id` bigint(20) NULL DEFAULT NULL COMMENT '服务器id',
  `lev` int(11) NULL DEFAULT NULL COMMENT '等级',
  `prev` int(11) NULL DEFAULT NULL COMMENT '变化前',
  `next` int(11) NULL DEFAULT NULL COMMENT '变化后',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for mail_log_new
-- ----------------------------
DROP TABLE IF EXISTS `mail_log_new`;
CREATE TABLE `mail_log_new`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sender_id` int(10) UNSIGNED NOT NULL,
  `sender_name` varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `receive_id` int(10) UNSIGNED NOT NULL,
  `title` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `content` varchar(445) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `send_time` datetime NULL DEFAULT NULL,
  `state` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0未读、1已读、2已删除',
  `rewards` varchar(4000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `has_reward` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '1未领奖、2已领奖',
  `type` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `server_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '区服ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for players_diamond_logs
-- ----------------------------
DROP TABLE IF EXISTS `players_diamond_logs`;
CREATE TABLE `players_diamond_logs`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NULL DEFAULT NULL,
  `item_id` int(11) NULL DEFAULT NULL,
  `op_type` int(11) NULL DEFAULT NULL COMMENT '1增加、2减少',
  `param` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `count` int(10) UNSIGNED NULL DEFAULT NULL,
  `server_id` bigint(20) NULL DEFAULT NULL COMMENT '服务器id',
  `lev` int(11) NULL DEFAULT NULL COMMENT '等级',
  `prev` int(11) NULL DEFAULT NULL COMMENT '变化前',
  `next` int(11) NULL DEFAULT NULL COMMENT '变化后',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
