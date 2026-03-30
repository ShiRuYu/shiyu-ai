-- 初始化用户数据
INSERT INTO `user` (`id`, `username`, `enable`, `del_flag`, `create_time`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (1, 'admin', TRUE, 0, NOW(), NOW(), 'Admin', NULL, 'https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif?imageView2/1/w/80/h/80', NULL, NULL);

-- 初始化角色数据
INSERT INTO `role` (`id`, `code`, `name`, `enable`, `del_flag`) 
VALUES (1, 'SUPER_ADMIN', '超级管理员', TRUE, 0);

INSERT INTO `role` (`id`, `code`, `name`, `enable`, `del_flag`) 
VALUES (2, 'ROLE_QA', '质检员', TRUE, 0);

-- 初始化菜单数据
-- 根菜单：基础功能
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (9, '基础功能', 'Base', 'MENU', NULL, '', NULL, 'i-fe:grid', NULL, '', NULL, NULL, NULL, TRUE, TRUE, 0, 0);

-- 子菜单：图标 Icon
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (14, '图标 Icon', 'Icon', 'MENU', 9, '/base/icon', NULL, 'i-fe:feather', NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 1, 0);

-- 按钮权限：创建新用户
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (13, '创建新用户', 'AddUser', 'BUTTON', 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 1, 0);

-- 初始化用户角色关联数据
-- 用户 admin 分配超级管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`) 
VALUES (1, 1);

-- 用户 admin 分配质检员角色
INSERT INTO `user_role` (`user_id`, `role_id`) 
VALUES (1, 2);

-- 初始化角色菜单关联数据
-- 超级管理员的权限（这里先留空，根据实际需求配置）
-- INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES (1, 9);
-- INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES (1, 14);

-- 质检员的权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) 
VALUES (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 9), (2, 10), (2, 11), (2, 12), (2, 14), (2, 15);
