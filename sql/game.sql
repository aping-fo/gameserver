-- MySQL dump 10.13  Distrib 5.6.41, for Linux (x86_64)
--
-- Host: localhost    Database: game
-- ------------------------------------------------------
-- Server version	5.6.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `active_num`
--

DROP TABLE IF EXISTS `active_num`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `active_num` (
  `activeNum` char(10) NOT NULL,
  `playerId` int(10) unsigned NOT NULL DEFAULT '0',
  `type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`activeNum`) USING BTREE,
  KEY `Index_playerId` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='激活码';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attach`
--

DROP TABLE IF EXISTS `attach`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attach` (
  `playerId` int(20) NOT NULL,
  `type` smallint(4) NOT NULL,
  `extraInfo` text NOT NULL,
  PRIMARY KEY (`playerId`,`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `gang`
--

DROP TABLE IF EXISTS `gang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `gang` (
  `id` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='帮派表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `goods`
--

DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `goods` (
  `playerId` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='物品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mail`
--

DROP TABLE IF EXISTS `mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mail` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `senderId` int(10) unsigned NOT NULL,
  `senderName` varchar(12) NOT NULL,
  `receiveId` int(10) unsigned NOT NULL,
  `title` varchar(35) NOT NULL,
  `content` varchar(445) NOT NULL,
  `sendTime` datetime DEFAULT NULL,
  `state` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `rewards` varchar(400) DEFAULT NULL COMMENT '0未读、1已读、2已删除',
  `hasReward` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '1未领奖、2已领奖',
  `type` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `Index_playerId` (`receiveId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=192039 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='邮件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manager`
--

DROP TABLE IF EXISTS `manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `manager` (
  `playerId` int(10) unsigned NOT NULL,
  `banChat` int(10) unsigned NOT NULL DEFAULT '0',
  `banLogin` int(10) unsigned NOT NULL DEFAULT '0',
  `banChatEnd` datetime DEFAULT NULL,
  `banLoginEnd` datetime DEFAULT NULL,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='角色管理，如禁言等';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet`
--

DROP TABLE IF EXISTS `pet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pet` (
  `playerId` int(11) NOT NULL,
  `data` blob,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
  `serverId` int(11) unsigned NOT NULL DEFAULT '0',
  `regTime` datetime DEFAULT NULL,
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
  `clientMac` varchar(255) DEFAULT NULL COMMENT '设备编号',
  PRIMARY KEY (`playerId`) USING BTREE,
  KEY `Index_accName` (`accName`) USING BTREE,
  KEY `Index_name` (`name`) USING BTREE,
  KEY `Index_fight` (`fight`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='玩家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player_data`
--

DROP TABLE IF EXISTS `player_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `player_data` (
  `playerId` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='玩家相关数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rank`
--

DROP TABLE IF EXISTS `rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rank` (
  `type` int(10) NOT NULL,
  `data` longblob NOT NULL,
  PRIMARY KEY (`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `serial_data`
--

DROP TABLE IF EXISTS `serial_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `serial_data` (
  `id` int(10) unsigned NOT NULL,
  `data` longblob,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='服务器全局数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `serverparam`
--

DROP TABLE IF EXISTS `serverparam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `serverparam` (
  `paramvalue` text NOT NULL,
  `text` varchar(255) DEFAULT NULL,
  `paramkey` varchar(255) NOT NULL,
  `serverid` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_charge_record`
--

DROP TABLE IF EXISTS `t_charge_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_charge_record` (
  `accountName` varchar(255) NOT NULL COMMENT '登录账号',
  `playerId` int(11) DEFAULT NULL COMMENT '角色ID',
  `nickName` varchar(255) DEFAULT NULL COMMENT '昵称',
  `totalCharge` int(11) DEFAULT NULL COMMENT '总共充值',
  `loginDays` varchar(255) DEFAULT NULL COMMENT '连续最大登录天数',
  PRIMARY KEY (`accountName`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_marry_rank`
--

DROP TABLE IF EXISTS `t_marry_rank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_marry_rank` (
  `openId` varchar(50) NOT NULL COMMENT 'openID',
  `nickName` varchar(255) DEFAULT NULL COMMENT '昵称',
  `avatarUrl` varchar(255) DEFAULT NULL COMMENT '头像地址',
  `score` int(11) DEFAULT '0',
  PRIMARY KEY (`openId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `task`
--

DROP TABLE IF EXISTS `task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task` (
  `playerId` int(10) unsigned NOT NULL COMMENT 'çŽ©å®¶id',
  `data` longblob COMMENT 'æ•°æ®',
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wb_data`
--

DROP TABLE IF EXISTS `wb_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wb_data` (
  `id` int(11) NOT NULL,
  `data` longblob COMMENT '世界BOSS记录',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-10-29  8:36:20
