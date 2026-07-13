-- ============================================
-- V014: 将 EduAdmin（教育管理）合并为 EducationCenter（教育空间）的子目录
-- 原因：教育管理（学科/教材/章节/课程/题库/考试/学生/资源/计划/复习/学情/错题）
--       本质是教育空间的后台管理功能，不应作为独立顶级目录
-- ============================================

-- 1. 将 EduAdmin (id=40) 的 parent_id 改为 EducationCenter (id=1500)
--    使其成为教育空间下的子目录
UPDATE `menu`
SET `parent_id` = 1500,
    `order` = 20,
    `icon` = 'carbon:settings'
WHERE `id` = 40 AND `parent_id` IS NULL;

-- 2. 更新旧 EducationCenter 子菜单的排序，为 EduAdmin 让出位置
--    原 order 1-19 保持不变（学习/练习/考试/复习/AI助手/数据分析）
--    EduAdmin 使用 order=20 排在最后

