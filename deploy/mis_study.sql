-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: mis_study
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `mis_study`
--

/*!40000 DROP DATABASE IF EXISTS `mis_study`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `mis_study` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `mis_study`;

--
-- Table structure for table `per_clients_info`
--

DROP TABLE IF EXISTS `per_clients_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `per_clients_info` (
  `client_id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `client_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户名称',
  `client_address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户地址',
  `contact_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人姓名',
  `contact_position` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人职位',
  `contact_phone` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系人邮箱',
  `client_level` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户级别',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
  PRIMARY KEY (`client_id`),
  UNIQUE KEY `uk_client_name_normal` (`client_name`,`del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `per_clients_info`
--

LOCK TABLES `per_clients_info` WRITE;
/*!40000 ALTER TABLE `per_clients_info` DISABLE KEYS */;
INSERT INTO `per_clients_info` VALUES (1,'北京测试客户有限公司','北京市海淀区测试路 1 号','张三','采购经理','13800000001','zhangsan@example.com','A','第一条客户测试数据','admin','2026-07-09 09:03:10',NULL,NULL,'0'),(2,'上海样例客户有限公司','上海市浦东新区样例路 2 号','李四','项目负责人','13800000002','lisi@example.com','B','第二条客户测试数据','admin','2026-07-09 09:03:10',NULL,NULL,'0');
/*!40000 ALTER TABLE `per_clients_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pro_project_info`
--

DROP TABLE IF EXISTS `pro_project_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pro_project_info` (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_number` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目编号',
  `project_abbreviation` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目简称',
  `project_name_cn` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目中文名称',
  `project_name_en` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目英文名称',
  `project_type` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目类型',
  `project_status` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目状态',
  `pm_person_id` bigint DEFAULT NULL COMMENT '项目经理人员ID',
  `pm_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '项目经理姓名',
  `client_id` bigint DEFAULT NULL COMMENT '客户ID',
  `client_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户名称',
  `project_scale` decimal(12,2) DEFAULT NULL COMMENT '项目规模/金额',
  `currency` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '币种',
  `business_area` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务领域',
  `contract_status` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '合同签署状态',
  `start_date` date DEFAULT NULL COMMENT '项目开始日期',
  `end_date` date DEFAULT NULL COMMENT '项目结束日期',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志：0存在，2删除',
  PRIMARY KEY (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pro_project_info`
--

LOCK TABLES `pro_project_info` WRITE;
/*!40000 ALTER TABLE `pro_project_info` DISABLE KEYS */;
INSERT INTO `pro_project_info` VALUES (2,'P002','逻辑删除测试','逻辑删除功能测试项目','Logic Delete Test Project','内部项目','进行中',NULL,'王五',NULL,'测试客户有限公司',50000.00,'CNY','系统开发','未签署','2026-07-07','2026-09-30','这是一条专门用于测试逻辑删除的数据','admin','2026-07-07 11:20:53','admin','2026-07-07 11:23:58','2'),(3,'P003','分页筛选测试','分页条件查询测试项目','Page Query Test Project','内部项目','进行中',NULL,'赵六',NULL,'分页测试客户公司',88000.00,'CNY','软件开发','已签署','2026-07-08','2026-10-31','这是一条用于测试分页和条件查询的数据','admin','2026-07-07 14:42:05',NULL,'2026-07-07 14:42:05','0'),(4,'P001','前端测试','VUE测试','Test','外部项目','进行中',NULL,'张三',NULL,'bear',100000.00,'CNY','科技','未签署','2026-07-07','2026-07-31','',NULL,'2026-07-08 15:53:17',NULL,'2026-07-08 15:53:30','0');
/*!40000 ALTER TABLE `pro_project_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'mis_study'
--

--
-- Dumping routines for database 'mis_study'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-10  1:16:34
