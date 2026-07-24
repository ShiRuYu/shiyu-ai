-- ============================================
-- Data: auth_code → menu BUTTON 迁移
-- 将 auth_code 表中的权限码转为 menu 表的 BUTTON 类型
-- 并建立 role_scope_menu 关联
-- ============================================

-- 0. 注意：R__Seed_data_auth.sql 中 id=11 为 SystemAuthCode(权限码管理)，id=16 为 Agent(AgentDefinition)
--    BUTTON 菜单 parent_id 指向 id=16(Agent)

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1101, '查看用户列表', 'system:user:list', 'BUTTON', 2, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1102, '创建用户', 'system:user:create', 'BUTTON', 2, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1103, '更新用户', 'system:user:update', 'BUTTON', 2, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1104, '删除用户', 'system:user:delete', 'BUTTON', 2, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1105, '查看角色列表', 'system:role:list', 'BUTTON', 3, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1106, '创建角色', 'system:role:create', 'BUTTON', 3, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1107, '更新角色', 'system:role:update', 'BUTTON', 3, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1108, '删除角色', 'system:role:delete', 'BUTTON', 3, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1109, '查看菜单列表', 'system:menu:list', 'BUTTON', 4, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1110, '创建菜单', 'system:menu:create', 'BUTTON', 4, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1111, '更新菜单', 'system:menu:update', 'BUTTON', 4, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1112, '删除菜单', 'system:menu:delete', 'BUTTON', 4, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1113, '查看租户列表', 'system:tenant:list', 'BUTTON', 5, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1114, '创建租户', 'system:tenant:create', 'BUTTON', 5, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1115, '更新租户', 'system:tenant:update', 'BUTTON', 5, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1116, '删除租户', 'system:tenant:delete', 'BUTTON', 5, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1117, '查看子租户', 'system:sub-tenant:list', 'BUTTON', 6, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1118, '创建子租户', 'system:sub-tenant:create', 'BUTTON', 6, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1119, '更新子租户', 'system:sub-tenant:update', 'BUTTON', 6, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1120, '删除子租户', 'system:sub-tenant:delete', 'BUTTON', 6, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1121, '查看字典', 'system:dict:list', 'BUTTON', 7, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1122, '创建字典', 'system:dict:create', 'BUTTON', 7, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1123, '更新字典', 'system:dict:update', 'BUTTON', 7, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1124, '删除字典', 'system:dict:delete', 'BUTTON', 7, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1125, '查看 Agent 列表', 'agent:admin:list', 'BUTTON', 16, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1126, '创建 Agent', 'agent:admin:create', 'BUTTON', 16, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1127, '编辑 Agent', 'agent:admin:edit', 'BUTTON', 16, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1128, '删除 Agent', 'agent:admin:delete', 'BUTTON', 16, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1129, '查看平台', 'agent:platform:list', 'BUTTON', 12, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1130, '创建平台', 'agent:platform:create', 'BUTTON', 12, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1131, '编辑平台', 'agent:platform:edit', 'BUTTON', 12, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1132, '删除平台', 'agent:platform:delete', 'BUTTON', 12, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1133, '设置默认平台', 'agent:platform:set-default', 'BUTTON', 12, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1134, '查看模型', 'agent:model:list', 'BUTTON', 13, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1135, '创建模型', 'agent:model:create', 'BUTTON', 13, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1136, '编辑模型', 'agent:model:edit', 'BUTTON', 13, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1137, '删除模型', 'agent:model:delete', 'BUTTON', 13, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1138, '设置默认模型', 'agent:model:set-default', 'BUTTON', 13, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1139, '对话调试', 'agent:chat:config', 'BUTTON', 14, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1140, '查看意图', 'agent:intent:list', 'BUTTON', 15, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1141, '创建意图', 'agent:intent:create', 'BUTTON', 15, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1142, '删除意图', 'agent:intent:delete', 'BUTTON', 15, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1143, '查看知识点', 'knowledge:list', 'BUTTON', 71, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1144, '创建知识点', 'knowledge:create', 'BUTTON', 71, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1145, '编辑知识点', 'knowledge:edit', 'BUTTON', 71, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1146, '删除知识点', 'knowledge:delete', 'BUTTON', 71, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1147, '查看知识图谱', 'knowledge:graph', 'BUTTON', 72, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1148, '查看文档', 'knowledge:document:list', 'BUTTON', 73, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1149, '上传文档', 'knowledge:document:upload', 'BUTTON', 73, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1150, '删除文档', 'knowledge:document:delete', 'BUTTON', 73, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1151, '重建索引', 'knowledge:index:rebuild', 'BUTTON', 74, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1152, '管理知识关系', 'knowledge:relation', 'BUTTON', 75, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1153, '查看学科', 'edu:subject:list', 'BUTTON', 41, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1154, '创建学科', 'edu:subject:create', 'BUTTON', 41, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1155, '编辑学科', 'edu:subject:edit', 'BUTTON', 41, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1156, '删除学科', 'edu:subject:delete', 'BUTTON', 41, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1157, '查看教材', 'edu:textbook:list', 'BUTTON', 42, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1158, '创建教材', 'edu:textbook:create', 'BUTTON', 42, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1159, '编辑教材', 'edu:textbook:edit', 'BUTTON', 42, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1160, '删除教材', 'edu:textbook:delete', 'BUTTON', 42, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1161, '查看章节', 'edu:chapter:list', 'BUTTON', 43, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1162, '创建章节', 'edu:chapter:create', 'BUTTON', 43, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1163, '编辑章节', 'edu:chapter:edit', 'BUTTON', 43, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1164, '删除章节', 'edu:chapter:delete', 'BUTTON', 43, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1165, '查看课程', 'edu:course:list', 'BUTTON', 44, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1166, '创建课程', 'edu:course:create', 'BUTTON', 44, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1167, '编辑课程', 'edu:course:edit', 'BUTTON', 44, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1168, '删除课程', 'edu:course:delete', 'BUTTON', 44, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1169, '查看题目', 'edu:question:list', 'BUTTON', 45, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1170, '创建题目', 'edu:question:create', 'BUTTON', 45, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1171, '编辑题目', 'edu:question:edit', 'BUTTON', 45, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1172, '删除题目', 'edu:question:delete', 'BUTTON', 45, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1173, '查看考试', 'edu:exam:list', 'BUTTON', 46, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1174, '创建考试', 'edu:exam:create', 'BUTTON', 46, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1175, '编辑考试', 'edu:exam:edit', 'BUTTON', 46, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1176, '删除考试', 'edu:exam:delete', 'BUTTON', 46, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1177, '发布考试', 'edu:exam:publish', 'BUTTON', 46, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1178, '查看学生', 'edu:student:list', 'BUTTON', 47, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1179, '查看资源', 'edu:resource:list', 'BUTTON', 51, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1180, '上传资源', 'edu:resource:upload', 'BUTTON', 51, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1181, '删除资源', 'edu:resource:delete', 'BUTTON', 51, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1182, '查看学习计划', 'edu:plan:list', 'BUTTON', 48, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1183, '查看复习任务', 'edu:review:list', 'BUTTON', 49, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1184, '查看学情分析', 'edu:analytics', 'BUTTON', 50, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1185, '查看错题管理', 'edu:wrong-question', 'BUTTON', 52, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1186, '查看人物', 'record:profile:list', 'BUTTON', 81, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1187, '创建人物', 'record:profile:create', 'BUTTON', 81, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1188, '编辑人物', 'record:profile:edit', 'BUTTON', 81, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1189, '删除人物', 'record:profile:delete', 'BUTTON', 81, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1190, '查看时间轴', 'record:timeline:list', 'BUTTON', 82, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1191, '创建事件', 'record:timeline:create', 'BUTTON', 82, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1192, '删除事件', 'record:timeline:delete', 'BUTTON', 82, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1193, '查看标签', 'record:tags:list', 'BUTTON', 84, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1194, '查看媒体', 'record:media:list', 'BUTTON', 85, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1195, '上传媒体', 'record:media:upload', 'BUTTON', 85, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1196, '上传文件', 'file:upload', 'BUTTON', 90, 1, FALSE, 1, 0, 0, 'system', 'system');
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1197, '删除文件', 'file:delete', 'BUTTON', 90, 1, FALSE, 1, 0, 0, 'system', 'system');

-- 2. 建立角色-菜单关联
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1101, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1102, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1103, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1104, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1105, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1106, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1107, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1108, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1109, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1110, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1111, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1112, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1113, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1114, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1115, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1116, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1117, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1118, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1119, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1120, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1121, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1122, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1123, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1124, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1125, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1126, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1127, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1128, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1129, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1130, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1131, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1132, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1133, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1134, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1135, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1136, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1137, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1138, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1139, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1140, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1141, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1142, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1143, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1144, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1145, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1146, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1147, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1148, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1149, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1150, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1151, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1152, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1153, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1154, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1155, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1156, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1157, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1158, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1159, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1160, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1161, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1162, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1163, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1164, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1165, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1166, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1167, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1168, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1169, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1170, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1171, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1172, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1173, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1174, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1175, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1176, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1177, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1178, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1179, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1180, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1181, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1182, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1183, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1184, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1185, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1186, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1187, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1188, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1189, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1190, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1191, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1192, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1193, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1194, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1195, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1196, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, 1, 1197, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);


-- 2b. 将 BUTTON 权限也分配给 admin 角色（role_id=2）
--     管理员角色拥有和超级管理员相同的页面和按钮权限（不含学生业务）
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1101, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1102, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1103, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1104, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1105, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1106, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1107, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1108, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1109, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1110, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1111, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1112, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1113, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1114, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1115, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1116, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1117, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1118, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1119, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1120, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1121, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1122, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1123, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1124, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1125, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1126, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1127, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1128, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1129, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1130, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1131, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1132, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1133, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1134, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1135, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1136, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1137, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1138, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1139, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1140, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1141, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1142, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1143, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1144, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1145, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1146, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1147, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1148, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1149, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1150, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1151, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1152, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1153, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1154, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1155, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1156, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1157, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1158, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1159, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1160, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1161, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1162, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1163, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1164, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1165, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1166, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1167, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1168, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1169, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1170, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1171, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1172, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1173, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1174, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1175, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1176, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1177, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1178, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1179, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1180, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1181, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1182, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1183, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1184, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1185, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1186, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1187, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1188, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1189, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1190, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1191, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1192, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1193, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1194, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1195, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1196, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, 1, 1197, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
-- ============================================
-- 3. 标记旧 auth_code 表数据为已废弃
--    BUTTON 菜单已迁移到 menu 表并通过 role_scope_menu 关联，
--    原 auth_code 表中的对应权限码标记为已废弃
-- ============================================
UPDATE `auth_code` SET `del_flag` = 1, `update_by` = 'system', `update_time` = CURRENT_TIMESTAMP
WHERE `id` BETWEEN 1 AND 97 AND `del_flag` = 0;

-- ============================================
-- 4. 超级管理员 (role_id=1) 也拥有所有 BUTTON 菜单权限
--    （除已通过 role_scope_menu 关联的 admin 角色外）
-- ============================================
INSERT IGNORE INTO `role_scope_menu` (`role_id`, `scoped_tenant_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 1, 1, m.id, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM `menu` m
WHERE m.`type` = 'BUTTON';
