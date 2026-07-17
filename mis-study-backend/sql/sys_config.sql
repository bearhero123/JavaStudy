CREATE TABLE IF NOT EXISTS sys_config (
                                          config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
                                          config_name VARCHAR(100) NOT NULL COMMENT '参数名称',
                                          config_key VARCHAR(100) NOT NULL COMMENT '参数键名',
                                          config_value VARCHAR(500) NOT NULL DEFAULT '' COMMENT '参数键值',
                                          config_type CHAR(1) NOT NULL DEFAULT 'N' COMMENT '系统内置：Y是，N否',
                                          remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                          create_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
                                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          update_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
                                          update_time DATETIME DEFAULT NULL COMMENT '更新时间',
                                          del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
                                          UNIQUE KEY uk_sys_config_key_normal (config_key, del_flag),
                                          KEY idx_sys_config_type_name (config_type, del_flag, config_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统参数表';