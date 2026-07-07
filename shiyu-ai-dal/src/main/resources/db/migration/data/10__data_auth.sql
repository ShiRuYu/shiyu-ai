-- ============================================
-- Data: data_auth
-- ============================================

INSERT IGNORE INTO `tenant` (`id`, `code`, `name`, `contact_name`, `contact_phone`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'default', '默认租户', 'Admin', '13800000000', '1', 0, '0', '0');

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (0, 'vben', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, '1', 0, '0', NOW(), '0', NOW(), 'Vben', NULL, NULL, NULL, NULL);

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (1, 'admin', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, '1', 0, '0', NOW(), '0', NOW(), 'Admin', NULL, NULL, NULL, NULL);

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (2, 'jack', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, '1', 0, '0', NOW(), '0', NOW(), 'Jack', NULL, NULL, NULL, NULL);

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (0, 'super', '超级管理员', 1, '1', '拥有系统所有权限', 0, '0', NOW(), '0', NOW());

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (1, 'admin', '管理员', 1, '1', '系统管理员角色', 0, '0', NOW(), '0', NOW());

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (2, 'user', '普通用户', 1, '1', '普通用户角色', 0, '0', NOW(), '0', NOW());

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`)
VALUES (0, 0, 0, 1);

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`)
VALUES (1, 0, 1, 1);

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`)
VALUES (2, 0, 2, 1);

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (0, 0, '默认空间', 1, 0, NULL, NULL, NULL, '1', '系统默认工作空间', 0, '0', '0');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 0, '总公司', 1, 1, 'Vben', '15888888888', 'vben@shiyu.com', '1', '公司顶层组织', 0, '0', '0');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 1, '技术部', 1, 1, NULL, NULL, NULL, '1', '研发技术部门', 0, '0', '0');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 1, '产品部', 1, 2, NULL, NULL, NULL, '1', '产品部门', 0, '0', '0');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, '前端组', 1, 1, NULL, NULL, NULL, '1', NULL, 0, '0', '0');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 2, '后端组', 1, 2, NULL, NULL, NULL, '1', NULL, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (1, '仪表盘', 'Dashboard', 'MENU', NULL, 1, '/dashboard', '/analytics', 'lucide:layout-dashboard', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', -1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (2, '分析页', 'Analytics', 'MENU', 1, 1, '/analytics', NULL, 'lucide:area-chart', '/dashboard/analytics/index', '', TRUE, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (3, '工作空间', 'Workspace', 'MENU', 1, 1, '/workspace', NULL, 'carbon:workspace', '/dashboard/workspace/index', '', TRUE, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (100, '系统管理', 'System', 'MENU', NULL, 1, '/system', NULL, 'ion:settings-outline', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', 9997, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (200, '用户管理', 'SystemUser', 'MENU', 100, 1, '/system/user', NULL, 'carbon:user-avatar', '/system/user/list', '', TRUE, NULL, NULL, TRUE, '1', 0, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20001, '用户查询', 'system:user:query', 'BUTTON', 200, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询用户列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20002, '用户新增', 'system:user:create', 'BUTTON', 200, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增用户', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20003, '用户修改', 'system:user:update', 'BUTTON', 200, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改用户', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20004, '用户删除', 'system:user:delete', 'BUTTON', 200, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除用户', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (201, '菜单管理', 'SystemMenu', 'MENU', 100, 1, '/system/menu', NULL, 'carbon:menu', '/system/menu/list', '', TRUE, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20101, '新增菜单', 'System:Menu:Create', 'BUTTON', 201, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20102, '编辑菜单', 'System:Menu:Edit', 'BUTTON', 201, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20103, '删除菜单', 'System:Menu:Delete', 'BUTTON', 201, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (202, '工作空间管理', 'SystemWorkspace', 'MENU', 100, 1, '/system/workspace', NULL, 'carbon:container-services', '/system/workspace/list', '', TRUE, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20201, '新增工作空间', 'System:Workspace:Create', 'BUTTON', 202, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20202, '编辑工作空间', 'System:Workspace:Edit', 'BUTTON', 202, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20203, '删除工作空间', 'System:Workspace:Delete', 'BUTTON', 202, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (203, '角色管理', 'SystemRole', 'MENU', 100, 1, '/system/role', NULL, 'carbon:user-role', '/system/role/list', '', TRUE, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20301, '角色查询', 'system:role:query', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询角色列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20302, '角色新增', 'system:role:create', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增角色', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20303, '角色修改', 'system:role:update', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改角色信息', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20304, '角色删除', 'system:role:delete', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除角色', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (204, '字典管理', 'SystemDict', 'MENU', 100, 1, '/system/dict', NULL, 'carbon:data-table', '/common/dict/list', '', TRUE, NULL, NULL, TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20401, '字典查询', 'system:dict:query', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询字典列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20402, '字典新增', 'system:dict:create', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增字典', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20403, '字典修改', 'system:dict:update', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改字典', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20404, '字典删除', 'system:dict:delete', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除字典', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (205, '租户管理', 'SystemTenant', 'MENU', 100, 1, '/system/tenant', NULL, 'carbon:enterprise', '/system/tenant/list', '', TRUE, NULL, NULL, TRUE, '1', 5, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20501, '租户查询', 'system:tenant:query', 'BUTTON', 205, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询租户列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20502, '租户新增', 'system:tenant:create', 'BUTTON', 205, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增租户', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20503, '租户修改', 'system:tenant:update', 'BUTTON', 205, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改租户', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20504, '租户删除', 'system:tenant:delete', 'BUTTON', 205, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除租户', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (400, '日常记录', 'Record', 'MENU', NULL, 1, '/record', NULL, 'mdi:book-open-page-variant', '', '', FALSE, NULL, '日常记录管理目录', TRUE, '1', 5, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (401, '人物管理', 'RecordProfile', 'MENU', 400, 1, '/record/profile', NULL, 'mdi:account-multiple', '/record/profile/list', '', TRUE, NULL, '人物信息管理', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40101, '人物查询', 'record:profile:query', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询人物列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40102, '人物新增', 'record:profile:add', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增人物', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40103, '人物修改', 'record:profile:edit', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改人物信息', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40104, '人物删除', 'record:profile:remove', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除人物', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (402, '时间轴管理', 'RecordTimeline', 'MENU', 400, 1, '/record/timeline', NULL, 'mdi:timeline', '/record/timeline/list', '', TRUE, NULL, '时间轴事件管理', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40201, '时间轴查询', 'record:timeline:query', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询时间轴事件列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40202, '时间轴新增', 'record:timeline:add', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增时间轴事件', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40203, '时间轴修改', 'record:timeline:edit', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改时间轴事件', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40204, '时间轴删除', 'record:timeline:remove', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除时间轴事件', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (500, '智能体', 'Agent', 'MENU', NULL, 1, '/agent', NULL, 'carbon:ibm-watson-assistant', '', '', FALSE, NULL, 'AI智能体管理', TRUE, '1', 6, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (403, '记录管理', 'RecordRecords', 'MENU', 400, 1, '/record/records', NULL, 'mdi:book-open-page-variant', '/record/records/list', '', TRUE, NULL, '日常记录管理', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (404, '媒体管理', 'RecordMedia', 'MENU', 400, 1, '/record/media', NULL, 'carbon:folder', '/record/media/list', '', TRUE, NULL, '媒体文件管理', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (405, '标签管理', 'RecordTags', 'MENU', 400, 1, '/record/tags', NULL, 'carbon:tag', '/record/tags/list', '', TRUE, NULL, '标签管理', TRUE, '1', 5, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (501, 'Agent管理', 'AgentAdminList', 'MENU', 500, 1, '/agent/admin/list', NULL, 'carbon:cube', '/agent/admin/agent-list', '', TRUE, NULL, 'Agent 注册与管理', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (503, '平台管理', 'CommonPlatform', 'MENU', 500, 1, '/agent/platform', NULL, 'carbon:cloud', '/agent/platform/list', '', TRUE, NULL, 'AI平台管理', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50301, '平台查询', 'common:platform:query', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询平台列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50302, '平台新增', 'common:platform:create', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增平台', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50303, '平台修改', 'common:platform:update', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改平台', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50304, '平台删除', 'common:platform:delete', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除平台', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (504, '模型管理', 'CommonModel', 'MENU', 500, 1, '/agent/model', NULL, 'carbon:ai-generate', '/agent/model/list', '', TRUE, NULL, 'AI模型管理', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50401, '模型查询', 'common:model:query', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询模型列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50402, '模型新增', 'common:model:create', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增模型', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50403, '模型修改', 'common:model:update', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改模型', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50404, '模型删除', 'common:model:delete', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除模型', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (505, '版本管理', 'AgentVersion', 'MENU', 500, 1, '/agent/admin/edit', NULL, 'carbon:version', '/agent/admin/agent-edit', '', FALSE, NULL, 'Agent 版本管理与 Graph 编排（隐藏菜单，请从编辑页面进入）', FALSE, '1', 5, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (507, '意图管理', 'AgentIntent', 'MENU', 500, 1, '/agent/intent', NULL, 'carbon:idea', '/agent/intent/list', '', TRUE, NULL, '意图定义管理', TRUE, '1', 7, 0, '0', '0');

INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES 
(0, 0, 1, 1), (0, 0, 2, 1), (0, 0, 3, 1), 
(0, 0, 100, 1), (0, 0, 200, 1), (0, 0, 20001, 1), (0, 0, 20002, 1), (0, 0, 20003, 1), (0, 0, 20004, 1), (0, 0, 201, 1), (0, 0, 20101, 1), (0, 0, 20102, 1), (0, 0, 20103, 1), (0, 0, 202, 1), (0, 0, 20201, 1), (0, 0, 20202, 1), (0, 0, 20203, 1), (0, 0, 203, 1), (0, 0, 20301, 1), (0, 0, 20302, 1), (0, 0, 20303, 1), (0, 0, 20304, 1), (0, 0, 204, 1), (0, 0, 20401, 1), (0, 0, 20402, 1), (0, 0, 20403, 1), (0, 0, 20404, 1),
(0, 0, 205, 1), (0, 0, 20501, 1), (0, 0, 20502, 1), (0, 0, 20503, 1), (0, 0, 20504, 1),
(0, 0, 400, 1), (0, 0, 401, 1), (0, 0, 40101, 1), (0, 0, 40102, 1), (0, 0, 40103, 1), (0, 0, 40104, 1), (0, 0, 402, 1), (0, 0, 40201, 1), (0, 0, 40202, 1), (0, 0, 40203, 1), (0, 0, 40204, 1), (0, 0, 403, 1), (0, 0, 404, 1), (0, 0, 405, 1),
(0, 0, 500, 1), (0, 0, 501, 1), (0, 0, 502, 1), (0, 0, 503, 1), (0, 0, 50301, 1), (0, 0, 50302, 1), (0, 0, 50303, 1), (0, 0, 50304, 1), (0, 0, 504, 1), (0, 0, 50401, 1), (0, 0, 50402, 1), (0, 0, 50403, 1), (0, 0, 50404, 1), (0, 0, 505, 1), (0, 0, 507, 1);

INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES 
(1, 0, 1, 1), (1, 0, 2, 1), (1, 0, 3, 1), 
(1, 0, 100, 1), (1, 0, 200, 1), (1, 0, 20001, 1), (1, 0, 20002, 1), (1, 0, 20003, 1), (1, 0, 20004, 1), (1, 0, 201, 1), (1, 0, 20101, 1), (1, 0, 20102, 1), (1, 0, 20103, 1), (1, 0, 202, 1), (1, 0, 20201, 1), (1, 0, 20202, 1), (1, 0, 20203, 1), (1, 0, 203, 1), (1, 0, 20301, 1), (1, 0, 20302, 1), (1, 0, 20303, 1), (1, 0, 20304, 1), (1, 0, 204, 1), (1, 0, 20401, 1), (1, 0, 20402, 1), (1, 0, 20403, 1), (1, 0, 20404, 1),
(1, 0, 205, 1), (1, 0, 20501, 1), (1, 0, 20502, 1), (1, 0, 20503, 1), (1, 0, 20504, 1),
(1, 0, 400, 1), (1, 0, 401, 1), (1, 0, 40101, 1), (1, 0, 40102, 1), (1, 0, 40103, 1), (1, 0, 40104, 1), (1, 0, 402, 1), (1, 0, 40201, 1), (1, 0, 40202, 1), (1, 0, 40203, 1), (1, 0, 40204, 1), (1, 0, 403, 1), (1, 0, 404, 1), (1, 0, 405, 1),
(1, 0, 500, 1), (1, 0, 501, 1), (1, 0, 502, 1), (1, 0, 503, 1), (1, 0, 50301, 1), (1, 0, 50302, 1), (1, 0, 50303, 1), (1, 0, 50304, 1), (1, 0, 504, 1), (1, 0, 50401, 1), (1, 0, 50402, 1), (1, 0, 50403, 1), (1, 0, 50404, 1), (1, 0, 505, 1), (1, 0, 507, 1);

INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES 
(2, 0, 1, 1), (2, 0, 2, 1), (2, 0, 3, 1),
(2, 0, 500, 1), (2, 0, 501, 1), (2, 0, 502, 1), (2, 0, 503, 1), (2, 0, 504, 1), (2, 0, 507, 1);

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (1, 'AC_100100', '权限码 100100', 1, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (2, 'AC_100110', '权限码 100110', 1, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (3, 'AC_100120', '权限码 100120', 1, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (4, 'AC_100010', '权限码 100010', 1, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (5, 'AC_100010', '权限码 100010', 2, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (6, 'AC_100020', '权限码 100020', 2, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (7, 'AC_100030', '权限码 100030', 2, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (8, 'AC_1000001', '权限码 1000001', 3, 1, '1', 0, '0', '0');

INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (9, 'AC_1000002', '权限码 1000002', 3, 1, '1', 0, '0', '0');


-- ============================================
-- 9. 智能体-对话调试
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (506, '对话调试', 'AgentChatConfig', 'MENU', 500, 1, '/agent/chat-config', NULL, 'carbon:chat', '/agent/chat-config/index', '', TRUE, NULL, 'AI对话调试与测试', TRUE, '1', 6, 0, '0', '0');

-- ============================================
-- 10. 知识库管理模块
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (600, '知识库管理', 'Knowledge', 'CATALOG', NULL, 1, '/knowledge', '/knowledge/list', 'lucide:library', '', '', TRUE, NULL, '知识点与文档管理', TRUE, '1', 7, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (601, '知识点管理', 'KnowledgeList', 'MENU', 600, 1, '/knowledge/list', NULL, 'carbon:concept', '/knowledge-engine/knowledge-list/list', '', TRUE, NULL, '知识点CRUD管理', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60101, '知识点查询', 'knowledge:query', 'BUTTON', 601, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询知识点列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60102, '知识点新增', 'knowledge:create', 'BUTTON', 601, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增知识点', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60103, '知识点修改', 'knowledge:update', 'BUTTON', 601, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改知识点', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60104, '知识点删除', 'knowledge:delete', 'BUTTON', 601, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除知识点', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (602, '知识关系', 'KnowledgeRelation', 'MENU', 600, 1, '/knowledge/relation', NULL, 'carbon:flow', '/knowledge-engine/knowledge-relation/index', '', TRUE, NULL, '知识关系（前置/后续/相关）管理', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (603, '知识图谱', 'KnowledgeGraph', 'MENU', 600, 1, '/knowledge/graph', NULL, 'carbon:network-3', '/knowledge-engine/knowledge-graph/index', '', TRUE, NULL, '知识图谱可视化', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (604, '文档管理', 'KnowledgeDocument', 'MENU', 600, 1, '/knowledge/document', NULL, 'carbon:document', '/knowledge-engine/document/list', '', TRUE, NULL, '文档CRUD管理', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60401, '文档查询', 'knowledge:doc:query', 'BUTTON', 604, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询文档列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60402, '文档新增', 'knowledge:doc:create', 'BUTTON', 604, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增文档', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60403, '文档修改', 'knowledge:doc:update', 'BUTTON', 604, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改文档', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (60404, '文档删除', 'knowledge:doc:delete', 'BUTTON', 604, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除文档', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (605, '索引管理', 'KnowledgeIndex', 'MENU', 600, 1, '/knowledge/index', NULL, 'carbon:data-class', '/knowledge-engine/index-rebuild/list', '', TRUE, NULL, '重建索引/状态查看', TRUE, '1', 5, 0, '0', '0');

-- ============================================
-- 11. 教育管理模块
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (700, '教育管理', 'Education', 'CATALOG', NULL, 1, '/edu', '/edu/subject', 'carbon:education', '', '', TRUE, NULL, '教育业务管理目录', TRUE, '1', 999, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (701, '学科管理', 'EducationSubject', 'MENU', 700, 1, '/edu/subject', NULL, 'carbon:book', '/education-admin/subject/list', '', TRUE, NULL, '学科管理', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (702, '教材管理', 'EducationTextbook', 'MENU', 700, 1, '/edu/textbook', NULL, 'carbon:notebook', '/education-admin/textbook/list', '', TRUE, NULL, '教材版本管理', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (703, '章节管理', 'EducationChapter', 'MENU', 700, 1, '/edu/chapter', NULL, 'carbon:tree', '/education-admin/chapter/list', '', TRUE, NULL, '章节树形管理', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (704, '课程管理', 'EducationCourse', 'MENU', 700, 1, '/edu/course', NULL, 'carbon:course', '/education-admin/course-admin/list', '', TRUE, NULL, '课程管理', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (705, '试卷管理', 'EducationExam', 'MENU', 700, 1, '/edu/exam', NULL, 'carbon:exam', '/education-admin/exam-admin/list', '', TRUE, NULL, '试卷管理', TRUE, '1', 5, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (706, '题库管理', 'EducationQuestion', 'MENU', 700, 1, '/edu/question', NULL, 'carbon:list-boxes', '/education-admin/question-admin/list', '', TRUE, NULL, '题库管理', TRUE, '1', 6, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (707, '学习计划', 'EducationPlan', 'MENU', 700, 1, '/edu/plan', NULL, 'carbon:task', '/education-admin/plan/list', '', TRUE, NULL, '学习计划管理', TRUE, '1', 7, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (708, '复习任务', 'EducationReview', 'MENU', 700, 1, '/edu/review', NULL, 'carbon:rotate', '/education-admin/review/list', '', TRUE, NULL, '复习任务管理', TRUE, '1', 8, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (709, '学情分析', 'EducationAnalytics', 'MENU', 700, 1, '/edu/analytics', NULL, 'carbon:chart-radar', '/education-admin/analytics/index', '', TRUE, NULL, '学情分析看板', TRUE, '1', 9, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (710, '资源管理', 'EducationResource', 'MENU', 700, 1, '/edu/resource', NULL, 'carbon:document', '/education-admin/resource-admin/list', '', TRUE, NULL, '资源管理', TRUE, '1', 10, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (711, '错题管理', 'EducationWrongQ', 'MENU', 700, 1, '/edu/wrong-question', NULL, 'carbon:error', '/education-admin/wrong-question/list', '', TRUE, NULL, '错题管理', TRUE, '1', 11, 0, '0', '0');

-- ============================================
-- 12. 文件管理
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (800, '文件管理', 'FileManager', 'MENU', NULL, 1, '/file', NULL, 'carbon:folder', '/file/list', '', TRUE, NULL, '文件管理', TRUE, '1', 9, 0, '0', '0');

-- ============================================
-- 13. 新菜单 -> super角色 (role_id=0) 完整权限
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES 
(0, 0, 506, 1),
(0, 0, 600, 1), (0, 0, 601, 1), (0, 0, 60101, 1), (0, 0, 60102, 1), (0, 0, 60103, 1), (0, 0, 60104, 1),
(0, 0, 602, 1), (0, 0, 603, 1),
(0, 0, 604, 1), (0, 0, 60401, 1), (0, 0, 60402, 1), (0, 0, 60403, 1), (0, 0, 60404, 1),
(0, 0, 605, 1),
(0, 0, 700, 1), (0, 0, 701, 1), (0, 0, 702, 1), (0, 0, 703, 1), (0, 0, 704, 1),
(0, 0, 705, 1), (0, 0, 706, 1), (0, 0, 707, 1), (0, 0, 708, 1), (0, 0, 709, 1),
(0, 0, 710, 1), (0, 0, 711, 1),
(0, 0, 800, 1);

-- ============================================
-- 14. 新菜单 -> admin角色 (role_id=1) 完整权限
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES 
(1, 0, 506, 1),
(1, 0, 600, 1), (1, 0, 601, 1), (1, 0, 60101, 1), (1, 0, 60102, 1), (1, 0, 60103, 1), (1, 0, 60104, 1),
(1, 0, 602, 1), (1, 0, 603, 1),
(1, 0, 604, 1), (1, 0, 60401, 1), (1, 0, 60402, 1), (1, 0, 60403, 1), (1, 0, 60404, 1),
(1, 0, 605, 1),
(1, 0, 700, 1), (1, 0, 701, 1), (1, 0, 702, 1), (1, 0, 703, 1), (1, 0, 704, 1),
(1, 0, 705, 1), (1, 0, 706, 1), (1, 0, 707, 1), (1, 0, 708, 1), (1, 0, 709, 1),
(1, 0, 710, 1), (1, 0, 711, 1),
(1, 0, 800, 1);

-- ============================================
-- 15. 新菜单 -> user角色 (role_id=2) 仅知识库和对话调试
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES 
(2, 0, 506, 1),
(2, 0, 600, 1), (2, 0, 601, 1), (2, 0, 604, 1);
