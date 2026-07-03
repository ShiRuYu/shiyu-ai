-- ============================================
-- Data: 教育中心菜单重构
-- 合并 学习/练习/考试/复习/数据/AI助手/教育管理 → 教育中心
--
-- 说明：10/17 已直接写入最终值（含 600 图标、旧 CATALOG order=999）
--       本文件仅处理 18 独有的逻辑：
--         ① 新增菜单（1500~1507）
--         ② 角色权限更新
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
VALUES (1505, '数据', 'EduAnalytics', 'MENU', 1500, 1, '/education-center/data', 'lucide:bar-chart-3', '/education-center/data/index', '学习报告、能力分析', TRUE, '1', 5, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1506, 'AI助手', 'EduAiTutor', 'MENU', 1500, 1, '/education-center/ai', 'lucide:bot', '/education-center/ai/index', 'AI讲解、AI出题、AI规划、AI对话、AI报告', TRUE, '1', 6, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1507, '管理', 'EduAdmin', 'MENU', 1500, 1, '/education-center/admin', 'carbon:education', '/education-center/admin/index', '课程体系/考试题库/学习管理/资源管理', TRUE, '1', 7, 0, '0', '0');

-- ============================================
-- 2. 角色权限更新
-- ============================================

DELETE FROM `role_workspace_menu` WHERE `menu_id` IN (700, 900, 1000, 1100, 1200, 1300, 1400);

INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
(0, 0, 1500, 1), (0, 0, 1501, 1), (0, 0, 1502, 1), (0, 0, 1503, 1),
(0, 0, 1504, 1), (0, 0, 1505, 1), (0, 0, 1506, 1), (0, 0, 1507, 1),
(1, 0, 1500, 1), (1, 0, 1501, 1), (1, 0, 1502, 1), (1, 0, 1503, 1),
(1, 0, 1504, 1), (1, 0, 1505, 1), (1, 0, 1506, 1), (1, 0, 1507, 1),
(3, 0, 1500, 1), (3, 0, 1501, 1), (3, 0, 1502, 1), (3, 0, 1503, 1),
(3, 0, 1504, 1), (3, 0, 1505, 1), (3, 0, 1506, 1), (3, 0, 1507, 1),
(4, 0, 1500, 1), (4, 0, 1501, 1), (4, 0, 1502, 1), (4, 0, 1503, 1),
(4, 0, 1504, 1), (4, 0, 1505, 1), (4, 0, 1506, 1),
(5, 0, 1500, 1), (5, 0, 1505, 1), (5, 0, 1506, 1),
(2, 0, 1500, 1), (2, 0, 1501, 1), (2, 0, 1502, 1), (2, 0, 1503, 1),
(2, 0, 1504, 1), (2, 0, 1505, 1), (2, 0, 1506, 1);
