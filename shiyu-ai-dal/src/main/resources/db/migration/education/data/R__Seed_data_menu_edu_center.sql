-- ============================================
-- Data: 教育空间菜单
-- 扁平化: 学习/练习/考试/复习/AI助手/数据分析/后台管理
-- ============================================

-- 教育空间主菜单
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1500, '教育空间', 'EducationCenter', 'CATALOG', NULL, 1, '/education-center', '/learning/course', 'lucide:graduation-cap', '', '教育全场景统一入口', TRUE, 1, 20, 0, 'system', 'system');

-- 学习
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1501, '课程学习', 'EduLearningCourse', 'MENU', 1500, 1, '/learning/course', 'lucide:book', '/learning/course/list', '课程学习', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1502, '知识浏览', 'EduLearningKnowledge', 'MENU', 1500, 1, '/learning/knowledge', 'lucide:brain', '/learning/knowledge/list', '知识浏览', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1503, '学习计划', 'EduLearningPlan', 'MENU', 1500, 1, '/learning/plan', 'lucide:calendar-check', '/learning/plan/list', '学习计划', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1508, '学习资源', 'EduLearningResource', 'MENU', 1500, 1, '/learning/resource', 'lucide:folder-open', '/learning/resource/list', '学习资源', TRUE, 1, 4, 0, 'system', 'system');

-- 题库练习
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1510, '题库练习', 'EduPracticeQuestion', 'MENU', 1500, 1, '/practice/question', 'lucide:list-checks', '/practice/question/list', '题库练习', TRUE, 1, 5, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1511, '错题本', 'EduPracticeWrong', 'MENU', 1500, 1, '/practice/wrong', 'lucide:x-circle', '/practice/wrong-question/list', '错题本', TRUE, 1, 6, 0, 'system', 'system');

-- 考试
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1520, '在线考试', 'EduExamList', 'MENU', 1500, 1, '/exam/list', 'lucide:file-text', '/exam/exam-list/list', '在线考试', TRUE, 1, 7, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1521, 'AI 组卷', 'EduExamAi', 'MENU', 1500, 1, '/exam/ai-exam', 'lucide:sparkles', '/exam/ai-exam/index', 'AI组卷', TRUE, 1, 8, 0, 'system', 'system');

-- 复习
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1530, '今日复习', 'EduReviewToday', 'MENU', 1500, 1, '/review/today', 'lucide:calendar-days', '/review/today/list', '今日复习', TRUE, 1, 9, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1531, '复习历史', 'EduReviewHistory', 'MENU', 1500, 1, '/review/history', 'lucide:history', '/review/history/list', '复习历史', TRUE, 1, 10, 0, 'system', 'system');

-- AI 助手
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1540, 'AI 讲解', 'EduAiTeacher', 'MENU', 1500, 1, '/ai-tutor/teacher', 'lucide:graduation-cap', '/ai-tutor/teacher/index', 'AI讲解', TRUE, 1, 11, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1541, 'AI 出题', 'EduAiPractice', 'MENU', 1500, 1, '/ai-tutor/practice', 'lucide:pencil-ruler', '/ai-tutor/practice/index', 'AI出题', TRUE, 1, 12, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1542, 'AI 规划', 'EduAiPlanner', 'MENU', 1500, 1, '/ai-tutor/planner', 'lucide:route', '/ai-tutor/planner/index', 'AI规划', TRUE, 1, 13, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1543, 'AI 对话', 'EduAiChat', 'MENU', 1500, 1, '/ai-tutor/chat', 'lucide:message-circle', '/ai-tutor/chat/index', 'AI对话', TRUE, 1, 14, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1544, 'AI 报告', 'EduAiReport', 'MENU', 1500, 1, '/ai-tutor/report', 'lucide:file-output', '/ai-tutor/report-gen/index', 'AI报告', TRUE, 1, 15, 0, 'system', 'system');

-- 数据分析
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1550, '学习报告', 'EduAnalyticsReport', 'MENU', 1500, 1, '/analytics-center/report', 'lucide:file-bar-chart', '/analytics/report/index', '学习报告', TRUE, 1, 16, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1551, '能力雷达', 'EduAnalyticsRadar', 'MENU', 1500, 1, '/analytics-center/radar', 'lucide:radar', '/analytics/ability-radar/index', '能力雷达', TRUE, 1, 17, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1552, '学习趋势', 'EduAnalyticsTrend', 'MENU', 1500, 1, '/analytics-center/trend', 'lucide:trending-up', '/analytics/trend/index', '学习趋势', TRUE, 1, 18, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1553, '薄弱分析', 'EduAnalyticsWeak', 'MENU', 1500, 1, '/analytics-center/weak', 'lucide:alert-triangle', '/analytics/weak-points/list', '薄弱分析', TRUE, 1, 19, 0, 'system', 'system');

-- 教育空间菜单-角色关联（教师/学生/家长）
-- 教师角色拥有全部教育空间菜单
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 2, 1500, 1, 1, 0, 'system', 'system'),
       (3, 2, 1501, 1, 1, 0, 'system', 'system'),
       (3, 2, 1502, 1, 1, 0, 'system', 'system'),
       (3, 2, 1503, 1, 1, 0, 'system', 'system'),
       (3, 2, 1508, 1, 1, 0, 'system', 'system'),
       (3, 2, 1510, 1, 1, 0, 'system', 'system'),
       (3, 2, 1511, 1, 1, 0, 'system', 'system'),
       (3, 2, 1520, 1, 1, 0, 'system', 'system'),
       (3, 2, 1521, 1, 1, 0, 'system', 'system'),
       (3, 2, 1530, 1, 1, 0, 'system', 'system'),
       (3, 2, 1531, 1, 1, 0, 'system', 'system'),
       (3, 2, 1540, 1, 1, 0, 'system', 'system'),
       (3, 2, 1541, 1, 1, 0, 'system', 'system'),
       (3, 2, 1542, 1, 1, 0, 'system', 'system'),
       (3, 2, 1543, 1, 1, 0, 'system', 'system'),
       (3, 2, 1544, 1, 1, 0, 'system', 'system'),
       (3, 2, 1550, 1, 1, 0, 'system', 'system'),
       (3, 2, 1551, 1, 1, 0, 'system', 'system'),
       (3, 2, 1552, 1, 1, 0, 'system', 'system'),
       (3, 2, 1553, 1, 1, 0, 'system', 'system');

-- 学生角色拥有教育空间菜单（不含后台管理）
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, 1500, 1, 1, 0, 'system', 'system'),
       (4, 2, 1501, 1, 1, 0, 'system', 'system'),
       (4, 2, 1502, 1, 1, 0, 'system', 'system'),
       (4, 2, 1503, 1, 1, 0, 'system', 'system'),
       (4, 2, 1508, 1, 1, 0, 'system', 'system'),
       (4, 2, 1510, 1, 1, 0, 'system', 'system'),
       (4, 2, 1511, 1, 1, 0, 'system', 'system'),
       (4, 2, 1520, 1, 1, 0, 'system', 'system'),
       (4, 2, 1521, 1, 1, 0, 'system', 'system'),
       (4, 2, 1530, 1, 1, 0, 'system', 'system'),
       (4, 2, 1531, 1, 1, 0, 'system', 'system'),
       (4, 2, 1540, 1, 1, 0, 'system', 'system'),
       (4, 2, 1541, 1, 1, 0, 'system', 'system'),
       (4, 2, 1542, 1, 1, 0, 'system', 'system'),
       (4, 2, 1543, 1, 1, 0, 'system', 'system'),
       (4, 2, 1544, 1, 1, 0, 'system', 'system'),
       (4, 2, 1550, 1, 1, 0, 'system', 'system'),
       (4, 2, 1551, 1, 1, 0, 'system', 'system'),
       (4, 2, 1552, 1, 1, 0, 'system', 'system'),
       (4, 2, 1553, 1, 1, 0, 'system', 'system');

-- 家长角色拥有教育空间菜单（仅数据分析）
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 2, 1500, 1, 1, 0, 'system', 'system'),
       (5, 2, 1550, 1, 1, 0, 'system', 'system'),
       (5, 2, 1551, 1, 1, 0, 'system', 'system'),
       (5, 2, 1552, 1, 1, 0, 'system', 'system'),
       (5, 2, 1553, 1, 1, 0, 'system', 'system');

-- ============================================
-- 教育空间隐藏路由（菜单不可见，但程序导航可用）
-- ============================================

-- 课程详情
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1560, '课程详情', 'EduLearningCourseDetail', 'MENU', 1500, 1, '/learning/course/:id', 'lucide:book', '/learning/course/detail', '课程详情', FALSE, 1, 99, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1561, '课程学习', 'EduLearningCourseLearn', 'MENU', 1500, 1, '/learning/course/:courseId/learn', 'lucide:book', '/learning/course/learn', '课程学习页面', FALSE, 1, 99, 0, 'system', 'system');

-- 知识点详情
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1562, '知识点详情', 'EduLearningKnowledgeDetail', 'MENU', 1500, 1, '/learning/knowledge/:id', 'lucide:brain', '/learning/knowledge/detail', '知识点详情', FALSE, 1, 99, 0, 'system', 'system');

-- 学习计划详情
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1563, '计划详情', 'EduLearningPlanDetail', 'MENU', 1500, 1, '/learning/plan/:id', 'lucide:calendar-check', '/learning/plan/detail', '计划详情', FALSE, 1, 99, 0, 'system', 'system');

-- 答题中
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1564, '答题中', 'EduPracticeDoing', 'MENU', 1500, 1, '/practice/question/:id', 'lucide:list-checks', '/practice/question/practice', '答题中', FALSE, 1, 99, 0, 'system', 'system');

-- 考试中
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1565, '考试中', 'EduExamTake', 'MENU', 1500, 1, '/exam/take/:id', 'lucide:file-text', '/exam/exam-list/take', '考试中', FALSE, 1, 99, 0, 'system', 'system');

-- 考试结果
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1566, '考试结果', 'EduExamResult', 'MENU', 1500, 1, '/exam/result/:id', 'lucide:file-text', '/exam/exam-list/result', '考试结果', FALSE, 1, 99, 0, 'system', 'system');

-- 隐藏菜单-教师角色关联（id=1560~1566）
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 2, 1560, 1, 1, 0, 'system', 'system'),
       (3, 2, 1561, 1, 1, 0, 'system', 'system'),
       (3, 2, 1562, 1, 1, 0, 'system', 'system'),
       (3, 2, 1563, 1, 1, 0, 'system', 'system'),
       (3, 2, 1564, 1, 1, 0, 'system', 'system'),
       (3, 2, 1565, 1, 1, 0, 'system', 'system'),
       (3, 2, 1566, 1, 1, 0, 'system', 'system');

-- 隐藏菜单-学生角色关联
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, 1560, 1, 1, 0, 'system', 'system'),
       (4, 2, 1561, 1, 1, 0, 'system', 'system'),
       (4, 2, 1562, 1, 1, 0, 'system', 'system'),
       (4, 2, 1563, 1, 1, 0, 'system', 'system'),
       (4, 2, 1564, 1, 1, 0, 'system', 'system'),
       (4, 2, 1565, 1, 1, 0, 'system', 'system'),
       (4, 2, 1566, 1, 1, 0, 'system', 'system');
