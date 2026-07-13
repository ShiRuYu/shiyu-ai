-- ============================================
-- V016: 添加教育空间隐藏路由（不显示在菜单中，供程序导航使用）
-- 这些路由之前由前端 education-center.ts 定义，删除后需由后端补充
-- ============================================

-- 1. 插入隐藏菜单（show=FALSE → 前端解析为 hideInMenu: true）
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES
(1560, '课程详情',       'EduLearningCourseDetail',   'MENU', 1500, 1, '/learning/course/:id',            'lucide:book',          '/learning/course/detail',         '课程详情',   FALSE, 1, 99, 0, 'system', 'system'),
(1561, '课程学习',       'EduLearningCourseLearn',    'MENU', 1500, 1, '/learning/course/:courseId/learn','lucide:book',          '/learning/course/learn',          '课程学习页', FALSE, 1, 99, 0, 'system', 'system'),
(1562, '知识点详情',     'EduLearningKnowledgeDetail','MENU', 1500, 1, '/learning/knowledge/:id',         'lucide:brain',         '/learning/knowledge/detail',      '知识点详情', FALSE, 1, 99, 0, 'system', 'system'),
(1563, '计划详情',       'EduLearningPlanDetail',     'MENU', 1500, 1, '/learning/plan/:id',              'lucide:calendar-check','/learning/plan/detail',           '计划详情',   FALSE, 1, 99, 0, 'system', 'system'),
(1564, '答题中',         'EduPracticeDoing',          'MENU', 1500, 1, '/practice/question/:id',           'lucide:list-checks',   '/practice/question/practice',      '答题中',     FALSE, 1, 99, 0, 'system', 'system'),
(1565, '考试中',         'EduExamTake',               'MENU', 1500, 1, '/exam/take/:id',                   'lucide:file-text',     '/exam/exam-list/take',            '考试中',     FALSE, 1, 99, 0, 'system', 'system'),
(1566, '考试结果',       'EduExamResult',              'MENU', 1500, 1, '/exam/result/:id',                'lucide:file-text',     '/exam/exam-list/result',          '考试结果',   FALSE, 1, 99, 0, 'system', 'system');

-- 2. 角色-菜单关联（仅 super=0, admin=1, user=2 在 ws=0；teacher=3, student=4 在 ws=2）
--    隐藏菜单已有 parent_id=1500，角色只需有父菜单即可导航进入
--    role=0(super) ws=0
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
SELECT r.role_id, r.workspace_id, m.id, 1, 1, 0, 'system', 'system'
FROM (VALUES ROW(0,0), ROW(1,0), ROW(2,0), ROW(3,2), ROW(4,2)) AS r(role_id, workspace_id)
CROSS JOIN (SELECT id FROM `menu` WHERE id BETWEEN 1560 AND 1566) m
WHERE NOT EXISTS (
    SELECT 1 FROM `role_workspace_menu` rwm
    WHERE rwm.role_id = r.role_id AND rwm.workspace_id = r.workspace_id AND rwm.menu_id = m.id
);
