-- 个人成长记录系统数据库表结构
-- 使用 record 数据源 (recorddb)

-- 人物表(profile)
CREATE TABLE IF NOT EXISTS profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    gender VARCHAR(10),
    birth_date DATE,
    avatar VARCHAR(255),
    creator_id BIGINT NOT NULL,
    status CHAR(1) DEFAULT '1',
    del_flag TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 人物成员关系表(profile_member)
CREATE TABLE IF NOT EXISTS profile_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_profile_user ON profile_member (profile_id, user_id);

-- 时间轴事件表(timeline_event)
CREATE TABLE IF NOT EXISTS timeline_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    title VARCHAR(255),
    event_time TIMESTAMP NOT NULL,
    type VARCHAR(30),
    visibility VARCHAR(20) DEFAULT 'family',
    created_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_profile_time ON timeline_event (profile_id, event_time);

-- 记录内容表(record)
CREATE TABLE IF NOT EXISTS record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    content CLOB,
    mood VARCHAR(20),
    location VARCHAR(100),
    weather VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_event ON record (event_id);

-- 附件表(media)
CREATE TABLE IF NOT EXISTS media (
    id BIGINT NOT NULL AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    type VARCHAR(20),
    size BIGINT,
    duration INT,
    width INT,
    height INT,
    sort INT DEFAULT 0,
    bucket VARCHAR(100),
    object_key VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_record ON media (record_id);

-- 标签表(tag)
CREATE TABLE IF NOT EXISTS tag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    creator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_name_creator ON tag (name, creator_id);

-- 记录标签关联表(record_tag)
CREATE TABLE IF NOT EXISTS record_tag (
    record_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (record_id, tag_id)
);

-- ==================== 样例数据 ====================

-- 插入人物数据
INSERT INTO profile (id, name, gender, birth_date, avatar, creator_id, status, del_flag) VALUES
(1, '张小明', '男', '2015-06-15', '/avatars/boy_001.jpg', 1001, '1', 0),
(2, '李小花', '女', '2018-03-22', '/avatars/girl_001.jpg', 1001, '1', 0),
(3, '王大宝', '男', '2020-09-10', '/avatars/baby_001.jpg', 1002, '1', 0);

-- 插入人物成员关系
INSERT INTO profile_member (profile_id, user_id, role) VALUES
(1, 1001, 'owner'),
(1, 1003, 'parent'),
(2, 1001, 'owner'),
(2, 1003, 'parent'),
(3, 1002, 'owner'),
(3, 1004, 'parent');

-- 插入时间轴事件
INSERT INTO timeline_event (id, profile_id, title, event_time, type, visibility, created_by) VALUES
(1, 1, '第一次走路', '2016-08-20 10:30:00', 'milestone', 'family', 1001),
(2, 1, '幼儿园第一天', '2018-09-01 08:00:00', 'milestone', 'family', 1001),
(3, 1, '周末公园游玩', '2024-04-06 14:00:00', 'daily', 'family', 1001),
(4, 2, '出生', '2018-03-22 15:20:00', 'milestone', 'family', 1001),
(5, 2, '学会叫妈妈', '2019-01-10 09:15:00', 'milestone', 'family', 1001),
(6, 3, '满月', '2020-10-10 12:00:00', 'milestone', 'family', 1002),
(7, 3, '第一次翻身', '2021-01-15 16:30:00', 'milestone', 'private', 1002);

-- 插入记录内容
INSERT INTO record (id, event_id, content, mood, location, weather) VALUES
(1, 1, '今天小明终于迈出了人生的第一步！虽然摇摇晃晃，但坚持走了好几步，太棒了！', 'happy', '家中客厅', '晴'),
(2, 2, '小明第一天上幼儿园，刚开始有点紧张，但很快就和小朋友们玩在一起了。老师说他表现很好！', 'excited', '阳光幼儿园', '多云'),
(3, 3, '周末带小明去公园放风筝，天气真好，孩子玩得很开心，还认识了新朋友。', 'happy', '中央公园', '晴'),
(4, 4, '小花平安出生，体重3.2kg，母女平安。全家都很开心！', 'touched', '市妇幼保健院', '阴'),
(5, 5, '小花今天突然清晰地叫了一声"妈妈"，声音甜甜的，心都化了！', 'happy', '家中', '晴'),
(6, 6, '大宝满月啦！今天办了一个小型的满月宴，亲友们都来了，收到很多祝福。', 'happy', '家中', '晴'),
(7, 7, '大宝今天自己翻过身了，动作很利索，看来离爬行不远了！', 'proud', '卧室', '多云');

-- 插入附件(图片/视频)
INSERT INTO media (record_id, url, type, size, width, height, sort, bucket, object_key) VALUES
(1, '/media/walking_001.jpg', 'image', 2048576, 1920, 1080, 1, 'growth-photos', '2016/08/walking_001.jpg'),
(1, '/media/walking_002.jpg', 'image', 1856432, 1920, 1080, 2, 'growth-photos', '2016/08/walking_002.jpg'),
(2, '/media/kindergarten_001.jpg', 'image', 3145728, 2048, 1536, 1, 'growth-photos', '2018/09/kindergarten_001.jpg'),
(2, '/media/kindergarten_video.mp4', 'video', 15728640, 1920, 1080, 2, 'growth-videos', '2018/09/kindergarten_001.mp4'),
(3, '/media/park_001.jpg', 'image', 2621440, 1920, 1080, 1, 'growth-photos', '2024/04/park_001.jpg'),
(3, '/media/park_002.jpg', 'image', 2359296, 1920, 1080, 2, 'growth-photos', '2024/04/park_002.jpg'),
(3, '/media/park_kite.jpg', 'image', 1966080, 1920, 1080, 3, 'growth-photos', '2024/04/park_kite.jpg'),
(4, '/media/birth_001.jpg', 'image', 1572864, 1920, 1080, 1, 'growth-photos', '2018/03/birth_001.jpg'),
(5, '/media/call_mama.mp4', 'video', 8388608, 1920, 1080, 1, 'growth-videos', '2019/01/call_mama.mp4'),
(6, '/media/fullmoon_001.jpg', 'image', 2883584, 2048, 1536, 1, 'growth-photos', '2020/10/fullmoon_001.jpg'),
(6, '/media/fullmoon_002.jpg', 'image', 2621440, 2048, 1536, 2, 'growth-photos', '2020/10/fullmoon_002.jpg'),
(7, '/media/rollover.mp4', 'video', 6291456, 1920, 1080, 1, 'growth-videos', '2021/01/rollover.mp4');

-- 插入标签
INSERT INTO tag (id, name, creator_id) VALUES
(1, '第一次', 1001),
(2, '成长里程碑', 1001),
(3, '幼儿园', 1001),
(4, '户外活动', 1001),
(5, '家庭聚会', 1001),
(6, '重要时刻', 1002),
(7, '婴儿期', 1002);

-- 插入记录标签关联
INSERT INTO record_tag (record_id, tag_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(2, 2),
(3, 4),
(4, 1),
(4, 6),
(5, 1),
(5, 2),
(6, 5),
(6, 6),
(7, 1),
(7, 7);

-- ==================== 重置自增序列 ====================
-- H2数据库在手动插入ID后需要重置序列，避免主键冲突
ALTER TABLE profile ALTER COLUMN id RESTART WITH 100;
ALTER TABLE timeline_event ALTER COLUMN id RESTART WITH 100;
ALTER TABLE record ALTER COLUMN id RESTART WITH 100;
ALTER TABLE media ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tag ALTER COLUMN id RESTART WITH 100;
