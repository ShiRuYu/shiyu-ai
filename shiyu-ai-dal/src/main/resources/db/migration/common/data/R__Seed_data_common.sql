-- ============================================
-- Data: common — 字典数据
-- ============================================

-- 时区
INSERT IGNORE INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (1, 'timezone', 'America/New_York (GMT-5)', 'America/New_York', 1, 1, 'N', 1, '美国纽约时区', 'system', 'system'),
(2, 'timezone', 'Europe/London (GMT0)', 'Europe/London', 1, 2, 'N', 1, '欧洲伦敦时区', 'system', 'system'),
(3, 'timezone', 'Asia/Shanghai (GMT+8)', 'Asia/Shanghai', 1, 3, 'Y', 1, '亚洲上海时区', 'system', 'system'),
(4, 'timezone', 'Asia/Tokyo (GMT+9)', 'Asia/Tokyo', 1, 4, 'N', 1, '亚洲东京时区', 'system', 'system'),
(5, 'timezone', 'Asia/Seoul (GMT+9)', 'Asia/Seoul', 1, 5, 'N', 1, '亚洲首尔时区', 'system', 'system');

-- 意图编码
INSERT IGNORE INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`, `create_by`, `update_by`)
VALUES (6, 'INTENT_CODE', '闲聊', 'CHITCHAT', 1, 1, 1, '意图编码：闲聊', 'system', 'system'),
(7, 'INTENT_CODE', '问答', 'QUESTION', 1, 2, 1, '意图编码：问答', 'system', 'system'),
(8, 'INTENT_CODE', '翻译', 'TRANSLATION', 1, 3, 1, '意图编码：翻译', 'system', 'system'),
(9, 'INTENT_CODE', '代码帮助', 'CODE_HELP', 1, 4, 1, '意图编码：代码帮助', 'system', 'system'),
(10, 'INTENT_CODE', '写作辅助', 'WRITING_ASSISTANCE', 1, 5, 1, '意图编码：写作辅助', 'system', 'system'),
(11, 'INTENT_CODE', '教育', 'EDUCATION', 1, 6, 1, '意图编码：教育', 'system', 'system'),
(12, 'INTENT_CODE', '数据分析', 'DATA_ANALYSIS', 1, 7, 1, '意图编码：数据分析', 'system', 'system'),
(13, 'INTENT_CODE', '未知', 'UNKNOWN', 1, 99, 1, '意图编码：未知', 'system', 'system');

-- 意图分类
INSERT IGNORE INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`, `create_by`, `update_by`)
VALUES (14, 'INTENT_CATEGORY', '会话', 'CONVERSATION', 1, 1, 1, '意图分类：会话', 'system', 'system'),
(15, 'INTENT_CATEGORY', '知识问答', 'KNOWLEDGE', 1, 2, 1, '意图分类：知识问答', 'system', 'system'),
(16, 'INTENT_CATEGORY', '任务', 'TASK', 1, 3, 1, '意图分类：任务', 'system', 'system'),
(17, 'INTENT_CATEGORY', '搜索', 'SEARCH', 1, 4, 1, '意图分类：搜索', 'system', 'system'),
(18, 'INTENT_CATEGORY', '技术支持', 'TECHNICAL', 1, 5, 1, '意图分类：技术支持', 'system', 'system');
