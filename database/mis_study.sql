/*
 Navicat Premium Dump SQL

 Source Server         : local-mysql
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : mis_study

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 13/07/2026 21:18:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for per_clients_info
-- ----------------------------
DROP TABLE IF EXISTS `per_clients_info`;
CREATE TABLE `per_clients_info`  (
  `client_id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `client_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '客户名称',
  `client_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户地址',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人职位',
  `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人邮箱',
  `client_level` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户级别',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
  PRIMARY KEY (`client_id`) USING BTREE,
  UNIQUE INDEX `uk_client_name_normal`(`client_name` ASC, `del_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '客户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of per_clients_info
-- ----------------------------
INSERT INTO `per_clients_info` VALUES (1, '北京测试客户有限公司', '北京市海淀区测试路 1 号', '张三', '采购经理', '13800000001', 'zhangsan@example.com', 'A', '第一条客户测试数据', 'admin', '2026-07-09 09:03:10', NULL, NULL, '0');
INSERT INTO `per_clients_info` VALUES (2, '上海样例客户有限公司', '上海市浦东新区样例路 2 号', '李四', '项目负责人', '13800000002', 'lisi@example.com', 'B', '第二条客户测试数据', 'admin', '2026-07-09 09:03:10', NULL, NULL, '0');

-- ----------------------------
-- Table structure for per_vendor_info
-- ----------------------------
DROP TABLE IF EXISTS `per_vendor_info`;
CREATE TABLE `per_vendor_info`  (
  `vendor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `vendor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '供应商名称',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人名字',
  `contact_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人职位',
  `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人邮箱地址',
  `vendor_ratings` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商评级',
  `vendor_addr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '供应商地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
  PRIMARY KEY (`vendor_id`) USING BTREE,
  UNIQUE INDEX `uk_vendor_name_normal`(`vendor_name` ASC, `del_flag` ASC) USING BTREE,
  INDEX `idx_vendor_ratings_del_flag`(`vendor_ratings` ASC, `del_flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of per_vendor_info
-- ----------------------------
INSERT INTO `per_vendor_info` VALUES (1, '北京测试供应商有限公司', '赵一', '销售经理', '13900000001', 'zhaoyi@example.com', 'A', '北京市朝阳区供应商路 1 号', '第一条供应商测试数据', 'admin', '2026-07-10 16:49:09', NULL, NULL, '0');
INSERT INTO `per_vendor_info` VALUES (2, '上海样例供应商有限公司', '钱二', '客户经理', '13900000002', 'qianer@example.com', 'B', '上海市浦东新区供应商路 2 号', '第二条供应商测试数据', 'admin', '2026-07-10 16:49:09', NULL, NULL, '0');
INSERT INTO `per_vendor_info` VALUES (7, '广州测试供应商有限公司', '赵一', '销售经理', '13900000001', 'zhaoyi@example.com', 'A', '北京市朝阳区供应商路 1 号', '第一条供应商测试数据', 'admin', '2026-07-13 09:24:22', NULL, NULL, '0');
INSERT INTO `per_vendor_info` VALUES (8, '深圳样例供应商有限公司', '钱二', '客户经理', '13900000002', 'qianer@example.com', 'B', '上海市浦东新区供应商路 2 号', '第二条供应商测试数据', 'admin', '2026-07-13 09:24:22', NULL, NULL, '0');
INSERT INTO `per_vendor_info` VALUES (9, '前端联调供应商-1783933454080', '联调联系人-已修改', '实施经理', '13900009999', 'frontend@example.com', 'B', '深圳市南山区前端联调路 13 号', '07.13 前端浏览器联调临时数据', 'admin', '2026-07-13 17:09:35', 'admin', '2026-07-13 17:12:33', '2');

-- ----------------------------
-- Table structure for pro_project_info
-- ----------------------------
DROP TABLE IF EXISTS `pro_project_info`;
CREATE TABLE `pro_project_info`  (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目编号',
  `project_abbreviation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目简称',
  `project_name_cn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目中文名称',
  `project_name_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目英文名称',
  `project_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目类型',
  `project_status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目状态',
  `pm_person_id` bigint NULL DEFAULT NULL COMMENT '项目经理人员ID',
  `pm_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目经理姓名',
  `client_id` bigint NULL DEFAULT NULL COMMENT '客户ID',
  `client_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '客户名称',
  `project_scale` decimal(12, 2) NULL DEFAULT NULL COMMENT '项目规模/金额',
  `currency` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '币种',
  `business_area` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务领域',
  `contract_status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '合同签署状态',
  `start_date` date NULL DEFAULT NULL COMMENT '项目开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '项目结束日期',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志：0存在，2删除',
  PRIMARY KEY (`project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目基本信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pro_project_info
-- ----------------------------
INSERT INTO `pro_project_info` VALUES (2, 'P002', '逻辑删除测试', '逻辑删除功能测试项目', 'Logic Delete Test Project', '内部项目', '进行中', NULL, '王五', NULL, '测试客户有限公司', 50000.00, 'CNY', '系统开发', '未签署', '2026-07-07', '2026-09-30', '这是一条专门用于测试逻辑删除的数据', 'admin', '2026-07-07 11:20:53', 'admin', '2026-07-07 11:23:58', '2');
INSERT INTO `pro_project_info` VALUES (3, 'P003', '分页筛选测试', '分页条件查询测试项目', 'Page Query Test Project', '内部项目', '进行中', NULL, '赵六', NULL, '分页测试客户公司', 88000.00, 'CNY', '软件开发', '已签署', '2026-07-08', '2026-10-31', '这是一条用于测试分页和条件查询的数据', 'admin', '2026-07-07 14:42:05', NULL, '2026-07-07 14:42:05', '0');
INSERT INTO `pro_project_info` VALUES (4, 'P001', '前端测试', 'VUE测试', 'Test', '外部项目', '进行中', NULL, '张三', NULL, 'bear', 100000.00, 'CNY', '科技', '未签署', '2026-07-07', '2026-07-31', '', NULL, '2026-07-08 15:53:17', NULL, '2026-07-08 15:53:30', '0');

SET FOREIGN_KEY_CHECKS = 1;
