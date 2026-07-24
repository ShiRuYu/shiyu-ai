-- ============================================
-- Schema: schema_record
-- ============================================


CREATE TABLE IF NOT EXISTS profile (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '人物ID',
    name VARCHAR(64) NOT NULL COMMENT '姓名',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    scoped_tenant_id BIGINT NOT NULL COMMENT '作用域租户ID',
    gender TINYINT DEFAULT 2 COMMENT '性别（0男 1女 2未知）',
    birth_date DATE COMMENT '出生日期',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_profile_scope ON profile (scoped_tenant_id);

COMMENT ON TABLE profile IS '人物表';


CREATE TABLE IF NOT EXISTS profile_member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    profile_id BIGINT NOT NULL COMMENT '人物ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    scoped_tenant_id BIGINT NOT NULL COMMENT '作用域租户ID',
    role VARCHAR(20) COMMENT '角色（owner/parent等）',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_profile_user ON profile_member (profile_id, user_id);

CREATE INDEX IF NOT EXISTS idx_pm_scope ON profile_member (scoped_tenant_id);

COMMENT ON TABLE profile_member IS '人物成员关系表';


CREATE TABLE IF NOT EXISTS timeline_event (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    profile_id BIGINT NOT NULL COMMENT '人物ID',
    title VARCHAR(255) COMMENT '事件标题',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    scoped_tenant_id BIGINT NOT NULL COMMENT '作用域租户ID',
    event_time TIMESTAMP NOT NULL COMMENT '事件时间',
    type VARCHAR(30) COMMENT '事件类型（milestone/daily等）',
    visibility VARCHAR(20) DEFAULT 'family' COMMENT '可见性（family/private等）',
    status TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_profile_time ON timeline_event (profile_id, event_time);

CREATE INDEX IF NOT EXISTS idx_te_scope ON timeline_event (scoped_tenant_id);

COMMENT ON TABLE timeline_event IS '时间轴事件表';


CREATE TABLE IF NOT EXISTS record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    event_id BIGINT NOT NULL COMMENT '事件ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    scoped_tenant_id BIGINT NOT NULL COMMENT '作用域租户ID',
    content CLOB COMMENT '记录内容',
    mood VARCHAR(20) COMMENT '心情',
    location VARCHAR(100) COMMENT '地点',
    weather VARCHAR(50) COMMENT '天气',
    status TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_event ON record (event_id);

CREATE INDEX IF NOT EXISTS idx_record_scope ON record (scoped_tenant_id);

COMMENT ON TABLE record IS '记录内容表';


CREATE TABLE IF NOT EXISTS media (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    record_id BIGINT NOT NULL COMMENT '记录ID',
    url VARCHAR(500) NOT NULL COMMENT '文件URL',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    scoped_tenant_id BIGINT NOT NULL COMMENT '作用域租户ID',
    type VARCHAR(20) COMMENT '文件类型（image/video等）',
    size BIGINT COMMENT '文件大小（字节）',
    duration INT COMMENT '时长（秒，仅视频）',
    width INT COMMENT '宽度（像素）',
    height INT COMMENT '高度（像素）',
    sort INT DEFAULT 0 COMMENT '排序',
    bucket VARCHAR(100) COMMENT '存储桶名称',
    object_key VARCHAR(255) COMMENT '对象键',
    status TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_record ON media (record_id);

CREATE INDEX IF NOT EXISTS idx_media_scope ON media (scoped_tenant_id);

COMMENT ON TABLE media IS '附件表';


CREATE TABLE IF NOT EXISTS tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    status TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    scoped_tenant_id BIGINT NOT NULL COMMENT '作用域租户ID',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_name_creator ON tag (name, create_by);

CREATE INDEX IF NOT EXISTS idx_tag_scope ON tag (scoped_tenant_id);

COMMENT ON TABLE tag IS '标签表';


CREATE TABLE IF NOT EXISTS record_tag (
    record_id BIGINT NOT NULL COMMENT '记录ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    scoped_tenant_id BIGINT NOT NULL COMMENT '作用域租户ID',
    PRIMARY KEY (record_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_rt_scope ON record_tag (scoped_tenant_id);

COMMENT ON TABLE record_tag IS '记录标签关联表';

