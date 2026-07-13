-- ============================================
-- V013: 修复教育空间存量菜单数据
-- 旧数据: id=1501(学习) 1502(练习) 1503(考试) 1504(复习) 1505(教育管理) 1506(AI助手) 1507(数据中心)
-- 新数据: 20个扁平子菜单
-- ============================================

-- 1. 更新旧教育中心子菜单名称为新名称
--    1501: 学习 → 课程学习
UPDATE `menu` SET `name` = '课程学习', `code` = 'EduLearningCourse', `icon` = 'lucide:book', `component` = '/learning/course/list', `order` = 1
WHERE `id` = 1501 AND `name` = '学习';
--    1502: 练习 → 知识浏览
UPDATE `menu` SET `name` = '知识浏览', `code` = 'EduLearningKnowledge', `icon` = 'lucide:brain', `component` = '/learning/knowledge/list', `order` = 2
WHERE `id` = 1502 AND `name` = '练习';
--    1503: 考试 → 学习计划
UPDATE `menu` SET `name` = '学习计划', `code` = 'EduLearningPlan', `icon` = 'lucide:calendar-check', `component` = '/learning/plan/list', `order` = 3
WHERE `id` = 1503 AND `name` = '考试';

-- 2. 删除废弃的旧教育中心子菜单(1504-1507)
DELETE FROM `role_workspace_menu` WHERE `menu_id` IN (1504, 1505, 1506, 1507);
DELETE FROM `menu` WHERE `id` IN (1504, 1505, 1506, 1507);

-- 3. 更新教育空间名称(1500: 教育中心 → 教育空间)
UPDATE `menu` SET `name` = '教育空间', `code` = 'EducationCenter', `redirect` = '/learning/course', `order` = 20
WHERE `id` = 1500 AND `name` = '教育中心';

-- 4. 清理 menu 表中孤立引用(role_workspace_menu引用了已删除的menu)
DELETE FROM `role_workspace_menu` WHERE NOT EXISTS (SELECT 1 FROM `menu` m WHERE m.`id` = `role_workspace_menu`.`menu_id`);
