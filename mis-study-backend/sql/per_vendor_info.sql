CREATE TABLE IF NOT EXISTS per_vendor_info (
                                               vendor_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商ID',
                                               vendor_name VARCHAR(100) NOT NULL COMMENT '供应商名称',
                                               contact_name VARCHAR(50) NOT NULL COMMENT '联系人',
                                               contact_position VARCHAR(50) DEFAULT NULL COMMENT '联系人职位',
                                               contact_phone VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
                                               contact_email VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
                                               vendor_ratings VARCHAR(30) DEFAULT NULL COMMENT '供应商评级',
                                               vendor_addr VARCHAR(255) DEFAULT NULL COMMENT '供应商地址',
                                               remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                               create_by VARCHAR(64) DEFAULT NULL,
                                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                               update_by VARCHAR(64) DEFAULT NULL,
                                               update_time DATETIME DEFAULT NULL,
                                               del_flag CHAR(1) NOT NULL DEFAULT '0',
                                               UNIQUE KEY uk_vendor_name_normal (vendor_name, del_flag),
                                               KEY idx_vendor_ratings_del_flag (vendor_ratings, del_flag)
) COMMENT='供应商信息表';


INSERT INTO per_vendor_info
(vendor_name, contact_name, contact_position, contact_phone, contact_email, vendor_ratings, vendor_addr, remark, create_by, del_flag)
VALUES
    ('广州测试供应商有限公司', '赵一', '销售经理', '13900000001', 'zhaoyi@example.com', 'A', '北京市朝阳区供应商路 1 号', '第一条供应商测试数据', 'admin', '0'),
    ('深圳样例供应商有限公司', '钱二', '客户经理', '13900000002', 'qianer@example.com', 'B', '上海市浦东新区供应商路 2 号', '第二条供应商测试数据', 'admin', '0');