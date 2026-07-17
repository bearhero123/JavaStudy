CREATE TABLE IF NOT EXISTS sys_role (
                                        role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
                                        role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
                                        role_key VARCHAR(100) NOT NULL COMMENT '角色权限字符',
                                        role_sort INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
                                        status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
                                        remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                        create_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
                                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        update_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
                                        update_time DATETIME DEFAULT NULL COMMENT '更新时间',
                                        del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
                                        UNIQUE KEY uk_sys_role_name_normal (role_name, del_flag),
                                        UNIQUE KEY uk_sys_role_key_normal (role_key, del_flag),
                                        KEY idx_sys_role_status_sort (status, del_flag, role_sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';



