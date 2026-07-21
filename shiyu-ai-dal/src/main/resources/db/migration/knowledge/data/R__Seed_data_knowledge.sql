-- ============================================
-- Data: knowledge — 知识点/关系/文档
-- ============================================

-- 知识点
INSERT IGNORE INTO `knowledge` (`id`, `code`, `name`, `description`, `difficulty`, `category`, `tags`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 'math_natural', '自然数', '用来表示物体个数的数: 0,1,2,3,...', 1, 'MATH', '["自然数","初等数学"]', 1, 1, 'system', 'system'),
(2, 'math_integer', '整数', '正整数、零和负整数的统称', 1, 'MATH', '["整数"]', 1, 1, 'system', 'system'),
(3, 'math_numberline', '数轴', '规定了原点、正方向和单位长度的直线', 2, 'MATH', '["数轴"]', 1, 1, 'system', 'system'),
(4, 'math_opposite', '相反数', '只有符号不同的两个数互为相反数', 2, 'MATH', '["相反数"]', 1, 1, 'system', 'system'),
(5, 'math_absval', '绝对值', '一个数在数轴上对应的点到原点的距离', 2, 'MATH', '["绝对值"]', 1, 1, 'system', 'system'),
(6, 'math_rational', '有理数', '整数和分数的统称', 2, 'MATH', '["有理数"]', 1, 1, 'system', 'system'),
(7, 'math_linear_fn', '一次函数', 'y=kx+b (k≠0) 形式的函数', 3, 'MATH', '["一次函数"]', 1, 1, 'system', 'system'),
(8, 'math_quad_fn', '二次函数', 'y=ax²+bx+c (a≠0) 形式的函数', 3, 'MATH', '["二次函数"]', 1, 1, 'system', 'system'),
(9, 'math_function', '函数', '两个变量之间的对应关系', 3, 'MATH', '["函数"]', 1, 1, 'system', 'system'),
(10, 'math_derivative', '导数', '函数在某一点的变化率', 4, 'MATH', '["导数","高等数学"]', 1, 1, 'system', 'system'),
(11, 'phys_force', '力', '物体之间的相互作用', 2, 'PHYS', '["力"]', 1, 1, 'system', 'system'),
(12, 'phys_motion', '运动', '物体位置随时间的变化', 2, 'PHYS', '["运动"]', 1, 1, 'system', 'system'),
(13, 'eng_verb_tense', '动词时态', '英语动词的时态变化', 2, 'ENG', '["时态"]', 1, 1, 'system', 'system');

-- 知识点关系
INSERT IGNORE INTO `knowledge_relation` (`id`, `source_id`, `target_id`, `relation_type`, `weight`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 2, 1, 'PRE', 1.0, 1, 1, 'system', 'system'),
(2, 3, 2, 'PRE', 1.0, 1, 1, 'system', 'system'),
(3, 4, 3, 'PRE', 1.0, 1, 1, 'system', 'system'),
(4, 5, 3, 'PRE', 1.0, 1, 1, 'system', 'system'),
(5, 6, 2, 'PRE', 1.0, 1, 1, 'system', 'system'),
(6, 7, 9, 'PRE', 1.0, 1, 1, 'system', 'system'),
(7, 8, 9, 'PRE', 1.0, 1, 1, 'system', 'system'),
(8, 9, 10, 'PRE', 1.0, 1, 1, 'system', 'system');

-- 知识文档
INSERT IGNORE INTO `knowledge_document` (`id`, `title`, `content`, `doc_type`, `source`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, '自然数概念', '自然数是指用以计量事物的件数或表示事物次序的数。即用数码0,1,2,3,4,……所表示的数。', 'ARTICLE', '人教版数学七年级上册', 1, 1, 'system', 'system'),
(2, '绝对值的定义', '绝对值是一个数在数轴上对应的点到原点的距离，用符号|a|表示。', 'ARTICLE', '人教版数学七年级上册', 1, 1, 'system', 'system');

-- 文档-知识点关联
INSERT IGNORE INTO `knowledge_doc_relation` (`doc_id`, `knowledge_id`, `relation_type`) VALUES
(1, 1, 'RELATED'),
(2, 5, 'RELATED');
