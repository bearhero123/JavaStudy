CREATE TABLE IF NOT EXISTS sys_post (
    post_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    post_name VARCHAR(100) NOT NULL COMMENT '岗位名称',
    post_sort INT NOT NULL DEFAULT 0 COMMENT '岗位排序',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by VARCHAR(64) DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64) DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    del_flag CHAR(1) NOT NULL DEFAULT '0',
    UNIQUE KEY uk_sys_post_code_normal (post_code, del_flag),
    UNIQUE KEY uk_sys_post_name_normal (post_name, del_flag),
    KEY idx_sys_post_status_sort (status, del_flag, post_sort)
) COMMENT='岗位表';






