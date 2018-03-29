/*
Navicat MySQL Data Transfer

Source Server         : 192.168.7.200
Source Server Version : 50173
Source Host           : 192.168.7.200:3306
Source Database       : game_new

Target Server Type    : MYSQL
Target Server Version : 50173
File Encoding         : 65001

Date: 2018-03-19 11:31:58
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for active_num
-- ----------------------------
DROP TABLE IF EXISTS `active_num`;
CREATE TABLE `active_num` (
  `activeNum` char(10) NOT NULL,
  `playerId` int(10) unsigned NOT NULL DEFAULT '0',
  `type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`activeNum`),
  KEY `Index_playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='激活码';

-- ----------------------------
-- Table structure for attach
-- ----------------------------
DROP TABLE IF EXISTS `attach`;
CREATE TABLE `attach` (
  `playerId` int(20) NOT NULL,
  `type` smallint(4) NOT NULL,
  `extraInfo` text NOT NULL,
  PRIMARY KEY (`playerId`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for gang
-- ----------------------------
DROP TABLE IF EXISTS `gang`;
CREATE TABLE `gang` (
  `id` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='帮派表';

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `playerId` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='物品表';

-- ----------------------------
-- Table structure for mail
-- ----------------------------
DROP TABLE IF EXISTS `mail`;
CREATE TABLE `mail` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `senderId` int(10) unsigned NOT NULL,
  `senderName` varchar(12) NOT NULL,
  `receiveId` int(10) unsigned NOT NULL,
  `title` varchar(35) NOT NULL,
  `content` varchar(445) NOT NULL,
  `sendTime` datetime DEFAULT NULL,
  `state` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `rewards` varchar(400) DEFAULT NULL,
  `hasReward` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `type` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `Index_playerId` (`receiveId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=156066 DEFAULT CHARSET=utf8 COMMENT='邮件表';

-- ----------------------------
-- Table structure for manager
-- ----------------------------
DROP TABLE IF EXISTS `manager`;
CREATE TABLE `manager` (
  `playerId` int(10) unsigned NOT NULL,
  `banChat` int(10) unsigned NOT NULL DEFAULT '0',
  `banLogin` int(10) unsigned NOT NULL DEFAULT '0',
  `banChatEnd` datetime DEFAULT NULL,
  `banLoginEnd` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色管理，如禁言等';

-- ----------------------------
-- Table structure for pet
-- ----------------------------
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
  `playerId` int(11) NOT NULL,
  `data` blob,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for player
-- ----------------------------
DROP TABLE IF EXISTS `player`;
CREATE TABLE `player` (
  `playerId` int(10) unsigned NOT NULL,
  `accName` varchar(63) NOT NULL,
  `name` varchar(20) NOT NULL,
  `sex` tinyint(3) unsigned NOT NULL,
  `vocation` tinyint(3) unsigned NOT NULL,
  `exp` int(10) unsigned NOT NULL DEFAULT '0',
  `coin` int(10) unsigned NOT NULL DEFAULT '0',
  `diamond` int(10) unsigned NOT NULL DEFAULT '0',
  `chargeDiamond` int(10) unsigned NOT NULL DEFAULT '0',
  `vip` int(10) unsigned NOT NULL DEFAULT '0',
  `energy` int(10) unsigned NOT NULL DEFAULT '0',
  `serverId` smallint(5) unsigned NOT NULL DEFAULT '0',
  `regTime` datetime NOT NULL,
  `lastLoginTime` datetime DEFAULT NULL,
  `lastLogoutTime` datetime DEFAULT NULL,
  `ip` varchar(25) DEFAULT NULL,
  `sceneId` int(10) unsigned NOT NULL DEFAULT '0',
  `x` float NOT NULL DEFAULT '0',
  `y` float NOT NULL DEFAULT '0',
  `lastSceneId` int(10) unsigned NOT NULL DEFAULT '0',
  `lev` smallint(5) unsigned NOT NULL DEFAULT '1',
  `crit` int(10) unsigned NOT NULL DEFAULT '0',
  `hp` int(10) unsigned NOT NULL DEFAULT '0',
  `symptom` int(10) unsigned NOT NULL DEFAULT '0',
  `fu` int(10) unsigned NOT NULL DEFAULT '0',
  `fight` int(10) unsigned NOT NULL DEFAULT '0',
  `z` float NOT NULL DEFAULT '0',
  `fashionId` int(10) unsigned NOT NULL DEFAULT '0',
  `gangId` int(10) unsigned NOT NULL DEFAULT '0',
  `totalCoin` int(10) unsigned NOT NULL DEFAULT '0',
  `weaponId` int(10) unsigned NOT NULL DEFAULT '0',
  `energyTime` bigint(20) unsigned NOT NULL DEFAULT '0',
  `totalDiamond` int(10) unsigned NOT NULL DEFAULT '0',
  `channel` varchar(20) DEFAULT NULL,
  `attack` int(10) unsigned NOT NULL DEFAULT '0',
  `defense` int(10) unsigned NOT NULL DEFAULT '0',
  `title` int(10) unsigned NOT NULL DEFAULT '0',
  `achievement` int(11) DEFAULT '0' COMMENT '成就点',
  PRIMARY KEY (`playerId`),
  KEY `Index_accName` (`accName`) USING BTREE,
  KEY `Index_name` (`name`) USING BTREE,
  KEY `Index_fight` (`fight`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='玩家表';

-- ----------------------------
-- Table structure for player_data
-- ----------------------------
DROP TABLE IF EXISTS `player_data`;
CREATE TABLE `player_data` (
  `playerId` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='玩家相关数据';

-- ----------------------------
-- Table structure for rank
-- ----------------------------
DROP TABLE IF EXISTS `rank`;
CREATE TABLE `rank` (
  `type` int(10) NOT NULL,
  `data` longblob NOT NULL,
  PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for serial_data
-- ----------------------------
DROP TABLE IF EXISTS `serial_data`;
CREATE TABLE `serial_data` (
  `id` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='服务器全局数据表';

-- ----------------------------
-- Table structure for serverparam
-- ----------------------------
DROP TABLE IF EXISTS `serverparam`;
CREATE TABLE `serverparam` (
  `paramvalue` text NOT NULL,
  `text` varchar(255) DEFAULT NULL,
  `paramkey` varchar(255) NOT NULL,
  `serverid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `playerId` int(10) unsigned NOT NULL COMMENT 'çŽ©å®¶id',
  `data` longblob COMMENT 'æ•°æ®',
  PRIMARY KEY (`playerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务表';

-- ----------------------------
-- Table structure for wb_data
-- ----------------------------
DROP TABLE IF EXISTS `wb_data`;
CREATE TABLE `wb_data` (
  `id` int(11) NOT NULL,
  `data` longblob COMMENT '世界BOSS记录',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
