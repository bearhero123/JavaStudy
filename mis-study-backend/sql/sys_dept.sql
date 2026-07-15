CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID，0为根节点',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    order_num INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag CHAR(1) NOT NULL DEFAULT '0',
    UNIQUE KEY uk_sys_dept_parent_name_normal (parent_id, dept_name, del_flag),
    KEY idx_sys_dept_parent_status (parent_id, status, del_flag)
) COMMENT='部门表';


