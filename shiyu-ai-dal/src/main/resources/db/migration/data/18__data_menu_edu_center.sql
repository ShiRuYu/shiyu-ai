-- ============================================
-- Data: 教育中心菜单
-- 合并 学习/练习/考试/复习/教育管理 → 教育中心
-- ============================================

-- 教育中心主菜单
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1500, '教育中心', 'EducationCenter', 'CATALOG', NULL, 1, '/education-center', '/education-center/learning', 'lucide:graduation-cap', '', '教育全场景统一入口', TRUE, 1, 3, 0, 'system', 'system');

-- 教育中心子菜单
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1501, '学习', 'EduLearning', 'MENU', 1500, 1, '/education-center/learning', 'lucide:book-open', '/education-center/learning/index', '课程学习、知识浏览、学习计划', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1502, '练习', 'EduPractice', 'MENU', 1500, 1, '/education-center/practice', 'lucide:pen-tool', '/education-center/practice/index', '题库练习、错题本', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1503, '考试', 'EduExam', 'MENU', 1500, 1, '/education-center/exam', 'lucide:clipboard-check', '/education-center/exam/index', '在线考试、AI组卷', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1504, '复习', 'EduReview', 'MENU', 1500, 1, '/education-center/review', 'lucide:repeat', '/education-center/review/index', '今日复习、复习历史', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1505, '教育管理', 'EduManagement', 'MENU', 1500, 1, '/education-center/management', 'lucide:settings-2', '/education-center/management/index', '学科/教材/知识点/题目管理', TRUE, 1, 5, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1506, 'AI 助手', 'EduAiAssistant', 'MENU', 1500, 1, '/education-center/ai', 'lucide:sparkles', '/education-center/ai/index', 'AI 学习助手', TRUE, 1, 6, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1507, '数据中心', 'EduDataCenter', 'MENU', 1500, 1, '/education-center/data', 'lucide:bar-chart-3', '/education-center/data/index', '学习数据统计和分析', TRUE, 1, 7, 0, 'system', 'system');

-- 教育中心菜单-角色关联（教师/学生/家长）
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 2, 1500, 1, 1, 0, 'system', 'system'),
       (3, 2, 1501, 1, 1, 0, 'system', 'system'),
       (3, 2, 1502, 1, 1, 0, 'system', 'system'),
       (3, 2, 1503, 1, 1, 0, 'system', 'system'),
       (3, 2, 1504, 1, 1, 0, 'system', 'system'),
       (3, 2, 1505, 1, 1, 0, 'system', 'system'),
       (3, 2, 1506, 1, 1, 0, 'system', 'system'),
       (3, 2, 1507, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, 1500, 1, 1, 0, 'system', 'system'),
       (4, 2, 1501, 1, 1, 0, 'system', 'system'),
       (4, 2, 1502, 1, 1, 0, 'system', 'system'),
       (4, 2, 1503, 1, 1, 0, 'system', 'system'),
       (4, 2, 1504, 1, 1, 0, 'system', 'system'),
       (4, 2, 1506, 1, 1, 0, 'system', 'system'),
       (4, 2, 1507, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 2, 1500, 1, 1, 0, 'system', 'system'),
       (5, 2, 1507, 1, 1, 0, 'system', 'system');
