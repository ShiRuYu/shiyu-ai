-- ============================================
-- Data: data_record
-- ============================================

INSERT IGNORE INTO profile (id, name, tenant_id, workspace_id, gender, birth_date, avatar, status, del_flag, create_by, update_by) VALUES
(1, '张小明', 1, 0, 0, '2015-06-15', '/avatars/boy_001.jpg', '1', 0, 'system', 'system'),
(2, '李小花', 1, 0, 1, '2018-03-22', '/avatars/girl_001.jpg', '1', 0, 'system', 'system'),
(3, '王大宝', 1, 0, 0, '2020-09-10', '/avatars/baby_001.jpg', '1', 0, 'system', 'system');

INSERT IGNORE INTO profile_member (profile_id, user_id, tenant_id, workspace_id, role, create_by, update_by) VALUES
(1, 1001, 1, 0, 'owner', 'system', 'system'),
(1, 1003, 1, 0, 'parent', 'system', 'system'),
(2, 1001, 1, 0, 'owner', 'system', 'system'),
(2, 1003, 1, 0, 'parent', 'system', 'system'),
(3, 1002, 1, 0, 'owner', 'system', 'system'),
(3, 1004, 1, 0, 'parent', 'system', 'system');

INSERT IGNORE INTO timeline_event (id, profile_id, title, tenant_id, workspace_id, event_time, type, visibility, created_by, create_by, update_by) VALUES
(1, 1, '第一次走路', 1, 0, '2016-08-20 10:30:00', 'milestone', 'family', 1001, 'system', 'system'),
(2, 1, '幼儿园第一天', 1, 0, '2018-09-01 08:00:00', 'milestone', 'family', 1001, 'system', 'system'),
(3, 1, '周末公园游玩', 1, 0, '2024-04-06 14:00:00', 'daily', 'family', 1001, 'system', 'system'),
(4, 2, '出生', 1, 0, '2018-03-22 15:20:00', 'milestone', 'family', 1001, 'system', 'system'),
(5, 2, '学会叫妈妈', 1, 0, '2019-01-10 09:15:00', 'milestone', 'family', 1001, 'system', 'system'),
(6, 3, '满月', 1, 0, '2020-10-10 12:00:00', 'milestone', 'family', 1002, 'system', 'system'),
(7, 3, '第一次翻身', 1, 0, '2021-01-15 16:30:00', 'milestone', 'private', 1002, 'system', 'system');

INSERT IGNORE INTO record (id, event_id, tenant_id, workspace_id, content, mood, location, weather, create_by, update_by) VALUES
(1, 1, 1, 0, '今天小明终于迈出了人生的第一步！虽然摇摇晃晃，但坚持走了好几步，太棒了！', 'happy', '家中客厅', '晴', 'system', 'system'),
(2, 2, 1, 0, '小明第一天上幼儿园，刚开始有点紧张，但很快就和小朋友们玩在一起了。老师说他表现很好！', 'excited', '阳光幼儿园', '多云', 'system', 'system'),
(3, 3, 1, 0, '周末带小明去公园放风筝，天气真好，孩子玩得很开心，还认识了新朋友。', 'happy', '中央公园', '晴', 'system', 'system'),
(4, 4, 1, 0, '小花平安出生，体重3.2kg，母女平安。全家都很开心！', 'touched', '市妇幼保健院', '阴', 'system', 'system'),
(5, 5, 1, 0, '小花今天突然清晰地叫了一声“妈妈”，声音甜甜的，心都化了！', 'happy', '家中', '晴', 'system', 'system'),
(6, 6, 1, 0, '大宝满月啦！今天办了一个小型的满月宴，亲友们都来了，收到很多祝福。', 'happy', '家中', '晴', 'system', 'system'),
(7, 7, 1, 0, '大宝今天自己翻过身了，动作很利索，看来离爬行不远了！', 'proud', '卧室', '多云', 'system', 'system');

INSERT IGNORE INTO media (id, record_id, url, tenant_id, workspace_id, type, size, width, height, sort, bucket, object_key, create_by, update_by) VALUES
(1, 1, '/media/walking_001.jpg', 1, 0, 'image', 2048576, 1920, 1080, 1, 'growth-photos', '2016/08/walking_001.jpg', 'system', 'system'),
(2, 1, '/media/walking_002.jpg', 1, 0, 'image', 1856432, 1920, 1080, 2, 'growth-photos', '2016/08/walking_002.jpg', 'system', 'system'),
(3, 2, '/media/kindergarten_001.jpg', 1, 0, 'image', 3145728, 2048, 1536, 1, 'growth-photos', '2018/09/kindergarten_001.jpg', 'system', 'system'),
(4, 2, '/media/kindergarten_video.mp4', 1, 0, 'video', 15728640, 1920, 1080, 2, 'growth-videos', '2018/09/kindergarten_001.mp4', 'system', 'system'),
(5, 3, '/media/park_001.jpg', 1, 0, 'image', 2621440, 1920, 1080, 1, 'growth-photos', '2024/04/park_001.jpg', 'system', 'system'),
(6, 3, '/media/park_002.jpg', 1, 0, 'image', 2359296, 1920, 1080, 2, 'growth-photos', '2024/04/park_002.jpg', 'system', 'system'),
(7, 3, '/media/park_kite.jpg', 1, 0, 'image', 1966080, 1920, 1080, 3, 'growth-photos', '2024/04/park_kite.jpg', 'system', 'system'),
(8, 4, '/media/birth_001.jpg', 1, 0, 'image', 1572864, 1920, 1080, 1, 'growth-photos', '2018/03/birth_001.jpg', 'system', 'system'),
(9, 5, '/media/call_mama.mp4', 1, 0, 'video', 8388608, 1920, 1080, 1, 'growth-videos', '2019/01/call_mama.mp4', 'system', 'system'),
(10, 6, '/media/fullmoon_001.jpg', 1, 0, 'image', 2883584, 2048, 1536, 1, 'growth-photos', '2020/10/fullmoon_001.jpg', 'system', 'system'),
(11, 6, '/media/fullmoon_002.jpg', 1, 0, 'image', 2621440, 2048, 1536, 2, 'growth-photos', '2020/10/fullmoon_002.jpg', 'system', 'system'),
(12, 7, '/media/rollover.mp4', 1, 0, 'video', 6291456, 1920, 1080, 1, 'growth-videos', '2021/01/rollover.mp4', 'system', 'system');

INSERT IGNORE INTO tag (id, name, tenant_id, workspace_id, create_by, update_by) VALUES
(1, '第一次', 1, 0, 'system', 'system'),
(2, '成长里程碑', 1, 0, 'system', 'system'),
(3, '幼儿园', 1, 0, 'system', 'system'),
(4, '户外活动', 1, 0, 'system', 'system'),
(5, '家庭聚会', 1, 0, 'system', 'system'),
(6, '重要时刻', 1, 0, 'system', 'system'),
(7, '婴儿期', 1, 0, 'system', 'system');

INSERT IGNORE INTO record_tag (record_id, tag_id, tenant_id, workspace_id) VALUES
(1, 1, 1, 0), (1, 2, 1, 0), (2, 3, 1, 0), (2, 2, 1, 0),
(3, 4, 1, 0), (4, 1, 1, 0), (4, 6, 1, 0), (5, 1, 1, 0),
(5, 2, 1, 0), (6, 5, 1, 0), (6, 6, 1, 0), (7, 1, 1, 0), (7, 7, 1, 0);

