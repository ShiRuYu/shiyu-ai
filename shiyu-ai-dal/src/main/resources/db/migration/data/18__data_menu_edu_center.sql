-- ============================================
-- Data: 教育中心菜单重构
-- 合并 学习/练习/考试/复习/教育管理 → 教育中心
-- （数据/AI助手作为隐藏路由，不显示在侧边栏）
-- ============================================

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1500, '教育中心', 'EducationCenter', 'CATALOG', NULL, 1, '/education-center', '/education-center/learning', 'lucide:graduation-cap', '', '', TRUE, '教育全场景统一入口', TRUE, '1', 10, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1501, '学习', 'EduLearning', 'MENU', 1500, 1, '/education-center/learning', 'lucide:book-open', '/education-center/learning/index', '课程学习、知识浏览、学习计划、学习资源', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1502, '练习', 'EduPractice', 'MENU', 1500, 1, '/education-center/practice', 'lucide:pen-tool', '/education-center/practice/index', '题库练习、错题本', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1503, '考试', 'EduExam', 'MENU', 1500, 1, '/education-center/exam', 'lucide:clipboard-check', '/education-center/exam/index', '在线考试、AI组卷', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1504, '复习', 'EduReview', 'MENU', 1500, 1, '/education-center/review', 'lucide:repeat', '/education-center/review/index', '今日复习、复习历史', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1507, '管理', 'EduAdmin', 'MENU', 1500, 1, '/education-center/admin', 'carbon:education', '/education-center/admin/index', '课程体系/考试题库/学习管理/资源管理', TRUE, '1', 5, 0, '0', '0');

-- ============================================
-- 2. 角色权限更新
-- ============================================

DELETE FROM `role_workspace_menu` WHERE `menu_id` IN (700, 900, 1000, 1100, 1200, 1300, 1400);

INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
(0, 0, 1500, 1), (0, 0, 1501, 1), (0, 0, 1502, 1), (0, 0, 1503, 1),
(0, 0, 1504, 1), (0, 0, 1507, 1),
(1, 0, 1500, 1), (1, 0, 1501, 1), (1, 0, 1502, 1), (1, 0, 1503, 1),
(1, 0, 1504, 1), (1, 0, 1507, 1),
(3, 0, 1500, 1), (3, 0, 1501, 1), (3, 0, 1502, 1), (3, 0, 1503, 1),
(3, 0, 1504, 1), (3, 0, 1507, 1),
(4, 0, 1500, 1), (4, 0, 1501, 1), (4, 0, 1502, 1), (4, 0, 1503, 1),
(4, 0, 1504, 1),
(5, 0, 1500, 1),
(2, 0, 1500, 1), (2, 0, 1501, 1), (2, 0, 1502, 1), (2, 0, 1503, 1),
(2, 0, 1504, 1), (2, 0, 1507, 1);
