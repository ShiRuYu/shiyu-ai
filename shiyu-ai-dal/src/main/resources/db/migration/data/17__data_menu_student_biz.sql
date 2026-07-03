-- ============================================
-- Data: 前端菜单与权限补充
-- 基于 13-前端规划方案.md
-- 新增: 学习中心/练习中心/考试中心/复习中心/数据中心/AI助手
-- 新增角色: teacher/student/parent
-- 更新: 教育管理菜单组件路径
-- ============================================

-- ============================================
-- 1. 新增教育角色
-- ============================================
INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (3, 'teacher', '教师', 1, '1', '教师角色，可管理教务和使用AI助手', 0, '0', NOW(), '0', NOW());

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (4, 'student', '学生', 1, '1', '学生角色，可使用学习/练习/考试/复习/AI助手', 0, '0', NOW(), '0', NOW());

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (5, 'parent', '家长', 1, '1', '家长角色，可查看数据中心和学习报告', 0, '0', NOW(), '0', NOW());

-- ============================================
-- 2. 学习中心 (900)
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (900, '学习中心', 'Learning', 'CATALOG', NULL, 1, '/learning', NULL, 'lucide:book-open', '', '', TRUE, NULL, '学生学习入口', TRUE, '1', 999, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (901, '课程学习', 'LearningCourse', 'MENU', 1501, 1, '/learning/course', NULL, 'lucide:book', '/learning/course/list', '', TRUE, NULL, '课程列表与学习', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90101, '课程查询', 'learning:course:query', 'BUTTON', 901, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询课程列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90102, '开始学习', 'learning:course:learn', 'BUTTON', 901, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '开始学习课程', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (902, '知识浏览', 'LearningKnowledge', 'MENU', 1501, 1, '/learning/knowledge', NULL, 'lucide:brain', '/learning/knowledge/list', '', TRUE, NULL, '知识点浏览与搜索', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90201, '知识查询', 'learning:knowledge:query', 'BUTTON', 902, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询知识点', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (903, '学习计划', 'LearningPlan', 'MENU', 1501, 1, '/learning/plan', NULL, 'lucide:calendar-check', '/learning/plan/list', '', TRUE, NULL, '学习计划与每日任务', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90301, '计划查询', 'learning:plan:query', 'BUTTON', 903, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询学习计划', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90302, '创建计划', 'learning:plan:create', 'BUTTON', 903, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '创建学习计划', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (904, '学习资源', 'LearningResource', 'MENU', 1501, 1, '/learning/resource', NULL, 'lucide:folder-open', '/learning/resource/list', '', TRUE, NULL, '学习资源浏览', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90401, '资源查询', 'learning:resource:query', 'BUTTON', 904, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询学习资源', TRUE, '1', 1, 0, '0', '0');

-- ============================================
-- 3. 练习中心 (1000)
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1000, '练习中心', 'Practice', 'CATALOG', NULL, 1, '/practice', NULL, 'lucide:pen-tool', '', '', TRUE, NULL, '题库练习与错题本', TRUE, '1', 999, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1001, '题库练习', 'PracticeQuestion', 'MENU', 1502, 1, '/practice/question', NULL, 'lucide:list-checks', '/practice/question/list', '', TRUE, NULL, '按学科/年级/难度练习', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (100101, '题目查询', 'practice:question:query', 'BUTTON', 1001, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询题目', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (100102, '提交答案', 'practice:question:answer', 'BUTTON', 1001, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '提交题目答案', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1002, '错题本', 'PracticeWrong', 'MENU', 1502, 1, '/practice/wrong', NULL, 'lucide:x-circle', '/practice/wrong-question/list', '', TRUE, NULL, '学生错题管理', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (100201, '错题查询', 'practice:wrong:query', 'BUTTON', 1002, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询错题', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (100202, '重新练习', 'practice:wrong:retry', 'BUTTON', 1002, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '重新练习错题', TRUE, '1', 2, 0, '0', '0');

-- ============================================
-- 4. 考试中心 (1100)
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1100, '考试中心', 'Exam', 'CATALOG', NULL, 1, '/exam', NULL, 'lucide:clipboard-check', '', '', TRUE, NULL, '在线考试与AI组卷', TRUE, '1', 999, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1101, '在线考试', 'ExamList', 'MENU', 1503, 1, '/exam/list', NULL, 'lucide:file-text', '/exam/exam-list/list', '', TRUE, NULL, '考试列表与答题', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (110101, '考试查询', 'exam:list:query', 'BUTTON', 1101, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询考试列表', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (110102, '参加考试', 'exam:list:take', 'BUTTON', 1101, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '开始考试', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (110103, '交卷', 'exam:list:submit', 'BUTTON', 1101, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '提交考试', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1102, 'AI组卷', 'ExamAiExam', 'MENU', 1503, 1, '/exam/ai-exam', NULL, 'lucide:sparkles', '/exam/ai-exam/index', '', TRUE, NULL, 'AI智能组卷', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (110201, 'AI组卷', 'exam:ai:generate', 'BUTTON', 1102, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', 'AI生成试卷', TRUE, '1', 1, 0, '0', '0');

-- ============================================
-- 5. 复习中心 (1200)
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1200, '复习中心', 'Review', 'CATALOG', NULL, 1, '/review', NULL, 'lucide:repeat', '', '', TRUE, NULL, '艾宾浩斯复习管理', TRUE, '1', 999, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1201, '今日复习', 'ReviewToday', 'MENU', 1504, 1, '/review/today', NULL, 'lucide:calendar-days', '/review/today/list', '', TRUE, NULL, '今日艾宾浩斯复习任务', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (120101, '复习查询', 'review:today:query', 'BUTTON', 1201, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询今日复习任务', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (120102, '完成复习', 'review:today:complete', 'BUTTON', 1201, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '完成复习任务', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1202, '复习历史', 'ReviewHistory', 'MENU', 1504, 1, '/review/history', NULL, 'lucide:history', '/review/history/list', '', TRUE, NULL, '复习历史记录', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (120201, '历史查询', 'review:history:query', 'BUTTON', 1202, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询复习历史', TRUE, '1', 1, 0, '0', '0');

-- ============================================
-- 6. 数据中心 (1300)
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1300, '数据中心', 'AnalyticsCenter', 'CATALOG', NULL, 1, '/analytics-center', NULL, 'lucide:bar-chart-3', '', '', TRUE, NULL, '学习数据分析', TRUE, '1', 999, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1301, '学习报告', 'AnalyticsReport', 'MENU', 1505, 1, '/analytics-center/report', NULL, 'lucide:file-bar-chart', '/analytics/report/index', '', TRUE, NULL, 'AI生成学习报告', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (130101, '报告查看', 'analytics:report:view', 'BUTTON', 1301, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查看学习报告', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1302, '能力雷达', 'AnalyticsRadar', 'MENU', 1505, 1, '/analytics-center/radar', NULL, 'lucide:radar', '/analytics/ability-radar/index', '', TRUE, NULL, 'Bloom六维度能力雷达图', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (130201, '雷达查看', 'analytics:radar:view', 'BUTTON', 1302, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查看能力雷达图', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1303, '学习趋势', 'AnalyticsTrend', 'MENU', 1505, 1, '/analytics-center/trend', NULL, 'lucide:trending-up', '/analytics/trend/index', '', TRUE, NULL, '学习趋势折线图', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (130301, '趋势查看', 'analytics:trend:view', 'BUTTON', 1303, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查看学习趋势', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1304, '薄弱分析', 'AnalyticsWeak', 'MENU', 1505, 1, '/analytics-center/weak', NULL, 'lucide:alert-triangle', '/analytics/weak-points/list', '', TRUE, NULL, '薄弱知识点分析', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (130401, '薄弱查看', 'analytics:weak:view', 'BUTTON', 1304, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查看薄弱知识点', TRUE, '1', 1, 0, '0', '0');

-- ============================================
-- 7. AI助手 (1400)
-- ============================================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1400, 'AI助手', 'AiTutor', 'CATALOG', NULL, 1, '/ai-tutor', NULL, 'lucide:bot', '', '', TRUE, NULL, 'AI教学助手', TRUE, '1', 999, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1401, 'AI讲解', 'AiTutorTeacher', 'MENU', 1506, 1, '/ai-tutor/teacher', NULL, 'lucide:graduation-cap', '/ai-tutor/teacher/index', '', TRUE, NULL, 'TeacherAgent知识点讲解', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (140101, 'AI讲解', 'ai:teacher:execute', 'BUTTON', 1401, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '执行AI讲解', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1402, 'AI出题', 'AiTutorPractice', 'MENU', 1506, 1, '/ai-tutor/practice', NULL, 'lucide:pencil-ruler', '/ai-tutor/practice/index', '', TRUE, NULL, 'PracticeAgent智能出题', TRUE, '1', 2, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (140201, 'AI出题', 'ai:practice:execute', 'BUTTON', 1402, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '执行AI出题', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1403, 'AI规划', 'AiTutorPlanner', 'MENU', 1506, 1, '/ai-tutor/planner', NULL, 'lucide:route', '/ai-tutor/planner/index', '', TRUE, NULL, 'PlannerAgent学习路径规划', TRUE, '1', 3, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (140301, 'AI规划', 'ai:planner:execute', 'BUTTON', 1403, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '执行AI规划', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1404, 'AI对话', 'AiTutorChat', 'MENU', 1506, 1, '/ai-tutor/chat', NULL, 'lucide:message-circle', '/ai-tutor/chat/index', '', TRUE, NULL, '通用AI对话', TRUE, '1', 4, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (140401, 'AI对话', 'ai:chat:execute', 'BUTTON', 1404, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '执行AI对话', TRUE, '1', 1, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1405, 'AI报告', 'AiTutorReport', 'MENU', 1506, 1, '/ai-tutor/report', NULL, 'lucide:file-output', '/ai-tutor/report-gen/index', '', TRUE, NULL, 'ReportAgent生成学习报告', TRUE, '1', 5, 0, '0', '0');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (140501, 'AI报告', 'ai:report:execute', 'BUTTON', 1405, 1, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '生成AI报告', TRUE, '1', 1, 0, '0', '0');

-- ============================================
-- ============================================
-- ============================================
-- 9. super角色 (role_id=0) — 全部新菜单权限
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
-- 学习中心
(0, 0, 900, 1), (0, 0, 901, 1), (0, 0, 90101, 1), (0, 0, 90102, 1),
(0, 0, 902, 1), (0, 0, 90201, 1),
(0, 0, 903, 1), (0, 0, 90301, 1), (0, 0, 90302, 1),
(0, 0, 904, 1), (0, 0, 90401, 1),
-- 练习中心
(0, 0, 1000, 1), (0, 0, 1001, 1), (0, 0, 100101, 1), (0, 0, 100102, 1),
(0, 0, 1002, 1), (0, 0, 100201, 1), (0, 0, 100202, 1),
-- 考试中心
(0, 0, 1100, 1), (0, 0, 1101, 1), (0, 0, 110101, 1), (0, 0, 110102, 1), (0, 0, 110103, 1),
(0, 0, 1102, 1), (0, 0, 110201, 1),
-- 复习中心
(0, 0, 1200, 1), (0, 0, 1201, 1), (0, 0, 120101, 1), (0, 0, 120102, 1),
(0, 0, 1202, 1), (0, 0, 120201, 1),
-- 数据中心
(0, 0, 1300, 1), (0, 0, 1301, 1), (0, 0, 130101, 1),
(0, 0, 1302, 1), (0, 0, 130201, 1),
(0, 0, 1303, 1), (0, 0, 130301, 1),
(0, 0, 1304, 1), (0, 0, 130401, 1),
-- AI助手
(0, 0, 1400, 1), (0, 0, 1401, 1), (0, 0, 140101, 1),
(0, 0, 1402, 1), (0, 0, 140201, 1),
(0, 0, 1403, 1), (0, 0, 140301, 1),
(0, 0, 1404, 1), (0, 0, 140401, 1),
(0, 0, 1405, 1), (0, 0, 140501, 1);

-- ============================================
-- 10. admin角色 (role_id=1) — 全部新菜单权限 (同super)
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
-- 学习中心
(1, 0, 900, 1), (1, 0, 901, 1), (1, 0, 90101, 1), (1, 0, 90102, 1),
(1, 0, 902, 1), (1, 0, 90201, 1),
(1, 0, 903, 1), (1, 0, 90301, 1), (1, 0, 90302, 1),
(1, 0, 904, 1), (1, 0, 90401, 1),
-- 练习中心
(1, 0, 1000, 1), (1, 0, 1001, 1), (1, 0, 100101, 1), (1, 0, 100102, 1),
(1, 0, 1002, 1), (1, 0, 100201, 1), (1, 0, 100202, 1),
-- 考试中心
(1, 0, 1100, 1), (1, 0, 1101, 1), (1, 0, 110101, 1), (1, 0, 110102, 1), (1, 0, 110103, 1),
(1, 0, 1102, 1), (1, 0, 110201, 1),
-- 复习中心
(1, 0, 1200, 1), (1, 0, 1201, 1), (1, 0, 120101, 1), (1, 0, 120102, 1),
(1, 0, 1202, 1), (1, 0, 120201, 1),
-- 数据中心
(1, 0, 1300, 1), (1, 0, 1301, 1), (1, 0, 130101, 1),
(1, 0, 1302, 1), (1, 0, 130201, 1),
(1, 0, 1303, 1), (1, 0, 130301, 1),
(1, 0, 1304, 1), (1, 0, 130401, 1),
-- AI助手
(1, 0, 1400, 1), (1, 0, 1401, 1), (1, 0, 140101, 1),
(1, 0, 1402, 1), (1, 0, 140201, 1),
(1, 0, 1403, 1), (1, 0, 140301, 1),
(1, 0, 1404, 1), (1, 0, 140401, 1),
(1, 0, 1405, 1), (1, 0, 140501, 1);

-- ============================================
-- 11. teacher角色 (role_id=3) — 学习+练习+考试+复习+数据+AI+教务+知识
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
-- 仪表盘
(3, 0, 1, 1), (3, 0, 2, 1), (3, 0, 3, 1),
-- 学习中心 (全权限)
(3, 0, 900, 1), (3, 0, 901, 1), (3, 0, 90101, 1), (3, 0, 90102, 1),
(3, 0, 902, 1), (3, 0, 90201, 1),
(3, 0, 903, 1), (3, 0, 90301, 1), (3, 0, 90302, 1),
(3, 0, 904, 1), (3, 0, 90401, 1),
-- 练习中心 (全权限)
(3, 0, 1000, 1), (3, 0, 1001, 1), (3, 0, 100101, 1), (3, 0, 100102, 1),
(3, 0, 1002, 1), (3, 0, 100201, 1), (3, 0, 100202, 1),
-- 考试中心 (全权限)
(3, 0, 1100, 1), (3, 0, 1101, 1), (3, 0, 110101, 1), (3, 0, 110102, 1), (3, 0, 110103, 1),
(3, 0, 1102, 1), (3, 0, 110201, 1),
-- 复习中心 (全权限)
(3, 0, 1200, 1), (3, 0, 1201, 1), (3, 0, 120101, 1), (3, 0, 120102, 1),
(3, 0, 1202, 1), (3, 0, 120201, 1),
-- 数据中心 (全权限)
(3, 0, 1300, 1), (3, 0, 1301, 1), (3, 0, 130101, 1),
(3, 0, 1302, 1), (3, 0, 130201, 1),
(3, 0, 1303, 1), (3, 0, 130301, 1),
(3, 0, 1304, 1), (3, 0, 130401, 1),
-- AI助手 (全权限)
(3, 0, 1400, 1), (3, 0, 1401, 1), (3, 0, 140101, 1),
(3, 0, 1402, 1), (3, 0, 140201, 1),
(3, 0, 1403, 1), (3, 0, 140301, 1),
(3, 0, 1404, 1), (3, 0, 140401, 1),
(3, 0, 1405, 1), (3, 0, 140501, 1),
-- 知识引擎 (全权限)
(3, 0, 600, 1), (3, 0, 601, 1), (3, 0, 60101, 1), (3, 0, 60102, 1), (3, 0, 60103, 1), (3, 0, 60104, 1),
(3, 0, 602, 1), (3, 0, 603, 1),
(3, 0, 604, 1), (3, 0, 60401, 1), (3, 0, 60402, 1), (3, 0, 60403, 1), (3, 0, 60404, 1),
(3, 0, 605, 1),
-- 教育管理 (全权限)
(3, 0, 700, 1), (3, 0, 701, 1), (3, 0, 702, 1), (3, 0, 703, 1), (3, 0, 704, 1),
(3, 0, 705, 1), (3, 0, 706, 1), (3, 0, 707, 1), (3, 0, 708, 1), (3, 0, 709, 1),
(3, 0, 710, 1), (3, 0, 711, 1),
-- 日常记录
(3, 0, 400, 1), (3, 0, 401, 1), (3, 0, 40101, 1), (3, 0, 40102, 1), (3, 0, 40103, 1), (3, 0, 40104, 1),
(3, 0, 402, 1), (3, 0, 40201, 1), (3, 0, 40202, 1), (3, 0, 40203, 1), (3, 0, 40204, 1);

-- ============================================
-- 12. student角色 (role_id=4) — 学习+练习+考试+复习+数据+AI (无管理)
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
-- 仪表盘
(4, 0, 1, 1), (4, 0, 2, 1), (4, 0, 3, 1),
-- 学习中心 (浏览+学习，无管理)
(4, 0, 900, 1), (4, 0, 901, 1), (4, 0, 90101, 1), (4, 0, 90102, 1),
(4, 0, 902, 1), (4, 0, 90201, 1),
(4, 0, 903, 1), (4, 0, 90301, 1), (4, 0, 90302, 1),
(4, 0, 904, 1), (4, 0, 90401, 1),
-- 练习中心 (全权限)
(4, 0, 1000, 1), (4, 0, 1001, 1), (4, 0, 100101, 1), (4, 0, 100102, 1),
(4, 0, 1002, 1), (4, 0, 100201, 1), (4, 0, 100202, 1),
-- 考试中心 (查询+参加+交卷)
(4, 0, 1100, 1), (4, 0, 1101, 1), (4, 0, 110101, 1), (4, 0, 110102, 1), (4, 0, 110103, 1),
(4, 0, 1102, 1), (4, 0, 110201, 1),
-- 复习中心 (全权限)
(4, 0, 1200, 1), (4, 0, 1201, 1), (4, 0, 120101, 1), (4, 0, 120102, 1),
(4, 0, 1202, 1), (4, 0, 120201, 1),
-- 数据中心 (全权限)
(4, 0, 1300, 1), (4, 0, 1301, 1), (4, 0, 130101, 1),
(4, 0, 1302, 1), (4, 0, 130201, 1),
(4, 0, 1303, 1), (4, 0, 130301, 1),
(4, 0, 1304, 1), (4, 0, 130401, 1),
-- AI助手 (全权限)
(4, 0, 1400, 1), (4, 0, 1401, 1), (4, 0, 140101, 1),
(4, 0, 1402, 1), (4, 0, 140201, 1),
(4, 0, 1403, 1), (4, 0, 140301, 1),
(4, 0, 1404, 1), (4, 0, 140401, 1),
(4, 0, 1405, 1), (4, 0, 140501, 1),
-- 日常记录
(4, 0, 400, 1), (4, 0, 401, 1), (4, 0, 40101, 1), (4, 0, 40102, 1), (4, 0, 40103, 1), (4, 0, 40104, 1),
(4, 0, 402, 1), (4, 0, 40201, 1), (4, 0, 40202, 1), (4, 0, 40203, 1), (4, 0, 40204, 1);

-- ============================================
-- 13. parent角色 (role_id=5) — 仅仪表盘+数据中心+AI报告
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
-- 仪表盘
(5, 0, 1, 1), (5, 0, 2, 1), (5, 0, 3, 1),
-- 数据中心 (仅查看)
(5, 0, 1300, 1), (5, 0, 1301, 1), (5, 0, 130101, 1),
(5, 0, 1302, 1), (5, 0, 130201, 1),
(5, 0, 1303, 1), (5, 0, 130301, 1),
(5, 0, 1304, 1), (5, 0, 130401, 1),
-- AI助手 (仅AI报告)
(5, 0, 1400, 1), (5, 0, 1405, 1), (5, 0, 140501, 1);

-- ============================================
-- 14. user角色 (role_id=2) — 追加学习/练习/考试/复习/数据/AI (同student)
-- ============================================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`) VALUES
-- 学习中心
(2, 0, 900, 1), (2, 0, 901, 1), (2, 0, 90101, 1), (2, 0, 90102, 1),
(2, 0, 902, 1), (2, 0, 90201, 1),
(2, 0, 903, 1), (2, 0, 90301, 1), (2, 0, 90302, 1),
(2, 0, 904, 1), (2, 0, 90401, 1),
-- 练习中心
(2, 0, 1000, 1), (2, 0, 1001, 1), (2, 0, 100101, 1), (2, 0, 100102, 1),
(2, 0, 1002, 1), (2, 0, 100201, 1), (2, 0, 100202, 1),
-- 考试中心
(2, 0, 1100, 1), (2, 0, 1101, 1), (2, 0, 110101, 1), (2, 0, 110102, 1), (2, 0, 110103, 1),
(2, 0, 1102, 1), (2, 0, 110201, 1),
-- 复习中心
(2, 0, 1200, 1), (2, 0, 1201, 1), (2, 0, 120101, 1), (2, 0, 120102, 1),
(2, 0, 1202, 1), (2, 0, 120201, 1),
-- 数据中心
(2, 0, 1300, 1), (2, 0, 1301, 1), (2, 0, 130101, 1),
(2, 0, 1302, 1), (2, 0, 130201, 1),
(2, 0, 1303, 1), (2, 0, 130301, 1),
(2, 0, 1304, 1), (2, 0, 130401, 1),
-- AI助手
(2, 0, 1400, 1), (2, 0, 1401, 1), (2, 0, 140101, 1),
(2, 0, 1402, 1), (2, 0, 140201, 1),
(2, 0, 1403, 1), (2, 0, 140301, 1),
(2, 0, 1404, 1), (2, 0, 140401, 1),
(2, 0, 1405, 1), (2, 0, 140501, 1);
