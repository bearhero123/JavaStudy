CREATE TABLE IF NOT EXISTS sys_role_menu (
                                             role_id BIGINT NOT NULL COMMENT '角色ID',
                                             menu_id BIGINT NOT NULL COMMENT '菜单ID',
                                             PRIMARY KEY (role_id, menu_id),
                                             KEY idx_sys_role_menu_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色与菜单关联表';