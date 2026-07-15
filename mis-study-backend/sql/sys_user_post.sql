CREATE TABLE IF NOT EXISTS sys_user_post (
     user_id BIGINT NOT NULL COMMENT '用户ID',
     post_id BIGINT NOT NULL COMMENT '岗位ID',
     PRIMARY KEY (user_id, post_id),
     KEY idx_sys_user_post_post_id (post_id)
) COMMENT='用户与岗位关联表';