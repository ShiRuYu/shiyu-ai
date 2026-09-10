-- PostgreSQL baseline generated from the validated H2 resources.
-- Do not edit generated sections; update the source baseline and regenerate.

-- Source: modules/infrastructure/shiyu-common-core/src/main/resources/db/baseline/h2/seed/common/01_common.sql
-- Final system-ai seed baseline. Executed once on an empty H2 database.

INSERT INTO common_dict VALUES(1, 'timezone', 'America/New_York (GMT-5)', 'America/New_York', 1, 1, NULL, NULL, 'N', 1, '美国纽约时区', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(2, 'timezone', 'Europe/London (GMT0)', 'Europe/London', 1, 2, NULL, NULL, 'N', 1, '欧洲伦敦时区', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(3, 'timezone', 'Asia/Shanghai (GMT+8)', 'Asia/Shanghai', 1, 3, NULL, NULL, 'Y', 1, '亚洲上海时区', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(4, 'timezone', 'Asia/Tokyo (GMT+9)', 'Asia/Tokyo', 1, 4, NULL, NULL, 'N', 1, '亚洲东京时区', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(5, 'timezone', 'Asia/Seoul (GMT+9)', 'Asia/Seoul', 1, 5, NULL, NULL, 'N', 1, '亚洲首尔时区', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(6, 'INTENT_CODE', '闲聊', 'CHITCHAT', 1, 1, NULL, NULL, 'N', 1, '意图编码：闲聊', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(7, 'INTENT_CODE', '问答', 'QUESTION', 1, 2, NULL, NULL, 'N', 1, '意图编码：问答', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(8, 'INTENT_CODE', '翻译', 'TRANSLATION', 1, 3, NULL, NULL, 'N', 1, '意图编码：翻译', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(9, 'INTENT_CODE', '代码帮助', 'CODE_HELP', 1, 4, NULL, NULL, 'N', 1, '意图编码：代码帮助', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(10, 'INTENT_CODE', '写作辅助', 'WRITING_ASSISTANCE', 1, 5, NULL, NULL, 'N', 1, '意图编码：写作辅助', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(11, 'INTENT_CODE', '教育', 'EDUCATION', 1, 6, NULL, NULL, 'N', 1, '意图编码：教育', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(12, 'INTENT_CODE', '数据分析', 'DATA_ANALYSIS', 1, 7, NULL, NULL, 'N', 1, '意图编码：数据分析', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(13, 'INTENT_CODE', '未知', 'UNKNOWN', 1, 99, NULL, NULL, 'N', 1, '意图编码：未知', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(14, 'INTENT_CATEGORY', '会话', 'CONVERSATION', 1, 1, NULL, NULL, 'N', 1, '意图分类：会话', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(15, 'INTENT_CATEGORY', '知识问答', 'KNOWLEDGE', 1, 2, NULL, NULL, 'N', 1, '意图分类：知识问答', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(16, 'INTENT_CATEGORY', '任务', 'TASK', 1, 3, NULL, NULL, 'N', 1, '意图分类：任务', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(17, 'INTENT_CATEGORY', '搜索', 'SEARCH', 1, 4, NULL, NULL, 'N', 1, '意图分类：搜索', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO common_dict VALUES(18, 'INTENT_CATEGORY', '技术支持', 'TECHNICAL', 1, 5, NULL, NULL, 'N', 1, '意图分类：技术支持', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Source: modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2/seed/iam/02_auth.sql
-- Final system-ai seed baseline. Executed once on an empty H2 database.

INSERT INTO auth_tenant VALUES(1, NULL, 'default', '默认租户', 'Admin', '13800000000', NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_user VALUES(2, 'admin', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'Admin', NULL, NULL, NULL, 'admin@example.com', NULL, NULL, NULL);
INSERT INTO auth_role VALUES(1, 'super', '超级管理员', 1, 1, '拥有系统所有权限', 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role VALUES(2, 'admin', '管理员', 1, 1, '系统管理员', 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role VALUES(3, 'user', '普通用户', 1, 1, '普通用户', 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(1, '系统管理', 'SystemSettings', 'CATALOG', NULL, 1, '/system', '/system/user', 'lucide:settings', '', NULL, NULL, NULL, '用户、权限、租户与平台基础设施', TRUE, 1, 50, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(2, '用户管理', 'SystemUser', 'MENU', 1, 1, '/system/user', NULL, 'lucide:user', 'feature:iam.users', NULL, NULL, NULL, NULL, TRUE, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(3, '角色管理', 'SystemRole', 'MENU', 1, 1, '/system/role', NULL, 'lucide:shield', 'feature:iam.roles', NULL, NULL, NULL, NULL, TRUE, 1, 2, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(4, '菜单管理', 'SystemMenu', 'MENU', 1, 1, '/system/menu', NULL, 'lucide:menu', 'feature:iam.menus', NULL, NULL, NULL, NULL, TRUE, 1, 3, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(5, '租户管理', 'SystemTenant', 'MENU', 1, 1, '/system/tenant', NULL, 'lucide:building-2', 'feature:iam.tenants', NULL, NULL, NULL, NULL, TRUE, 1, 4, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(7, '字典管理', 'SystemDict', 'MENU', 1, 1, '/system/dict', NULL, 'lucide:book-type', 'feature:iam.dictionaries', NULL, NULL, NULL, NULL, TRUE, 1, 5, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(11, '权限码管理', 'SystemAuthCode', 'MENU', 1, 1, '/system/auth-code', NULL, 'lucide:shield-check', 'feature:iam.auth-codes', NULL, NULL, NULL, NULL, TRUE, 1, 6, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_menu VALUES(90, '文件管理', 'FileManager', 'MENU', 1, 1, '/file', NULL, 'carbon:folder', 'feature:iam.files', NULL, NULL, NULL, '平台文件与对象存储管理', TRUE, 1, 7, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_user_scope_role VALUES(2, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 5, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 7, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 11, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(1, 90, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 5, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 7, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 11, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_menu VALUES(2, 90, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(1, 'system:user:list', '查看用户列表', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(2, 'system:user:create', '创建用户', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(3, 'system:user:update', '更新用户', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(4, 'system:user:delete', '删除用户', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(5, 'system:role:list', '查看角色列表', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(6, 'system:role:create', '创建角色', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(7, 'system:role:update', '更新角色', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(8, 'system:role:delete', '删除角色', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(9, 'system:menu:list', '查看菜单列表', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(10, 'system:menu:create', '创建菜单', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(11, 'system:menu:update', '更新菜单', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(12, 'system:menu:delete', '删除菜单', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(13, 'system:tenant:list', '查看租户列表', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(14, 'system:tenant:create', '创建租户', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(15, 'system:tenant:update', '更新租户', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(16, 'system:tenant:delete', '删除租户', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(17, 'system:auth-code:list', '查看权限码列表', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(18, 'system:auth-code:create', '创建权限码', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(19, 'system:auth-code:update', '更新权限码', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(20, 'system:auth-code:delete', '删除权限码', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(21, 'system:dict:list', '查看字典', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(22, 'system:dict:create', '创建字典', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(23, 'system:dict:update', '更新字典', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(24, 'system:dict:delete', '删除字典', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(25, 'agent:admin:list', '查看 Agent 列表', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(26, 'agent:admin:create', '创建 Agent', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(27, 'agent:admin:edit', '编辑 Agent', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(28, 'agent:admin:delete', '删除 Agent', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(29, 'agent:platform:list', '查看平台', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(30, 'agent:platform:create', '创建平台', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(31, 'agent:platform:edit', '编辑平台', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(32, 'agent:platform:delete', '删除平台', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(33, 'agent:platform:set-default', '设置默认平台', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(34, 'agent:model:list', '查看模型', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(35, 'agent:model:create', '创建模型', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(36, 'agent:model:edit', '编辑模型', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(37, 'agent:model:delete', '删除模型', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(38, 'agent:model:set-default', '设置默认模型', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(39, 'agent:chat:config', '对话调试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(40, 'agent:intent:list', '查看意图', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(41, 'agent:intent:create', '创建意图', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(42, 'agent:intent:delete', '删除意图', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(43, 'knowledge:list', '查看知识点', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(44, 'knowledge:create', '创建知识点', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(45, 'knowledge:edit', '编辑知识点', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(46, 'knowledge:delete', '删除知识点', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(47, 'knowledge:graph', '查看知识图谱', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(48, 'knowledge:document:list', '查看文档', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(49, 'knowledge:document:upload', '上传文档', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(50, 'knowledge:document:delete', '删除文档', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(51, 'knowledge:index:rebuild', '重建索引', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(52, 'knowledge:relation', '管理知识关系', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(53, 'edu:subject:list', '查看学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(54, 'edu:subject:create', '创建学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(55, 'edu:subject:edit', '编辑学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(56, 'edu:subject:delete', '删除学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(57, 'edu:textbook:list', '查看教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(58, 'edu:textbook:create', '创建教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(59, 'edu:textbook:edit', '编辑教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(60, 'edu:textbook:delete', '删除教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(61, 'edu:chapter:list', '查看章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(62, 'edu:chapter:create', '创建章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(63, 'edu:chapter:edit', '编辑章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(64, 'edu:chapter:delete', '删除章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(65, 'edu:course:list', '查看课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(66, 'edu:course:create', '创建课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(67, 'edu:course:edit', '编辑课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(68, 'edu:course:delete', '删除课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(69, 'edu:question:list', '查看题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(70, 'edu:question:create', '创建题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(71, 'edu:question:edit', '编辑题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(72, 'edu:question:delete', '删除题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(73, 'edu:exam:list', '查看考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(74, 'edu:exam:create', '创建考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(75, 'edu:exam:edit', '编辑考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(76, 'edu:exam:delete', '删除考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(77, 'edu:exam:publish', '发布考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(78, 'edu:student:list', '查看学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(211, 'edu:student:create', '创建学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(212, 'edu:student:update', '编辑学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(213, 'edu:student:delete', '删除学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(79, 'edu:resource:list', '查看资源', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(80, 'edu:resource:upload', '上传资源', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(81, 'edu:resource:delete', '删除资源', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(82, 'edu:plan:list', '查看学习计划', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(83, 'edu:review:list', '查看复习任务', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(84, 'edu:analytics', '查看学情分析', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(85, 'edu:wrong-question', '查看错题管理', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(96, 'file:upload', '上传文件', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(97, 'file:delete', '删除文件', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(98, 'plugin:list', '查看插件列表', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(99, 'plugin:start', '启动插件', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(100, 'plugin:stop', '停止插件', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(101, 'plugin:uninstall', '卸载插件', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(102, 'plugin:scan', '扫描插件', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(103, 'tool:mcp:list', '查看 MCP 工具', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(104, 'tool:mcp:detail', '查看 MCP 工具详情', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(105, 'tool:mcp:execute', '执行 MCP 工具', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(106, 'tool:mcp:categories', '查看 MCP 工具分类', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(107, 'tool:mcp:stats', '查看 MCP 工具统计', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(108, 'usage:overview', '查看用量概览', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(109, 'usage:daily', '查看日用量', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(110, 'usage:weekly', '查看周用量', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(111, 'usage:monthly', '查看月用量', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(112, 'usage:model', '按模型查看用量', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(113, 'usage:llm', '查看 LLM 用量', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(114, 'usage:embedding', '查看 Embedding 用量', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(115, 'system:role:assign', '分配角色权限', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(116, 'system:user:password', '重置用户密码', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(117, 'agent:execute', '执行 Agent', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_auth_code VALUES(118, 'file:list', '查看文件列表和配置', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 1, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 2, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 3, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 4, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 5, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 5, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 6, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 6, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 7, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 7, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 8, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 8, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 9, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 9, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 10, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 10, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 11, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 11, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 12, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 12, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 13, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 13, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 14, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 14, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 15, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 15, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 16, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 16, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 17, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 17, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 18, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 18, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 19, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 19, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 20, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 20, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 21, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 21, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 22, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 22, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 23, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 23, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 24, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 24, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 25, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 25, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 26, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 26, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 27, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 27, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 28, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 28, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 29, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 29, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 30, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 30, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 31, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 31, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 32, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 32, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 33, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 33, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 34, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 34, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 35, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 35, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 36, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 36, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 37, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 37, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 38, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 38, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 39, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 39, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 40, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 40, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 41, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 41, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 42, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 42, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 43, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 43, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 44, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 44, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 45, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 45, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 46, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 46, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 47, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 47, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 48, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 48, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 49, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 49, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 50, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 50, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 51, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 51, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 52, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 52, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 53, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 53, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 54, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 54, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 55, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 55, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 56, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 56, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 57, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 57, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 58, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 58, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 59, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 59, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 60, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 60, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 61, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 61, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 62, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 62, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 63, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 63, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 64, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 64, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 65, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 65, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 66, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 66, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 67, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 67, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 68, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 68, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 69, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 69, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 70, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 70, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 71, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 71, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 72, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 72, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 73, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 73, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 74, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 74, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 75, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 75, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 76, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 76, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 77, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 77, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 78, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 78, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 79, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 79, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 80, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 80, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 81, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 81, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 82, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 82, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 83, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 83, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 84, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 84, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 85, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 85, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 96, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 96, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 97, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 97, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 98, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 98, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 99, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 99, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 100, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 100, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 101, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 101, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 102, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 102, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 103, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 103, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 104, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 104, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 105, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 105, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 106, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 106, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 107, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 107, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 108, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 108, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 109, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 109, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 110, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 110, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 111, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 111, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 112, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 112, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 113, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 113, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 114, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 114, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 115, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 115, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 116, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 116, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 117, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 117, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 118, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 118, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 211, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 211, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 212, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 212, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(1, 213, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_role_scope_auth_code VALUES(2, 213, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_menu VALUES(1, 90, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 12, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 13, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 14, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 15, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 16, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 17, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 18, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 19, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 20, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 21, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 22, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 23, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 24, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 25, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 26, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 27, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 28, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 29, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 30, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 31, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 32, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 33, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 34, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 35, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 36, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 37, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 38, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 39, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 40, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 41, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 42, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 43, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 44, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 45, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 46, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 47, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 48, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 49, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 50, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 51, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 52, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 53, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 54, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 55, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 56, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 57, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 58, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 59, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 60, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 61, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 62, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 63, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 64, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 65, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 66, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 67, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 68, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 69, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 70, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 71, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 72, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 73, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 74, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 75, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 76, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 77, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 78, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 79, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 80, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 81, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 82, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 83, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 84, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 85, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 96, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 97, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 98, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 99, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 100, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 101, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 102, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 103, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 104, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 105, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 106, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 107, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 108, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 109, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 110, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 111, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 112, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 113, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 114, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 115, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 116, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 117, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 118, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 211, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 212, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO auth_tenant_auth_code VALUES(1, 213, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Source: modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/baseline/h2/seed/agent/03_agent.sql
-- Final system-ai seed baseline. Executed once on an empty H2 database.

INSERT INTO agent_def VALUES(1, 'tutor-bot', '辅导助手', '通用学科辅导 Agent（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(2, 'knowledge-tutor', '知识问答助手', '知识问答 Agent（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(3, 'gcx-assistant', 'GCX 助手', 'GCX 智能助手（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(4, 'simple-assistant', '简单助手', '基础 LLM 问答助手（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(5, 'rag-knowledge-agent', '知识库问答', '基于文档知识库的 RAG 检索问答（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(6, 'smart-agent', '智能路由助手', '意图识别 + RAG + 工具调用全功能智能助手（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(7, 'practice', 'AI出题助手', '根据知识点和难度智能生成练习题（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(8, 'teacher', 'AI讲解助手', '根据知识点进行智能教学讲解（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(9, 'planner', '学习规划助手', '根据知识点生成个性化学习规划（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(10, 'report', '学习报告助手', '基于学习数据生成学习分析报告（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_def VALUES(11, 'exam', 'AI组卷助手', '根据知识点和考试要求智能组卷（使用硅基流动 Qwen3-8B 模型）', 1, NULL, 'v1.0.0', 1, NULL, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(1, 'tutor-bot', 'v1.0.0', 1, '初始版本', 1, '{name:"tutor-graph",description:"辅导助手工作流程",startnode:intent,endnode:output,nodes:{intent:{nodename:"意图识别",nodetype:intent,enabled:true,config:{category:education}},knowledge:{nodename:"知识检索",nodetype:rag_retrieval,enabled:true,config:{spaceids:[1],sourcetypes:[knowledge_entry],retrievalmode:hybrid,topk:5,scorethreshold:0}},teach:{nodename:"教学讲解",nodetype:education_teach,enabled:true,config:{}},practice:{nodename:"出题练习",nodetype:education_practice,enabled:true,config:{}},review:{nodename:"复习安排",nodetype:review_schedule,enabled:true,config:{}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:markdown}}},edges:{intent:[knowledge],knowledge:[teach],teach:[practice],practice:[review],review:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{intent:{x:100,y:100},knowledge:{x:300,y:100},teach:{x:500,y:100},practice:{x:700,y:100},review:{x:900,y:100},output:{x:1100,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(2, 'knowledge-tutor', 'v1.0.0', 1, '初始版本', 1, '{name:"knowledge-qa-graph",description:"知识问答工作流程",startnode:intent,endnode:output,nodes:{intent:{nodename:"意图识别",nodetype:intent,enabled:true,config:{category:knowledge}},retrieval:{nodename:"知识检索",nodetype:rag_retrieval,enabled:true,config:{spaceids:[1],sourcetypes:[document,knowledge_entry],retrievalmode:hybrid,topk:5,scorethreshold:0}},enhancement:{nodename:"知识增强",nodetype:rag_enhancement,enabled:true,config:{enhancementstrategy:summarization}},llm:{nodename:"LLM生成",nodetype:llm_call,enabled:true,config:{platform:deepseek,modelname:"deepseek-v4-flash",temperature:0.7}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:markdown}}},edges:{intent:[retrieval],retrieval:[enhancement],enhancement:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{intent:{x:100,y:100},retrieval:{x:300,y:100},enhancement:{x:500,y:100},llm:{x:700,y:100},output:{x:900,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(3, 'gcx-assistant', 'v1.0.0', 1, '初始版本', 1, '{name:"gcx-graph",description:"GCX助手工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},llm:{nodename:"GCX回答",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B"}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},llm:{x:300,y:100},output:{x:500,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(4, 'simple-assistant', 'v1.0.0', 1, '初始版本', 1, '{name:"simple-graph",description:"简单助手工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},llm:{nodename:"LLM回答",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",defaultprompt:"你是一个智能助手，请友好地回答用户的问题。"}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},llm:{x:300,y:100},output:{x:500,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(5, 'rag-knowledge-agent', 'v1.0.0', 1, '初始版本', 1, '{name:"rag-qa-graph",description:"知识库问答工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},rag_retrieval:{nodename:"知识库检索",nodetype:rag_retrieval,enabled:true,config:{spaceids:[1],sourcetypes:[document,knowledge_entry],retrievalmode:hybrid,topk:5,scorethreshold:0}},rag_enhance:{nodename:"检索增强",nodetype:rag_enhancement,enabled:true,config:{enhancementstrategy:summarization,contextwindowsize:3,addcontext:true}},llm:{nodename:"LLM回答",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",prompttemplate:"基于以下检索到的文档内容回答用户问题。\n\n{context}\n\n用户问题: {query}"}},output:{nodename:"格式化输出",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[rag_retrieval],rag_retrieval:[rag_enhance],rag_enhance:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},rag_retrieval:{x:300,y:100},rag_enhance:{x:500,y:100},llm:{x:700,y:100},output:{x:900,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(6, 'smart-agent', 'v1.0.0', 1, '初始版本', 1, '{name:"smart-graph",description:"智能路由助手工作流程",startnode:intent,endnode:output,nodes:{intent:{nodename:"意图识别",nodetype:intent,enabled:true,config:{category:general}},llm_chat:{nodename:"闲聊回答",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",defaultprompt:"你是一个友好的 AI 助手，请用轻松自然的语气和用户聊天。"}},rag_retrieval:{nodename:"知识库检索",nodetype:rag_retrieval,enabled:true,config:{topk:3}},rag_enhance:{nodename:"检索增强",nodetype:rag_enhancement,enabled:true,config:{enhancementstrategy:summarization,contextwindowsize:3}},rag_llm:{nodename:"RAG回答",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",prompttemplate:"基于以下检索到的文档回答用户问题。\n\n{context}\n\n用户问题: {query}"}},tool_call_weather:{nodename:"天气查询工具",nodetype:tool_call,enabled:true,config:{toolname:weather,enablecache:true}},tool_call_calculator:{nodename:"计算器工具",nodetype:tool_call,enabled:true,config:{toolname:calculator,enablecache:true}},tool_llm:{nodename:"工具结果回答",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",prompttemplate:"以下是工具执行结果，请用自然语言回复用户。\n\n工具结果: {toolResult}\n\n用户问题: {query}"}},output:{nodename:"格式化输出",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{rag_retrieval:[rag_enhance],rag_enhance:[rag_llm],rag_llm:[output],llm_chat:[output],tool_call_weather:[tool_llm],tool_call_calculator:[tool_llm],tool_llm:[output]},conditionaledges:{intent:{conditiontype:intent,defaulttarget:llm_chat,nodemappings:{chitchat:llm_chat,question:rag_retrieval,calculator:tool_call_calculator,weather:tool_call_weather}}}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{intent:{x:100,y:100},llm_chat:{x:300,y:50},rag_retrieval:{x:300,y:150},rag_enhance:{x:500,y:150},rag_llm:{x:700,y:150},tool_call_weather:{x:300,y:250},tool_call_calculator:{x:300,y:350},tool_llm:{x:500,y:300},output:{x:900,y:200}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(7, 'practice', 'v1.0.0', 1, '初始版本', 1, '{name:"practice-graph",description:"AI出题工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},llm:{nodename:"AI出题",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",defaultprompt:"你是一位经验丰富的 K12 出题教师，请根据知识点生成练习题。",prompttemplate:"你是一位经验丰富的 K12 出题教师。\n\n## 出题参数\n- 知识点ID: {knowledgeId}\n- 难度级别（1-4）: {difficulty}\n- 题目数量: {count}\n- 学生ID: {studentId}\n\n请生成 {count} 道难度为 {difficulty} 级的练习题。\n- 题目类型：选择题（60%）和填空题（40%）\n- 每行输出一个 JSON：{\"type\":\"CHOICE\",\"title\":\"题干\",\"options\":[\"A.\",\"B.\",\"C.\",\"D.\"],\"answer\":\"A\",\"analysis\":\"解析\",\"ability_dimension\":\"apply\"}\n- 仅输出 JSON 数据，用中文出题。\n"}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},llm:{x:300,y:100},output:{x:500,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(8, 'teacher', 'v1.0.0', 1, '初始版本', 1, '{name:"teacher-graph",description:"AI教学讲解工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},llm:{nodename:"教学讲解",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",defaultprompt:"你是一位耐心的 K12 学科教师，请根据知识点进行详细讲解。",prompttemplate:"你是一位耐心的 K12 学科教师。\n\n## 教学参数\n- 知识点ID: {knowledgeId}\n- 学生ID: {studentId}\n- 讲解风格: {style}\n\n请根据上述知识点进行详细、通俗易懂的讲解，包含概念解释、典型例题和易错点提醒。"}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},llm:{x:300,y:100},output:{x:500,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(9, 'planner', 'v1.0.0', 1, '初始版本', 1, '{name:"planner-graph",description:"学习规划工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},llm:{nodename:"学习规划",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",defaultprompt:"请根据知识点和学习目标生成个性化学习规划。",prompttemplate:"请根据以下信息生成个性化学习规划。\n\n## 规划参数\n- 知识点ID: {knowledgeId}\n- 学生ID: {studentId}\n- 目标日期: {targetDate}\n\n请制定从今天到目标日期的学习计划，每天的学习内容、练习安排和复习计划。"}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},llm:{x:300,y:100},output:{x:500,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(10, 'report', 'v1.0.0', 1, '初始版本', 1, '{name:"report-graph",description:"学习报告工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},llm:{nodename:"报告生成",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",defaultprompt:"请根据学习数据生成综合学习分析报告。",prompttemplate:"请根据以下学习数据生成学习分析报告。\n\n## 报告参数\n- 学生ID: {studentId}\n- 报告周期: {period}\n\n请生成包含以下内容的学习报告：\n1. 学习概况总结\n2. 各知识点掌握度分析\n3. 薄弱环节识别\n4. 针对性提升建议"}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},llm:{x:300,y:100},output:{x:500,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_version VALUES(11, 'exam', 'v1.0.0', 1, '初始版本', 1, '{name:"exam-graph",description:"AI组卷工作流程",startnode:input,endnode:output,nodes:{input:{nodename:"输入节点",nodetype:default,enabled:true},llm:{nodename:"AI组卷",nodetype:llm_call,enabled:true,config:{platform:silicon_flow,modelname:"Qwen/Qwen3-8B",defaultprompt:"请根据知识点和考试要求生成一份完整的试卷。",prompttemplate:"请根据以下参数生成一份完整的试卷。\n\n## 组卷参数\n- 知识点ID: {knowledgeId}\n- 学生ID: {studentId}\n- 难度级别: {difficulty}\n- 题目数量: {count}\n- 考试时长: {duration} 分钟\n\n请生成 {count} 道题目，包含选择题、填空题和解答题。"}},output:{nodename:"输出格式化",nodetype:output_format,enabled:true,config:{outputformat:text,prettyprint:true}}},edges:{input:[llm],llm:[output]},conditionaledges:{}}', '{zoom:1,offsetx:0,offsety:0,nodepositions:{input:{x:100,y:100},llm:{x:300,y:100},output:{x:500,y:100}}}', NULL, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(1, 'default', 'CHITCHAT', '闲聊', 1, '日常闲聊对话', 'CONVERSATION', 10, 0.75, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(2, 'default', 'QUESTION', '问答', 1, '知识问答', 'KNOWLEDGE', 50, 0.8, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(3, 'default', 'TRANSLATION', '翻译', 1, '语言翻译', 'TASK', 60, 0.85, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(4, 'default', 'EDUCATION', '教育', 1, '学科教育辅导', 'TASK', 70, 0.8, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(5, 'default', 'CODE_HELP', '代码帮助', 1, '编程辅助与代码生成', 'TASK', 65, 0.8, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(6, 'default', 'WRITING_ASSISTANCE', '写作辅助', 1, '文章写作与润色', 'TASK', 55, 0.75, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(7, 'default', 'DATA_ANALYSIS', '数据分析', 1, '数据处理与分析', 'TASK', 60, 0.8, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO agent_intent_def VALUES(8, 'default', 'UNKNOWN', '未知意图', 1, '无法识别的意图', 'CONVERSATION', 0, 0.5, NULL, NULL, '0', NULL, NULL, NULL, '1', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Source: modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/baseline/h2/seed/agent/04_app_runtime.sql
-- A publishable education application assembled from a module, platform and agent.
INSERT INTO ai_app (id, tenant_id, owner_user_id, name, description, status, published_version_id, created_at, updated_at)
VALUES ('edu-tutor-app', 1, 1, '数学学习助手', '面向七年级一元一次方程学习的教育应用示例。', 'PUBLISHED', 'edu-tutor-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ai_app_version (id, app_id, tenant_id, version, config_json, status, created_at, published_at)
VALUES ('edu-tutor-v1', 'edu-tutor-app', 1, '1.0.0',
        '{module:education,platform:deepseek,model:"deepseek-v4-flash",agentid:"tutor-bot",agentversion:"v1.0.0",knowledgespaceids:[1],resourcetypes:[video,pdf],executiontype:agent}',
        'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ai_app (id, tenant_id, owner_user_id, name, description, status, published_version_id, created_at, updated_at)
VALUES ('knowledge-qa-app', 1, 1, '知识库问答助手', '面向课程资料检索与引用回答的知识库应用示例。', 'PUBLISHED', 'knowledge-qa-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ai_app_version (id, app_id, tenant_id, version, config_json, status, created_at, published_at)
VALUES ('knowledge-qa-v1', 'knowledge-qa-app', 1, '1.0.0',
        '{module:knowledge,platform:deepseek,model:"deepseek-v4-flash",agentid:"rag-knowledge-agent",agentversion:"v1.0.0",knowledgespaceids:[1],retrievalmode:hybrid,topk:5,executiontype:agent}',
        'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Source: modules/domains/model/shiyu-model-implementation/src/main/resources/db/baseline/h2/seed/model/03_model.sql
-- Final system-ai seed baseline. Executed once on an empty H2 database.

INSERT INTO model_ai_platform VALUES(1, 'OpenAI', 'OPENAI', 'OPENAI_COMPATIBLE', 1, 'https://api.openai.com/v1', '', 0.7, 4096, 3, '["gpt-4o","gpt-4o-mini","gpt-4-turbo","gpt-3.5-turbo"]', NULL, 'N', 1, 'OpenAI 官方 API', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_platform VALUES(2, 'DeepSeek', 'DEEPSEEK', 'OPENAI_COMPATIBLE', 1, 'https://api.deepseek.com', '', 0.7, 4096, 3, '["deepseek-v4-flash","deepseek-reasoner"]', NULL, 'Y', 1, 'DeepSeek 官方 API', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_platform VALUES(3, 'OpenRouter', 'OPENROUTER', 'OPENAI_COMPATIBLE', 1, 'https://openrouter.ai/api', '', 0.7, 4096, 3, '["x-ai/grok-4.1-fast","anthropic/claude-3.5-sonnet","google/gemini-2.5-pro"]', NULL, 'N', 1, 'OpenRouter 聚合 API', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_platform VALUES(4, '硅基流动（通义千问）', 'SILICON_FLOW', 'OPENAI_COMPATIBLE', 1, 'https://api.siliconflow.cn', '', 0.7, 4096, 3, '["Qwen/Qwen3-14B","Qwen/Qwen3-8B"]', NULL, 'N', 1, '硅基流动（通义千问）平台 - 通义千问 Qwen 模型服务', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(1, 1, 'gpt-4o', 1, 'GPT-4o', NULL, NULL, 'N', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(2, 1, 'gpt-4o-mini', 1, 'GPT-4o Mini', NULL, NULL, 'Y', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(3, 1, 'gpt-3.5-turbo', 1, 'GPT-3.5 Turbo', NULL, NULL, 'N', 1, 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(4, 2, 'deepseek-v4-flash', 1, 'DeepSeek V4 Flash', NULL, NULL, 'Y', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(5, 2, 'deepseek-reasoner', 1, 'DeepSeek Reasoner', NULL, NULL, 'N', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(6, 3, 'x-ai/grok-4.1-fast', 1, 'Grok 4.1 Fast', NULL, NULL, 'N', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(7, 3, 'anthropic/claude-3.5-sonnet', 1, 'Claude 3.5 Sonnet', NULL, NULL, 'Y', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(8, 4, 'Qwen/Qwen3-14B', 1, 'Qwen3 14B', NULL, NULL, 'N', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO model_ai_model VALUES(9, 4, 'Qwen/Qwen3-8B', 1, 'Qwen3 8B', NULL, NULL, 'Y', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);

-- Source: modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2/seed/knowledge/04_knowledge.sql
-- Final knowledge seed baseline. Executed once on an empty H2 database.

INSERT INTO knowledge_difficulty_scale VALUES(1, 'STANDARD_5', '标准五级难度', '适用于通用知识体系的五级难度量表', 5, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO knowledge_difficulty_scale_level VALUES(1, 1, 1, '入门', '基础知识与概念理解', 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO knowledge_difficulty_scale_level VALUES(2, 1, 2, '基础', '基础应用与简单推理', 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO knowledge_difficulty_scale_level VALUES(3, 1, 3, '中等', '综合运用与中等推理', 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO knowledge_difficulty_scale_level VALUES(4, 1, 4, '较难', '复杂问题分析与推理', 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO knowledge_difficulty_scale_level VALUES(5, 1, 5, '挑战', '高难度综合与创新', 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Source: modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/baseline/h2/seed/knowledge/06_demo_content.sql
-- Realistic knowledge-base demo content for the default tenant.
INSERT INTO knowledge_space
    (id, code, name, description, access_mode, review_mode, embedding_profile, rerank_profile, chunk_strategy, chunk_size, chunk_overlap, active_index_version, tenant_id, status, del_flag, create_by, update_by, difficulty_scale_id, binding_mode, domain_code)
VALUES (1, 'DEMO_EDU_MATH', '七年级数学知识库', '用于数学辅导问答的课程资料与例题知识库', 'PRIVATE', 'OPTIONAL', 'text-embedding-v3', 'default', 'HEADING', 800, 100, 1, 1, 1, 0, 'system', 'system', 1, 'OPTIONAL', 'EDUCATION');

INSERT INTO knowledge_base
    (id, code, name, description, difficulty, category, tags, tenant_id, status, del_flag, create_by, update_by, space_id, difficulty_level)
VALUES (1001, 'MATH_LINEAR_EQUATION', '一元一次方程', '含有一个未知数且未知数次数为一的整式方程', 2, 'math', '["代数","方程","七年级"]', 1, 1, 0, 'system', 'system', 1, 2);

INSERT INTO knowledge_document
    (id, title, content, doc_type, source, author, tenant_id, status, del_flag, create_by, update_by, space_id, current_version_id, lifecycle_status, parse_status, storage_provider, file_size, mime_type)
VALUES (1001, '一元一次方程学习讲义', '一元一次方程的基本步骤：去括号、移项、合并同类项、系数化为一。', 'LECTURE', '七年级数学课程组', '拾羽实验学校数学组', 1, 1, 0, 'system', 'system', 1, 1001, 'PUBLISHED', 'READY', 'local', 86, 'text/plain');

INSERT INTO knowledge_document_version
    (id, document_id, space_id, version_no, title, content, storage_provider, file_size, mime_type, lifecycle_status, parse_status, model_profile, published_at, tenant_id, status, del_flag, create_by, update_by)
VALUES (1001, 1001, 1, 1, '一元一次方程学习讲义', '一元一次方程的基本步骤：去括号、移项、合并同类项、系数化为一。', 'local', 86, 'text/plain', 'PUBLISHED', 'READY', 'default', TIMESTAMP '2026-09-08 08:30:00', 1, 1, 0, 'system', 'system');

INSERT INTO knowledge_doc_relation
    (id, space_id, doc_id, knowledge_id, relation_type, tenant_id, create_by, status, del_flag)
VALUES (1001, 1, 1001, 1001, 'PRIMARY', 1, 'system', 1, 0);

INSERT INTO vector_knowledge_chunk
    (id, document_id, content, embedding, metadata, chunk_index, tenant_id, create_by, status, del_flag, space_id, version_id, embedding_model, embedding_dimension, page_number, section_path, token_count)
VALUES (1001, 1001, '解一元一次方程时，先化简方程两边，再把含未知数的项移到一边，常数项移到另一边，最后求出未知数。', NULL, '{subject:"数学",grade:7,source:"一元一次方程学习讲义"}', 0, 1, 'system', 1, 0, 1, 1001, 'text-embedding-v3', 1536, 1, '一、解方程步骤', 34);

INSERT INTO knowledge_ingestion_job
    (id, job_key, job_type, space_id, document_id, version_id, actor_user_id, job_status, stage, progress, attempts, max_attempts, checkpoint_data, finished_time, lock_version, tenant_id, status, del_flag, create_by, update_by)
VALUES (1001, 'demo-document-1001-v1', 'DOCUMENT_UPLOAD', 1, 1001, 1001, 2, 'SUCCEEDED', 'INDEXED', 100, 1, 3,
        '{parsed:true,chunkcount:1,embeddingmodel:"text-embedding-v3",vectorstored:true}',
        TIMESTAMP '2026-09-08 08:32:14', 1, 1, 1, 0, 'system', 'system');

INSERT INTO knowledge_audit_log
    (id, space_id, resource_type, resource_id, action, detail_json, tenant_id, status, del_flag, create_by, create_time, update_by, update_time)
VALUES (1001, 1, 'DOCUMENT', 1001, 'INGESTION_COMPLETED',
        '{jobkey:"demo-document-1001-v1",parsestatus:ready,vectorchunks:1,embeddingmodel:"text-embedding-v3"}',
        1, 1, 0, 'system', TIMESTAMP '2026-09-08 08:32:14', 'system', TIMESTAMP '2026-09-08 08:32:14');

INSERT INTO knowledge_evaluation_case
    (id, space_id, question, expected_doc_ids, expected_answer, tenant_id, status, del_flag, create_by, create_time, update_by, update_time)
VALUES (1001, 1, '一元一次方程移项时需要注意什么？', '[1001]',
        '移项后需要同时改变该项的符号，再合并同类项并求解。',
        1, 1, 0, 'system', TIMESTAMP '2026-09-08 08:35:00', 'system', TIMESTAMP '2026-09-08 08:35:00');

-- Source: modules/domains/iam/shiyu-iam-implementation/src/main/resources/db/baseline/h2/seed/iam/05_navigation.sql
-- Final v4 navigation seed. It is executed only against an empty database;
-- legacy menu cleanup belongs to the operator-controlled database rebuild.
INSERT INTO auth_menu (id,name,code,type,parent_id,tenant_id,path,redirect,icon,component,description,show,status,order,del_flag,create_by,create_time,update_by,update_time) VALUES
(2000,'工作台','Workbench','CATALOG',NULL,1,'/workbench','/workbench/overview','lucide:layout-dashboard','','待办、最近会话、审批与运行状态',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2010,'AI 控制台','AiConsole','CATALOG',NULL,1,'/workspace','/workspace/chat','lucide:sparkles','','Chat、Agent、RAG 与历史会话',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2020,'应用开发','AppStudio','CATALOG',NULL,1,'/app-studio','/app-studio/apps','lucide:blocks','','AI App、Agent、Prompt 与 Evaluation',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2030,'知识中心','KnowledgeCenter','CATALOG',NULL,1,'/knowledge-center','/knowledge-center/spaces','lucide:brain-circuit','','知识空间、检索与图谱洞察',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2040,'运行观测','Observability','CATALOG',NULL,1,'/observability','/observability/runs','lucide:activity','','Run、Trace、Usage 与工具审批',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2050,'教育中心','EducationCenter','CATALOG',NULL,1,'/education-center','/education-center/learning','lucide:graduation-cap','','学习、练习、复习与 AI 辅学',TRUE,1,6,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2070,'平台管理','PlatformAdmin','CATALOG',NULL,1,'/platform-admin','/platform-admin/models','lucide:shield-cog','','身份、模型、插件与运维',TRUE,1,8,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2011,'Chat 对话','ConversationChat','MENU',2010,1,'/workspace/chat',NULL,'lucide:message-circle','feature:conversation.chat','沉浸式聊天界面',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2012,'Agent 执行','AgentExecution','MENU',2010,1,'/workspace/agent',NULL,'lucide:bot','feature:agent.execution','运行已发布 Agent App',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2013,'RAG 检索','KnowledgeRetrieval','MENU',2010,1,'/workspace/rag',NULL,'lucide:search-check','feature:knowledge.retrieval','检索与引用中心',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2021,'AI App','AiAppStudio','MENU',2020,1,'/app-studio/apps',NULL,'lucide:app-window','feature:agent.apps','应用发布与版本',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2022,'Agent Studio','AgentStudio','MENU',2020,1,'/app-studio/agents',NULL,'lucide:bot','feature:agent.admin','Agent 图、版本与执行配置',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2023,'Prompt Studio','PromptStudio','MENU',2020,1,'/app-studio/prompts',NULL,'lucide:pen-line','feature:conversation.prompts','Prompt 版本、预览与变量校验',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2024,'评测中心','EvaluationStudio','MENU',2020,1,'/app-studio/evaluations',NULL,'lucide:chart-no-axes-combined','feature:knowledge.evaluations','数据集、运行与回归门槛',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2025,'意图路由','IntentRouter','MENU',2020,1,'/app-studio/intents',NULL,'lucide:route','feature:agent.intents','查询意图和路由策略',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2031,'知识空间','KnowledgeSpaces','MENU',2030,1,'/knowledge-center/spaces',NULL,'lucide:layers-3','feature:knowledge.spaces','空间、成员与权限',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2032,'文档中心','KnowledgeDocuments','MENU',2030,1,'/knowledge-center/documents',NULL,'lucide:file-stack','feature:knowledge.documents','文档、版本与索引状态',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2033,'检索实验室','KnowledgeSearch','MENU',2030,1,'/knowledge-center/search',NULL,'lucide:search-check','feature:knowledge.search','检索策略、引用与评分',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2034,'图谱洞察','KnowledgeGraph','MENU',2030,1,'/knowledge-center/graph',NULL,'lucide:network','feature:knowledge.graph','关系图谱与路径解释',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2035,'知识评测','KnowledgeEvaluations','MENU',2030,1,'/knowledge-center/evaluations',NULL,'lucide:chart-no-axes-combined','feature:knowledge.evaluations','知识检索评测',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2041,'运行记录','RunObservability','MENU',2040,1,'/observability/runs',NULL,'lucide:list-tree','feature:governance.observability','Run 与 Trace 时间线',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2042,'工具审批','ToolApprovals','MENU',2040,1,'/observability/approvals',NULL,'lucide:badge-check','feature:governance.approvals','高风险工具审批与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2071,'模型与 Provider','PlatformModels','MENU',2070,1,'/platform-admin/models',NULL,'lucide:cpu','feature:model.models','模型能力、路由与健康',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2072,'插件市场','PluginMarket','MENU',2070,1,'/platform-admin/plugins',NULL,'lucide:puzzle','feature:tooling.plugins','签名插件、权限与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2073,'配额与审计','PlatformQuotas','MENU',2070,1,'/platform-admin/quotas',NULL,'lucide:gauge','feature:governance.quotas','租户配额、用量与审计',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2074,'平台配置','PlatformProviders','MENU',2070,1,'/platform-admin/platforms',NULL,'lucide:server-cog','feature:model.platforms','平台连接、协议与密钥配置',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2051,'学习','EducationLearning','MENU',2050,1,'/education-center/learning',NULL,'lucide:book-open','feature:education.learning','课程与学习资源',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2052,'练习','EducationPractice','MENU',2050,1,'/education-center/practice',NULL,'lucide:clipboard-check','feature:education.practice','题库、错题与考试',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2053,'AI 辅学','EducationTutor','MENU',2050,1,'/education-center/ai-tutor',NULL,'lucide:sparkles','feature:education.tutor','讲解、出题、规划与报告',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2054,'学习分析','EducationAnalytics','MENU',2050,1,'/education-center/analytics',NULL,'lucide:chart-no-axes-combined','feature:education.analytics','学习报告与趋势',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP);

INSERT INTO auth_role_scope_menu (role_id,menu_id,tenant_id,status,del_flag,create_by,create_time,update_by,update_time)
SELECT R.id, M.id, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM auth_role R CROSS JOIN auth_menu M
WHERE R.id IN (1,2,3) AND M.id BETWEEN 2000 AND 2074;

INSERT INTO auth_tenant_menu (tenant_id,menu_id,status,create_time,update_time)
SELECT 1, M.id, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM auth_menu M WHERE M.id BETWEEN 2000 AND 2074;

-- Source: modules/domains/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/07_education.sql
-- Realistic education demo seed for the default tenant.
-- Scores are deliberately high (92-98) to exercise high-performing learner views.

INSERT INTO edu_subject
    (id, code, name, grade_level, icon, sort_order, status, tenant_id, del_flag, create_by, update_by)
VALUES (1, 'MATH', '数学', 'K2', 'calculator', 10, 1, 1, 0, 'system', 'system');

INSERT INTO edu_teacher
    (id, user_id, teacher_no, name, subject, school, title, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, 'T-2026-001', '王老师', '数学', '拾羽实验学校', '中学一级教师', 1, 1, 0, 'system', 'system');

INSERT INTO edu_student
    (id, user_id, student_no, name, gender, birth_date, grade, grade_level, school, class_name, learning_style, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 2, 'DEMO-2026-001', '示例学生', 1, DATE '2013-05-18', 7, 'K2', '拾羽实验学校', '七年级一班', 'visual', 1, 1, 0, 'system', 'system');

INSERT INTO edu_question
    (id, code, type, subject_code, grade, difficulty, ability_dimension, title, options, answer, analysis, source, tags, status, tenant_id, del_flag, create_by, update_by)
VALUES (1, 'MATH-7-ALG-001', 'CHOICE', 'MATH', 7, 2, 'apply',
        '若 2x + 5 = 17，则 x 的值为？', '["A. 5","B. 6","C. 7","D. 8"]', 'B',
        '移项得 2x = 12，因此 x = 6。', '七年级数学阶段测验', '["一元一次方程","基础运算"]', 1, 1, 0, 'system', 'system');

INSERT INTO edu_exam
    (id, name, type, subject_code, grade, duration_min, total_score, status, teacher_id, start_time, end_time, tenant_id, del_flag, create_by, update_by)
VALUES (1, '七年级数学阶段测验（示例）', 'UNIT_TEST', 'MATH', 7, 45, 100, 2, 1,
        TIMESTAMP '2026-09-01 09:00:00', TIMESTAMP '2026-09-01 09:45:00', 1, 1, 'system', 'system');

INSERT INTO edu_exam_section
    (id, exam_id, name, order_no, score_per_q, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, '一、选择题', 1, 100.00, 1, 1, 0, 'system', 'system');

INSERT INTO edu_exam_question
    (id, exam_id, section_id, question_id, order_no, score, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, 1, 1, 1, 95.00, 1, 1, 0, 'system', 'system');

INSERT INTO edu_study_record
    (id, student_id, knowledge_id, record_type, question_id, score, accuracy, duration_sec, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, 1001, 'PRACTICE', 1, 92.00, 0.92, 420, 1, 1, 0, 'system', 'system');

INSERT INTO edu_study_record
    (id, student_id, knowledge_id, record_type, question_id, score, accuracy, duration_sec, tenant_id, status, del_flag, create_by, update_by)
VALUES (2, 1, 1001, 'EXAM', 1, 95.00, 0.95, 2700, 1, 1, 0, 'system', 'system');

INSERT INTO edu_ability
    (id, student_id, knowledge_id, remember, understand, apply, analyze, evaluate, create_score, overall_mastery, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, 1001, 98.0, 96.0, 95.0, 93.0, 92.0, 90.0, 94.0, 1, 1, 0, 'system', 'system');

-- Source: modules/domains/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/08_learning_progress.sql
-- Realistic learning-progress seed for the sample student.
INSERT INTO edu_review_task
    (id, student_id, knowledge_id, review_date, review_round, status, result_score, completed_at, tenant_id, create_by, update_by, del_flag)
VALUES (1, 1, 1001, DATE '2026-09-10', 1, 0, NULL, NULL, 1, 'system', 'system', 0);

INSERT INTO edu_study_plan
    (id, student_id, target_knowledge_id, name, start_date, end_date, status, tenant_id, create_by, update_by, del_flag)
VALUES (1, 1, 1001, '一元一次方程巩固计划', DATE '2026-09-08', DATE '2026-09-14', 0, 1, 'system', 'system', 0);

INSERT INTO edu_study_plan_item
    (id, plan_id, knowledge_id, plan_date, order_no, status, completed_at, tenant_id, create_by, update_by, del_flag)
VALUES (1, 1, 1001, DATE '2026-09-09', 1, 2, TIMESTAMP '2026-09-09 19:30:00', 1, 'system', 'system', 0);

INSERT INTO edu_wrong_question
    (id, student_id, question_id, knowledge_id, student_answer, correct_times, created_at, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, 1, 1001, 'C', 0, TIMESTAMP '2026-09-08 18:20:00', 1, 1, 0, 'system', 'system');

INSERT INTO edu_learning_state
    (id, student_id, knowledge_id, state, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, 1001, 'PROFICIENT', 1, 1, 0, 'system', 'system');

INSERT INTO edu_achievement
    (id, student_id, code, name, description, icon, earned_at, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, 'ALGEBRA_FOUNDATION', '代数基础达标', '完成一元一次方程阶段测验并达到 90 分以上。', 'achievement-algebra', TIMESTAMP '2026-09-08 20:00:00', 1, 1, 0, 'system', 'system');

-- Source: modules/domains/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/09_curriculum.sql
-- Realistic curriculum structure for the sample mathematics learner.
INSERT INTO edu_textbook
    (id, name, subject_code, grade, publisher, isbn, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, '义务教育教科书 数学 七年级上册', 'MATH', 7, '人民教育出版社', '9787107335661', 1, 1, 0, 'system', 'system');

INSERT INTO edu_chapter
    (id, textbook_id, parent_id, name, chapter_order, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, NULL, '第三章 一元一次方程', 3, 1, 1, 0, 'system', 'system');

INSERT INTO edu_knowledge_textbook
    (id, knowledge_id, textbook_id, chapter_id, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1001, 1, 1, 1, 1, 0, 'system', 'system');

INSERT INTO edu_course
    (id, name, description, subject_code, grade, textbook_id, teacher_id, total_hours, status, view_count, tenant_id, del_flag, create_by, update_by)
VALUES (1, '七年级数学同步辅导', '围绕教材章节进行概念讲解、例题练习与错题复习。', 'MATH', 7, 1, 1, 16, 1, 128, 1, 0, 'system', 'system');

INSERT INTO edu_course_chapter
    (id, course_id, name, order_no, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, '一元一次方程', 1, 1, 1, 0, 'system', 'system');

INSERT INTO edu_course_section
    (id, chapter_id, name, order_no, content_url, video_url, duration_min, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1, '解方程的基本步骤', 1, '/education/courses/1/sections/1', NULL, 25, 1, 1, 0, 'system', 'system');

INSERT INTO edu_course_knowledge
    (course_id, knowledge_id, section_id, sort_order, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1001, 1, 1, 1, 1, 0, 'system', 'system');

-- Source: modules/domains/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/10_resources.sql
-- Learning resources connected to the curriculum and knowledge point.
INSERT INTO edu_resource
    (id, name, type, url, size_bytes, duration_sec, subject_code, grade, difficulty, description, status, view_count, tenant_id, del_flag, create_by, update_by)
VALUES (1, '一元一次方程例题讲解', 'VIDEO', '/education/resources/math-linear-equation-intro.mp4', 52428800, 780, 'MATH', 7, 2, '用生活化例子讲解列方程与解方程的完整步骤。', 1, 86, 1, 0, 'system', 'system');

INSERT INTO edu_resource_knowledge
    (resource_id, knowledge_id, sort_order, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1001, 1, 1, 1, 0, 'system', 'system');

INSERT INTO edu_question_knowledge
    (question_id, knowledge_id, weight, tenant_id, status, del_flag, create_by, update_by)
VALUES (1, 1001, 1.0, 1, 1, 0, 'system', 'system');

-- Source: modules/domains/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/11_resource_variants.sql
-- A second, distinct resource variant for recommendation and content API flows.
INSERT INTO edu_resource
    (id, name, type, url, size_bytes, subject_code, grade, difficulty, description, status, view_count, tenant_id, del_flag, create_by, update_by)
VALUES (2, '一元一次方程课后练习册', 'PDF', '/education/resources/math-linear-equation-practice.pdf', 1048576, 'MATH', 7, 2, '包含 12 道基础到中等难度练习题，适合课后巩固。', 1, 42, 1, 0, 'system', 'system');

INSERT INTO edu_resource_knowledge
    (resource_id, knowledge_id, sort_order, tenant_id, status, del_flag, create_by, update_by)
VALUES (2, 1001, 2, 1, 1, 0, 'system', 'system');

-- Source: modules/domains/conversation/shiyu-conversation-implementation/src/main/resources/db/baseline/h2/seed/conversation/11_conversation.sql
-- Realistic conversation seed matching the usage record and default admin user.
INSERT INTO chat_conversation
    (id, tenant_id, owner_user_id, scene_type, title, status, platform, model, version, created_at, updated_at)
VALUES ('demo-conversation-20260908', 1, 2, 'EDUCATION_TUTOR', '一元一次方程复习', 'ACTIVE',
        'DEEPSEEK', 'deepseek-v4-flash', 2,
        TIMESTAMP '2026-09-08 09:14:00', TIMESTAMP '2026-09-08 09:15:01');

INSERT INTO chat_message
    (id, tenant_id, conversation_id, role, content, status, sequence, created_at, updated_at)
VALUES ('demo-message-user-20260908', 1, 'demo-conversation-20260908', 'USER',
        '请用一个生活中的例子解释一元一次方程。', 'COMPLETED', 0,
        TIMESTAMP '2026-09-08 09:14:10', TIMESTAMP '2026-09-08 09:14:10');

INSERT INTO chat_message
    (id, tenant_id, conversation_id, parent_message_id, role, content, status, sequence, generation_id, created_at, updated_at)
VALUES ('demo-message-assistant-20260908', 1, 'demo-conversation-20260908', 'demo-message-user-20260908', 'ASSISTANT',
        '例如购物时已知总价和单价，可以用方程求购买数量。', 'COMPLETED', 1,
        'demo-generation-20260908-001', TIMESTAMP '2026-09-08 09:15:01', TIMESTAMP '2026-09-08 09:15:01');

INSERT INTO chat_generation_run
    (id, tenant_id, conversation_id, input_message_id, assistant_message_id, platform, model, status, prompt_tokens, completion_tokens, latency_ms, last_event_sequence, cancel_requested, version, created_at, updated_at)
VALUES ('demo-generation-20260908-001', 1, 'demo-conversation-20260908', 'demo-message-user-20260908',
        'demo-message-assistant-20260908', 'DEEPSEEK', 'deepseek-v4-flash', 'COMPLETED',
        842, 316, 1280, -1, FALSE, 1,
        TIMESTAMP '2026-09-08 09:14:00', TIMESTAMP '2026-09-08 09:15:01');

INSERT INTO chat_conversation
    (id, tenant_id, owner_user_id, scene_type, title, status, platform, model, version, created_at, updated_at)
VALUES ('demo-conversation-knowledge-20260908', 1, 2, 'KNOWLEDGE_QA', '课程资料检索', 'ACTIVE',
        'DEEPSEEK', 'deepseek-v4-flash', 1,
        TIMESTAMP '2026-09-08 10:02:00', TIMESTAMP '2026-09-08 10:03:12');

INSERT INTO chat_message
    (id, tenant_id, conversation_id, role, content, status, sequence, created_at, updated_at)
VALUES ('demo-message-knowledge-user-20260908', 1, 'demo-conversation-knowledge-20260908', 'USER',
        '课程资料中，一元一次方程的移项规则是什么？', 'COMPLETED', 0,
        TIMESTAMP '2026-09-08 10:02:10', TIMESTAMP '2026-09-08 10:02:10');

INSERT INTO chat_message
    (id, tenant_id, conversation_id, parent_message_id, role, content, status, sequence, generation_id, created_at, updated_at)
VALUES ('demo-message-knowledge-assistant-20260908', 1, 'demo-conversation-knowledge-20260908', 'demo-message-knowledge-user-20260908', 'ASSISTANT',
        '移项时要把项从等号一侧移到另一侧，并同时改变它的符号。', 'COMPLETED', 1,
        'demo-generation-knowledge-20260908-001', TIMESTAMP '2026-09-08 10:03:12', TIMESTAMP '2026-09-08 10:03:12');

INSERT INTO chat_generation_run
    (id, tenant_id, conversation_id, input_message_id, assistant_message_id, platform, model, status, prompt_tokens, completion_tokens, latency_ms, last_event_sequence, cancel_requested, version, created_at, updated_at)
VALUES ('demo-generation-knowledge-20260908-001', 1, 'demo-conversation-knowledge-20260908', 'demo-message-knowledge-user-20260908',
        'demo-message-knowledge-assistant-20260908', 'DEEPSEEK', 'deepseek-v4-flash', 'COMPLETED',
        516, 118, 940, -1, FALSE, 1,
        TIMESTAMP '2026-09-08 10:02:00', TIMESTAMP '2026-09-08 10:03:12');

-- Source: modules/domains/governance/shiyu-governance-implementation/src/main/resources/db/baseline/h2/seed/governance/10_governance.sql
-- Realistic usage seed for the default tenant and admin user.
INSERT INTO governance_usage_record
    (id, tenant_id, user_id, correlation_id, source_type, source_id,
     input_tokens, output_tokens, cost, occurred_at, usage_type, latency_ms, session_id, ext_info)
VALUES ('demo-usage-20260908-001', 1, 2, 'demo-chat-20260908-001', 'CHAT_GENERATION',
        'demo-generation-20260908-001', 842, 316, 0.00421000,
        TIMESTAMP '2026-09-08 09:15:00', 'METERED', 1280, 'demo-session-20260908',
        '{platform:deepseek,model:"deepseek-v4-flash",environment:demo}');

INSERT INTO governance_usage_record
    (id, tenant_id, user_id, correlation_id, source_type, source_id,
     input_tokens, output_tokens, cost, occurred_at, usage_type, latency_ms, session_id, ext_info)
VALUES ('demo-usage-20260908-002', 1, 2, 'demo-chat-knowledge-20260908-001', 'CHAT_GENERATION',
        'demo-generation-knowledge-20260908-001', 516, 118, 0.00207000,
        TIMESTAMP '2026-09-08 10:03:12', 'METERED', 940, 'demo-session-knowledge-20260908',
        '{appid:"knowledge-qa-app",platform:deepseek,model:"deepseek-v4-flash",environment:demo}');

-- Source: modules/domains/memory/shiyu-memory-implementation/src/main/resources/db/baseline/h2/seed/memory/12_memory.sql
-- Realistic memory event and graph seed for the default tenant.
INSERT INTO memory_entity
    (id, tenant_id, entity_type, external_ref, display_name, normalized_name, attributes, active)
VALUES ('demo-memory-entity-math', 1, 'KNOWLEDGE', 'knowledge:1001', '一元一次方程', '一元一次方程', '{subject:math,grade:7}', TRUE);

INSERT INTO memory_event
    (id, tenant_id, namespace, subject_type, subject_id, event_type, content, occurred_at, source_type, source_id, attributes, confidence, importance, status, confirmation_policy, created_at, updated_at)
VALUES ('demo-memory-event-math', 1, 'student:1', 'STUDENT', '1', 'LEARNING_MILESTONE',
        '学生在一元一次方程阶段测验中取得 95 分，能够独立完成移项和检验。',
        TIMESTAMP '2026-09-08 09:15:01', 'CHAT_GENERATION', 'demo-generation-20260908-001',
        '{score:95,subject:math}', 0.98, 0.85, 'ACTIVE', 'AUTO',
        TIMESTAMP '2026-09-08 09:15:02', TIMESTAMP '2026-09-08 09:15:02');

INSERT INTO memory_edge
    (id, tenant_id, source_node_id, target_node_id, graph_type, relation_type, directed, weight, confidence, origin, evidence_source, active, created_at)
VALUES ('demo-memory-edge-math', 1, 'demo-memory-event-math', 'demo-memory-entity-math', 'SEMANTIC', 'ABOUT', TRUE, 0.95, 0.98, 'SYSTEM', 'demo-generation-20260908-001', TRUE, TIMESTAMP '2026-09-08 09:15:02');

INSERT INTO memory_consolidation_job
    (id, tenant_id, event_id, status, attempts, available_at, created_at, updated_at)
VALUES (1, 1, 'demo-memory-event-math', 'COMPLETED', 1, TIMESTAMP '2026-09-08 09:16:00', TIMESTAMP '2026-09-08 09:15:02', TIMESTAMP '2026-09-08 09:16:00');

-- Source: modules/domains/tooling/shiyu-tooling-implementation/src/main/resources/db/baseline/h2/seed/tooling/15_plugin_market.sql
-- Realistic, disabled-by-default plugin catalog entry.
INSERT INTO plugin_market_entry
    (id, version, source, manifest, signature, publisher_key, permissions_json, checksum, update_policy, published_at, enabled)
VALUES ('edu-calculator', '1.0.0', 'https://plugins.example.invalid/edu-calculator-1.0.0.jar',
        '{name:"教育计算器",description:"为数学练习提供安全的表达式计算工具",entrypoint:"com.shiyu.plugin.education.CalculatorPlugin"}',
        NULL, NULL, '["calculator:read"]', 'demo-checksum-edu-calculator', 'MANUAL',
        TIMESTAMP '2026-09-08 08:00:00', FALSE);

-- The validator requires this marker after all schema and seed statements.
INSERT INTO common_schema_baseline (id, baseline_version, seed_profile) VALUES (1, '4', 'system-ai');
