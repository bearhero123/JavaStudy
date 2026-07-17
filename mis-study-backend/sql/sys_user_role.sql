CREATE TABLE IF NOT EXISTS sys_user_role (
                                             user_id BIGINT NOT NULL COMMENT '用户ID',
                                             role_id BIGINT NOT NULL COMMENT '角色ID',
                                             PRIMARY KEY (user_id, role_id),
                                             KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与角色关联表';