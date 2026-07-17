CREATE TABLE IF NOT EXISTS sys_menu (
                                        menu_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
                                        parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID，0为根节点',
                                        menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
                                        order_num INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
                                        path VARCHAR(200) NOT NULL DEFAULT '' COMMENT '路由地址，仅作业务数据记录',
                                        component VARCHAR(255) DEFAULT NULL COMMENT '组件路径，仅作业务数据记录',
                                        menu_type CHAR(1) NOT NULL DEFAULT 'C' COMMENT '菜单类型：M目录，C菜单，F按钮',
                                        visible CHAR(1) NOT NULL DEFAULT '0' COMMENT '显示状态：0显示，1隐藏',
                                        status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
                                        perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识，仅作业务数据记录',
                                        icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
                                        remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                        create_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
                                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        update_by VARCHAR(64) DEFAULT NULL COMMENT '更新人',
                                        update_time DATETIME DEFAULT NULL COMMENT '更新时间',
                                        del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0正常，2删除',
                                        UNIQUE KEY uk_sys_menu_parent_name_normal (parent_id, menu_name, del_flag),
                                        KEY idx_sys_menu_parent_sort (parent_id, del_flag, order_num),
                                        KEY idx_sys_menu_status_visible (status, visible, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

