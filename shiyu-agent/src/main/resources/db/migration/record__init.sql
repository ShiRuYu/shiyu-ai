-- 个人成长记录系统数据库表结构
-- 使用 record 数据源 (recorddb)

-- 人物表(profile)
DROP TABLE IF EXISTS profile;
CREATE TABLE profile (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '人物ID',
    name VARCHAR(64) NOT NULL COMMENT '姓名',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    gender TINYINT DEFAULT 2 COMMENT '性别（0男 1女 2未知）',
    birth_date DATE COMMENT '出生日期',
    avatar VARCHAR(255) COMMENT '头像URL',
    status CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);
COMMENT ON TABLE profile IS '人物表';

-- 人物成员关系表(profile_member)
DROP TABLE IF EXISTS profile_member;
CREATE TABLE profile_member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    profile_id BIGINT NOT NULL COMMENT '人物ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    role VARCHAR(20) COMMENT '角色（owner/parent等）',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);
CREATE INDEX idx_profile_user ON profile_member (profile_id, user_id);
COMMENT ON TABLE profile_member IS '人物成员关系表';

-- 时间轴事件表(timeline_event)
DROP TABLE IF EXISTS timeline_event;
CREATE TABLE timeline_event (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    profile_id BIGINT NOT NULL COMMENT '人物ID',
    title VARCHAR(255) COMMENT '事件标题',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    event_time TIMESTAMP NOT NULL COMMENT '事件时间',
    type VARCHAR(30) COMMENT '事件类型（milestone/daily等）',
    visibility VARCHAR(20) DEFAULT 'family' COMMENT '可见性（family/private等）',
    created_by BIGINT COMMENT '创建者ID',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);
CREATE INDEX idx_profile_time ON timeline_event (profile_id, event_time);
COMMENT ON TABLE timeline_event IS '时间轴事件表';

-- 记录内容表(record)
DROP TABLE IF EXISTS record;
CREATE TABLE record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    event_id BIGINT NOT NULL COMMENT '事件ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    content CLOB COMMENT '记录内容',
    mood VARCHAR(20) COMMENT '心情',
    location VARCHAR(100) COMMENT '地点',
    weather VARCHAR(50) COMMENT '天气',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);
CREATE INDEX idx_event ON record (event_id);
COMMENT ON TABLE record IS '记录内容表';

-- 附件表(media)
DROP TABLE IF EXISTS media;
CREATE TABLE media (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    record_id BIGINT NOT NULL COMMENT '记录ID',
    url VARCHAR(500) NOT NULL COMMENT '文件URL',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    type VARCHAR(20) COMMENT '文件类型（image/video等）',
    size BIGINT COMMENT '文件大小（字节）',
    duration INT COMMENT '时长（秒，仅视频）',
    width INT COMMENT '宽度（像素）',
    height INT COMMENT '高度（像素）',
    sort INT DEFAULT 0 COMMENT '排序',
    bucket VARCHAR(100) COMMENT '存储桶名称',
    object_key VARCHAR(255) COMMENT '对象键',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);
CREATE INDEX idx_record ON media (record_id);
COMMENT ON TABLE media IS '附件表';

-- 标签表(tag)
DROP TABLE IF EXISTS tag;
CREATE TABLE tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    create_by VARCHAR(64) COMMENT '创建者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) COMMENT '更新者',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
);
CREATE INDEX idx_name_creator ON tag (name, create_by);
COMMENT ON TABLE tag IS '标签表';

-- 记录标签关联表(record_tag)
DROP TABLE IF EXISTS record_tag;
CREATE TABLE record_tag (
    record_id BIGINT NOT NULL COMMENT '记录ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    PRIMARY KEY (record_id, tag_id)
);
COMMENT ON TABLE record_tag IS '记录标签关联表';

-- ==================== 样例数据 ====================

-- 插入人物数据
INSERT INTO profile (id, name, tenant_id, gender, birth_date, avatar, status, del_flag, create_by, update_by) VALUES
(1, '张小明', 1, 0, '2015-06-15', '/avatars/boy_001.jpg', '1', 0, 'system', 'system'),
(2, '李小花', 1, 1, '2018-03-22', '/avatars/girl_001.jpg', '1', 0, 'system', 'system'),
(3, '王大宝', 1, 0, '2020-09-10', '/avatars/baby_001.jpg', '1', 0, 'system', 'system');

-- 插入人物成员关系
INSERT INTO profile_member (profile_id, user_id, tenant_id, role, create_by, update_by) VALUES
(1, 1001, 1, 'owner', 'system', 'system'),
(1, 1003, 1, 'parent', 'system', 'system'),
(2, 1001, 1, 'owner', 'system', 'system'),
(2, 1003, 1, 'parent', 'system', 'system'),
(3, 1002, 1, 'owner', 'system', 'system'),
(3, 1004, 1, 'parent', 'system', 'system');

-- 插入时间轴事件
INSERT INTO timeline_event (id, profile_id, title, tenant_id, event_time, type, visibility, created_by, create_by, update_by) VALUES
(1, 1, '第一次走路', 1, '2016-08-20 10:30:00', 'milestone', 'family', 1001, 'system', 'system'),
(2, 1, '幼儿园第一天', 1, '2018-09-01 08:00:00', 'milestone', 'family', 1001, 'system', 'system'),
(3, 1, '周末公园游玩', 1, '2024-04-06 14:00:00', 'daily', 'family', 1001, 'system', 'system'),
(4, 2, '出生', 1, '2018-03-22 15:20:00', 'milestone', 'family', 1001, 'system', 'system'),
(5, 2, '学会叫妈妈', 1, '2019-01-10 09:15:00', 'milestone', 'family', 1001, 'system', 'system'),
(6, 3, '满月', 1, '2020-10-10 12:00:00', 'milestone', 'family', 1002, 'system', 'system'),
(7, 3, '第一次翻身', 1, '2021-01-15 16:30:00', 'milestone', 'private', 1002, 'system', 'system');

-- 插入记录内容
INSERT INTO record (id, event_id, tenant_id, content, mood, location, weather, create_by, update_by) VALUES
(1, 1, 1, '今天小明终于迈出了人生的第一步！虽然摇摇晃晃，但坚持走了好几步，太棒了！', 'happy', '家中客厅', '晴', 'system', 'system'),
(2, 2, 1, '小明第一天上幼儿园，刚开始有点紧张，但很快就和小朋友们玩在一起了。老师说他表现很好！', 'excited', '阳光幼儿园', '多云', 'system', 'system'),
(3, 3, 1, '周末带小明去公园放风筝，天气真好，孩子玩得很开心，还认识了新朋友。', 'happy', '中央公园', '晴', 'system', 'system'),
(4, 4, 1, '小花平安出生，体重3.2kg，母女平安。全家都很开心！', 'touched', '市妇幼保健院', '阴', 'system', 'system'),
(5, 5, 1, '小花今天突然清晰地叫了一声“妈妈”，声音甜甜的，心都化了！', 'happy', '家中', '晴', 'system', 'system'),
(6, 6, 1, '大宝满月啦！今天办了一个小型的满月宴，亲友们都来了，收到很多祝福。', 'happy', '家中', '晴', 'system', 'system'),
(7, 7, 1, '大宝今天自己翻过身了，动作很利索，看来离爬行不远了！', 'proud', '卧室', '多云', 'system', 'system');

-- 插入附件(图片/视频)
INSERT INTO media (id, record_id, url, tenant_id, type, size, width, height, sort, bucket, object_key, create_by, update_by) VALUES
(1, 1, '/media/walking_001.jpg', 1, 'image', 2048576, 1920, 1080, 1, 'growth-photos', '2016/08/walking_001.jpg', 'system', 'system'),
(1, 1, '/media/walking_002.jpg', 1, 'image', 1856432, 1920, 1080, 2, 'growth-photos', '2016/08/walking_002.jpg', 'system', 'system'),
(2, 2, '/media/kindergarten_001.jpg', 1, 'image', 3145728, 2048, 1536, 1, 'growth-photos', '2018/09/kindergarten_001.jpg', 'system', 'system'),
(2, 2, '/media/kindergarten_video.mp4', 1, 'video', 15728640, 1920, 1080, 2, 'growth-videos', '2018/09/kindergarten_001.mp4', 'system', 'system'),
(3, 3, '/media/park_001.jpg', 1, 'image', 2621440, 1920, 1080, 1, 'growth-photos', '2024/04/park_001.jpg', 'system', 'system'),
(3, 3, '/media/park_002.jpg', 1, 'image', 2359296, 1920, 1080, 2, 'growth-photos', '2024/04/park_002.jpg', 'system', 'system'),
(3, 3, '/media/park_kite.jpg', 1, 'image', 1966080, 1920, 1080, 3, 'growth-photos', '2024/04/park_kite.jpg', 'system', 'system'),
(4, 4, '/media/birth_001.jpg', 1, 'image', 1572864, 1920, 1080, 1, 'growth-photos', '2018/03/birth_001.jpg', 'system', 'system'),
(5, 5, '/media/call_mama.mp4', 1, 'video', 8388608, 1920, 1080, 1, 'growth-videos', '2019/01/call_mama.mp4', 'system', 'system'),
(6, 6, '/media/fullmoon_001.jpg', 1, 'image', 2883584, 2048, 1536, 1, 'growth-photos', '2020/10/fullmoon_001.jpg', 'system', 'system'),
(6, 6, '/media/fullmoon_002.jpg', 1, 'image', 2621440, 2048, 1536, 2, 'growth-photos', '2020/10/fullmoon_002.jpg', 'system', 'system'),
(7, 7, '/media/rollover.mp4', 1, 'video', 6291456, 1920, 1080, 1, 'growth-videos', '2021/01/rollover.mp4', 'system', 'system');

-- 插入标签
INSERT INTO tag (id, name, tenant_id, create_by, update_by) VALUES
(1, '第一次', 1, 'system', 'system'),
(2, '成长里程碑', 1, 'system', 'system'),
(3, '幼儿园', 1, 'system', 'system'),
(4, '户外活动', 1, 'system', 'system'),
(5, '家庭聚会', 1, 'system', 'system'),
(6, '重要时刻', 1, 'system', 'system'),
(7, '婴儿期', 1, 'system', 'system');

-- 插入记录标签关联
INSERT INTO record_tag (record_id, tag_id, tenant_id) VALUES
(1, 1, 1), (1, 2, 1), (2, 3, 1), (2, 2, 1),
(3, 4, 1), (4, 1, 1), (4, 6, 1), (5, 1, 1),
(5, 2, 1), (6, 5, 1), (6, 6, 1), (7, 1, 1), (7, 7, 1);

-- ==================== 重置自增序列 ====================
ALTER TABLE profile ALTER COLUMN id RESTART WITH 100;
ALTER TABLE timeline_event ALTER COLUMN id RESTART WITH 100;
ALTER TABLE record ALTER COLUMN id RESTART WITH 100;
ALTER TABLE media ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tag ALTER COLUMN id RESTART WITH 100;
