/*
Navicat MySQL Data Transfer

Source Server         : 192.168.7.200
Source Server Version : 50173
Source Host           : 192.168.7.200:3306
Source Database       : game_new_log

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2018-01-25 21:24:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for players_diamond_logs
-- ----------------------------
DROP TABLE IF EXISTS `players_diamond_logs`;
CREATE TABLE `players_diamond_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `op_type` int(11) NOT NULL,
  `param` varchar(100) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `count` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43046 DEFAULT CHARSET=utf8 COMMENT='钻石日志';

-- ----------------------------
-- Table structure for players_diamond_logs_copy
-- ----------------------------
DROP TABLE IF EXISTS `players_diamond_logs_copy`;
CREATE TABLE `players_diamond_logs_copy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `op_type` int(11) NOT NULL,
  `param` varchar(100) NOT NULL,
  `create_time` datetime NOT NULL,
  `count` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9834 DEFAULT CHARSET=utf8 COMMENT='钻石日志';
