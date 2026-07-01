-- ============================================
-- Data: data_education
-- ============================================

INSERT INTO `subject` (`id`, `code`, `name`, `grade_level`, `icon`, `sort_order`, `status`) VALUES
(1, 'MATH',     '数学',   'K3', 'i-mdi:math-compass',        1, 1),
(2, 'PHYSICS',  '物理',   'K3', 'i-mdi:physics',            2, 1),
(3, 'ENGLISH',  '英语',   'K3', 'i-mdi:alphabetical',       3, 1),
(4, 'CHINESE',  '语文',   'K3', 'i-mdi:book-open-page-variant', 4, 1),
(5, 'CHEMISTRY','化学',   'K3', 'i-mdi:flask',              5, 1);

INSERT INTO `textbook` (`id`, `name`, `subject_code`, `grade`, `publisher`, `isbn`) VALUES
(1, '人教版数学七年级上册',   'MATH',    7, '人民教育出版社', '978-7-107-12345-6'),
(2, '人教版数学七年级下册',   'MATH',    7, '人民教育出版社', '978-7-107-12346-3'),
(3, '北师大版数学七年级上册', 'MATH',    7, '北京师范大学出版社', '978-7-303-12345-7'),
(4, '人教版物理八年级上册',   'PHYSICS', 8, '人民教育出版社', '978-7-107-23456-7'),
(5, '人教版英语七年级上册',   'ENGLISH', 7, '人民教育出版社', '978-7-107-34567-8'),
(6, '人教版化学九年级上册',   'CHEMISTRY',9, '人民教育出版社', '978-7-107-45678-9');

INSERT INTO `chapter` (`id`, `textbook_id`, `parent_id`, `name`, `chapter_order`) VALUES
-- 人教版数学七年级上册（教材ID=1）
(1,  1, NULL, '第一章 有理数',           1),
(2,  1, 1,    '1.1 正数和负数',          1),
(3,  1, 1,    '1.2 数轴',                2),
(4,  1, 1,    '1.3 相反数',              3),
(5,  1, 1,    '1.4 绝对值',              4),
(6,  1, 1,    '1.5 有理数的加减法',       5),
(7,  1, NULL, '第二章 整式的加减',        2),
(8,  1, NULL, '第三章 一元一次方程',      3),
-- 人教版物理八年级上册（教材ID=4）
(9,  4, NULL, '第一章 机械运动',          1),
(10, 4, 9,    '1.1 长度和时间的测量',     1),
(11, 4, 9,    '1.2 运动的描述',           2),
(12, 4, NULL, '第二章 声现象',            2),
(13, 4, 12,   '2.1 声音的产生与传播',     1),
(14, 4, 12,   '2.2 声音的特性',           2),
-- 人教版英语七年级上册（教材ID=5）
(15, 5, NULL, 'Starter 预备篇',           0),
(16, 5, NULL, 'Unit 1 My name is Gina.', 1),
(17, 5, NULL, 'Unit 2 This is my sister.',2),
-- 人教版化学九年级上册（教材ID=6）
(18, 6, NULL, '绪言 化学使世界变得更加绚丽多彩', 0),
(19, 6, NULL, '第一单元 走进化学世界',    1),
(20, 6, 19,   '课题1 物质的变化和性质',   1);

INSERT INTO `knowledge_textbook` (`knowledge_id`, `textbook_id`, `chapter_id`) VALUES
(1,  1, 2),    -- 自然数 → 1.1
(2,  1, 2),    -- 整数 → 1.1
(3,  1, 3),    -- 数轴 → 1.2
(4,  1, 4),    -- 相反数 → 1.3
(5,  1, 5),    -- 绝对值 → 1.4
(6,  1, 6);

INSERT INTO `teacher` (`id`, `user_id`, `teacher_no`, `name`, `subject`, `school`, `title`) VALUES
(1, 0, 'T2024001', '张老师', 'MATH',     '阳光中学', '高级教师'),
(2, 1, 'T2024002', '李老师', 'PHYSICS',  '阳光中学', '一级教师');

INSERT INTO `student` (`id`, `user_id`, `student_no`, `name`, `gender`, `grade`, `grade_level`, `school`, `class_name`, `learning_style`) VALUES
(1, 1, 'ST2024001', '小明', 1, 7, 'K3', '阳光中学', '初一(1)班', 'visual'),
(2, 2, 'ST2024002', '小红', 2, 7, 'K3', '阳光中学', '初一(2)班', 'auditory'),
(3, 0, 'ST2024003', '小华', 1, 8, 'K3', '阳光中学', '初二(1)班', 'kinesthetic');

INSERT INTO `course` (`id`, `name`, `description`, `subject_code`, `grade`, `textbook_id`, `teacher_id`, `total_hours`, `status`, `view_count`) VALUES
(1, '七年级数学上册精讲',    '人教版七年级数学上册系统学习，覆盖有理数、整式、方程等核心内容', 'MATH', 7, 1, 1, 48, 1, 1280),
(2, '有理数专题突破',        '深入理解有理数概念、数轴、相反数、绝对值等难点', 'MATH', 7, 1, 1, 16, 1, 560),
(3, '八年级物理入门',        '人教版物理八年级上册，机械运动与声现象', 'PHYSICS', 8, 4, 2, 32, 1, 340),
(4, '七年级英语同步精讲',    '人教版七年级英语上册同步辅导', 'ENGLISH', 7, 5, 1, 40, 1, 890),
(5, '化学奥秘探索',          '化学九年级上册入门课程', 'CHEMISTRY', 9, 6, 2, 24, 1, 120);

INSERT INTO `course_chapter` (`id`, `course_id`, `name`, `order_no`) VALUES
(1, 1, '有理数',          1),
(2, 1, '整式的加减',      2),
(3, 1, '一元一次方程',    3),
(4, 2, '正负数与数轴',    1),
(5, 2, '相反数与绝对值',  2),
(6, 2, '有理数运算',      3),
(7, 3, '机械运动',        1),
(8, 3, '声现象',          2),
(9, 4, 'Starter',         1),
(10, 4, 'Unit 1',         2);

INSERT INTO `course_section` (`id`, `chapter_id`, `name`, `order_no`, `duration_min`) VALUES
(1, 1, '1.1 正数和负数',      1, 30),
(2, 1, '1.2 数轴',            2, 25),
(3, 1, '1.3 相反数',          3, 20),
(4, 1, '1.4 绝对值',          4, 35),
(5, 4, '数轴的绘制与应用',      1, 20),
(6, 5, '相反数的性质',          1, 15),
(7, 7, '长度和时间的测量',      1, 30),
(8, 8, '声音的产生与传播',      1, 25);

INSERT INTO `course_knowledge` (`course_id`, `knowledge_id`, `section_id`, `sort_order`) VALUES
(1, 1, 1, 1), (1, 2, 1, 2), (1, 3, 2, 1), (1, 4, 3, 1), (1, 5, 4, 1), (1, 6, 4, 2),
(2, 3, 5, 1), (2, 4, 6, 1), (2, 5, 6, 2), (2, 6, 6, 3);

INSERT INTO `resource` (`id`, `name`, `type`, `url`, `size_bytes`, `duration_sec`, `subject_code`, `grade`, `difficulty`, `description`, `view_count`) VALUES
(1,  '有理数概念精讲视频',   'VIDEO',    '/edu/math/rational_numbers.mp4',    52428800, 1800, 'MATH', 7, 2, '人教版有理数章节详细讲解视频', 3500),
(2,  '数轴与绝对值PPT',      'PPT',     '/edu/math/numberline_abs.pptx',     10485760, NULL, 'MATH', 7, 2, '数轴绘制方法和绝对值性质的课件', 2100),
(3,  '相反数练习题集',       'PDF',     '/edu/math/opposite_exercises.pdf',   2097152, NULL, 'MATH', 7, 1, '相反数基础练习题含解析', 1800),
(4,  '七年级数学知识点总结',  'PDF',     '/edu/math/grade7_summary.pdf',       5242880, NULL, 'MATH', 7, 2, '全册知识点思维导图', 5600),
(5,  '有理数运算互动动画',   'ANIMATION','/edu/math/rational_ops.html',        NULL, 600,  'MATH', 7, 3, '可视化有理数加减法运算过程', 1200),
(6,  '机械运动趣味实验',     'VIDEO',   '/edu/physics/motion_exp.mp4',       73400320, 2400, 'PHYSICS', 8, 2, '测量物体运动的实验视频', 890),
(7,  '声现象科普动画',       'ANIMATION','/edu/physics/sound_waves.html',     NULL, 480,  'PHYSICS', 8, 1, '声音产生与传播的可视化动画', 670),
(8,  '英语 Starter 单词卡',  'PDF',     '/edu/english/starter_words.pdf',     1048576, NULL, 'ENGLISH', 7, 1, '预备单元核心单词表', 2100),
(9,  '化学实验安全指南',     'PDF',     '/edu/chemistry/lab_safety.pdf',      3145728, NULL, 'CHEMISTRY', 9, 1, '化学实验室安全操作规范', 450),
(10, '一元一次方程精讲',     'VIDEO',   '/edu/math/linear_equation.mp4',     41943040, 1500, 'MATH', 7, 2, '一元一次方程的解法步骤讲解', 2200),
(11, '英语 Unit1 对话音频',  'AUDIO',   '/edu/english/unit1_dialogue.mp3',    5242880, 600,  'ENGLISH', 7, 2, 'My name is Gina 单元对话音频', 3100),
(12, '长度测量工具介绍',     'PPT',     '/edu/physics/measure_tools.pptx',    8388608, NULL, 'PHYSICS', 8, 1, '长度测量工具使用方法的课件', 560);

INSERT INTO `resource_knowledge` (`resource_id`, `knowledge_id`, `sort_order`) VALUES
(1, 6, 1), (2, 3, 1), (2, 5, 2), (3, 4, 1),
(4, 1, 1), (4, 2, 2), (4, 3, 3), (4, 4, 4), (4, 5, 5), (4, 6, 6),
(5, 6, 1), (10, 6, 2);

INSERT INTO `edu_question` (`id`, `code`, `type`, `subject_code`, `grade`, `difficulty`, `ability_dimension`, `title`, `options`, `answer`, `analysis`, `tags`, `used_count`) VALUES
-- ===== 数学 - 基础 =====
(1, 'Q-MATH-001', 'CHOICE', 'MATH', 7, 1, 'remember', '-5 的相反数是？',
 '["A. 5", "B. -5", "C. 0", "D. 1/5"]', 'A',
 '只有符号不同的两个数互为相反数，-5 的相反数是 5。', '["相反数","基础"]', 120),
(2, 'Q-MATH-002', 'CHOICE', 'MATH', 7, 1, 'remember', '一个数的绝对值是它本身，这个数是？',
 '["A. 正数", "B. 负数", "C. 非负数", "D. 非正数"]', 'C',
 '正数和 0 的绝对值等于本身，即非负数。', '["绝对值","基础"]', 98),
(3, 'Q-MATH-003', 'FILL', 'MATH', 7, 1, 'remember', '| -8 | = ___', NULL, '8',
 '负数的绝对值等于它的相反数，|-8| = 8。', '["绝对值"]', 156),
-- ===== 数学 - 理解应用 =====
(4, 'Q-MATH-004', 'CHOICE', 'MATH', 7, 2, 'understand', '数轴上表示 -2 和 3 的两点之间的距离是？',
 '["A. 1", "B. 5", "C. -1", "D. -5"]', 'B',
 '数轴上两点距离 = |3-(-2)| = |5| = 5。', '["数轴","距离"]', 87),
(5, 'Q-MATH-005', 'FILL', 'MATH', 7, 2, 'understand', '若 |x| = 5，则 x = ___ 或 ___', NULL, '5,-5',
 '绝对值等于 5 的数有 5 和 -5 两个。', '["绝对值","方程"]', 134),
(6, 'Q-MATH-006', 'SOLVE', 'MATH', 7, 2, 'apply', '计算：| -3 | + | 2 | - | -1 |', NULL, '4',
 '|-3| = 3, |2| = 2, |-1| = 1，3 + 2 - 1 = 4。', '["绝对值","运算"]', 65),
(7, 'Q-MATH-009', 'CHOICE', 'MATH', 7, 2, 'understand', '下列哪个数不是有理数？',
 '["A. 3.14", "B. -2/3", "C. √2", "D. 0"]', 'C',
 '无理数是无限不循环小数，√2 是无理数，其他都是有理数。', '["有理数","概念辨析"]', 45),
-- ===== 数学 - 拔高 =====
(8, 'Q-MATH-007', 'CHOICE', 'MATH', 7, 3, 'analyze', '若 a < 0，b > 0，且 |a| > |b|，则 a + b 的值？',
 '["A. 正数", "B. 负数", "C. 0", "D. 无法确定"]', 'B',
 '|a| > |b| 说明 a 的绝对值更大，负数的绝对值大说明负数离原点更远，两数相加结果为负。', '["绝对值","比较大小"]', 42),
(9, 'Q-MATH-008', 'SOLVE', 'MATH', 7, 3, 'analyze', '已知 |a-1| + |b+2| = 0，求 a + b 的值。', NULL, '-1',
 '绝对值非负，和为 0 则每个都为 0。a-1=0→a=1；b+2=0→b=-2；a+b=-1。', '["绝对值","非负性"]', 55),
(10, 'Q-MATH-010', 'SOLVE', 'MATH', 7, 3, 'analyze', '比较大小：-3/4 和 -4/5，并说明理由。', NULL,
 '-3/4 > -4/5',
 '通分：-3/4 = -15/20，-4/5 = -16/20。因为 -15/20 > -16/20，所以 -3/4 > -4/5。',
 '["有理数","比较大小"]', 38),
-- ===== 数学 - 竞赛 =====
(11, 'Q-MATH-011', 'SOLVE', 'MATH', 7, 4, 'create', '若 |x-2| + |y+3| + |z-5| = 0，求 x+y+z 的值。', NULL,
 '4',
 '绝对值非负，和为 0 每个都为 0。x-2=0→x=2；y+3=0→y=-3；z-5=0→z=5；2+(-3)+5=4。',
 '["绝对值","非负性","竞赛"]', 22),
-- ===== 物理 =====
(12, 'Q-PHYS-001', 'CHOICE', 'PHYSICS', 8, 1, 'remember', '以下哪个是长度单位？',
 '["A. 千克", "B. 米", "C. 秒", "D. 安培"]', 'B',
 '米(m) 是国际单位制中的长度基本单位。', '["长度","单位"]', 200),
(13, 'Q-PHYS-002', 'CHOICE', 'PHYSICS', 8, 2, 'understand', '声音不能在以下哪种介质中传播？',
 '["A. 空气", "B. 水", "C. 真空", "D. 铁轨"]', 'C',
 '声音需要介质传播，真空不能传声。', '["声现象","介质"]', 178),
(14, 'Q-PHYS-003', 'FILL', 'PHYSICS', 8, 1, 'remember', '声音在空气中的传播速度约为 ___ m/s（15℃时）', NULL,
 '340',
 '在 15℃的空气中，声音的传播速度约为 340m/s。', '["声现象","速度"]', 156),
(15, 'Q-PHYS-004', 'CHOICE', 'PHYSICS', 8, 2, 'understand', '下列哪个措施是在声源处减弱噪声？',
 '["A. 戴耳塞", "B. 装隔音窗", "C. 禁止鸣笛", "D. 种树"]', 'C',
 '禁止鸣笛是在声源处阻止噪声产生，属于在声源处减弱噪声。', '["声现象","噪声控制"]', 89),
(16, 'Q-PHYS-005', 'SOLVE', 'PHYSICS', 8, 3, 'apply', '小明对着山崖喊话，0.6s 后听到回声，求小明距离山崖多远？（声速取 340m/s）', NULL,
 '102m',
 '声音往返总距离 = 340 × 0.6 = 204m，单程距离 = 204 ÷ 2 = 102m。', '["声现象","回声计算"]', 67),
-- ===== 英语 =====
(17, 'Q-ENG-001', 'CHOICE', 'ENGLISH', 7, 1, 'remember', 'What is the meaning of "sister"?',
 '["A. 兄弟", "B. 姐妹", "C. 朋友", "D. 老师"]', 'B',
 '"sister" 的意思是"姐妹"。', '["词汇","家庭"]', 230),
(18, 'Q-ENG-002', 'FILL', 'ENGLISH', 7, 1, 'remember', '"My name ___ Gina." (填 be 动词)', NULL,
 'is',
 'My name 是第三人称单数，用 is。', '["语法","be动词"]', 198),
(19, 'Q-ENG-003', 'CHOICE', 'ENGLISH', 7, 2, 'understand', '— Nice to meet you. — ______',
 '["A. Good morning", "B. Nice to meet you, too", "C. Thank you", "D. How are you"]', 'B',
 '对 "Nice to meet you." 的标准回答是 "Nice to meet you, too."', '["口语","问候"]', 176),
(20, 'Q-ENG-004', 'JUDGE', 'ENGLISH', 7, 2, 'understand', '"This are my books." 这个句子是否正确？', NULL,
 'FALSE',
 'This 是单数，应该用 is，正确的句子是 "These are my books." 或 "This is my book."',
 '["语法","主谓一致"]', 88);

INSERT INTO `edu_question_knowledge` (`question_id`, `knowledge_id`, `weight`) VALUES
(1, 4, 1.0), (2, 5, 1.0), (3, 5, 1.0),
(4, 3, 0.8), (4, 5, 0.2),
(5, 5, 1.0), (6, 5, 1.0), (7, 6, 1.0),
(8, 5, 0.6), (8, 6, 0.4),
(9, 5, 1.0), (10, 6, 1.0), (11, 5, 1.0);

INSERT INTO `exam` (`id`, `name`, `type`, `subject_code`, `grade`, `duration_min`, `total_score`, `status`, `teacher_id`) VALUES
(1, '七年级有理数单元测验',    'UNIT_TEST',  'MATH', 7, 45, 100, 1, 1),
(2, '期中考试 - 数学',         'MIDTERM',    'MATH', 7, 90, 100, 0, 1),
(3, '声现象小测验',            'DAILY_QUIZ', 'PHYSICS', 8, 20, 60, 1, 2),
(4, '英语 Unit 1 单元测试',    'UNIT_TEST',  'ENGLISH', 7, 40, 80, 0, 1);

INSERT INTO `exam_section` (`id`, `exam_id`, `name`, `order_no`, `score_per_q`) VALUES
(1, 1, '选择题（每题5分）', 1, 5.00),
(2, 1, '填空题（每题5分）', 2, 5.00),
(3, 1, '解答题（每题10分）',3, 10.00),
(4, 3, '选择题（每题6分）', 1, 6.00),
(5, 3, '填空题（每题6分）', 2, 6.00);

INSERT INTO `exam_question` (`exam_id`, `section_id`, `question_id`, `order_no`, `score`) VALUES
(1, 1, 1, 1, 5.00), (1, 1, 2, 2, 5.00), (1, 1, 4, 3, 5.00), (1, 1, 8, 4, 5.00),
(1, 2, 3, 1, 5.00), (1, 2, 5, 2, 5.00),
(1, 3, 6, 1, 10.00), (1, 3, 9, 2, 10.00),
(3, 4, 12, 1, 6.00), (3, 4, 13, 2, 6.00),
(3, 5, 14, 1, 6.00);

INSERT INTO `ability` (`id`, `student_id`, `knowledge_id`, `remember`, `understand`, `apply`, `analyze`, `evaluate`, `create_score`, `overall_mastery`) VALUES
-- 小明（studentId=1）：数学基础一般，绝对值偏弱
(1, 1, 1, 90, 85, 70, 60, 50, 40, 70.0),
(2, 1, 2, 85, 80, 75, 65, 55, 45, 71.5),
(3, 1, 3, 80, 75, 70, 60, 50, 40, 67.5),
(4, 1, 4, 75, 70, 65, 55, 45, 35, 63.0),
(5, 1, 5, 60, 55, 50, 40, 35, 30, 48.5),   -- 绝对值偏弱
(6, 1, 6, 70, 65, 60, 50, 40, 35, 57.5),
-- 小红（studentId=2）：学霸，全面优秀
(7,  2, 1, 95, 90, 85, 80, 70, 65, 83.5),
(8,  2, 2, 90, 88, 82, 78, 72, 68, 81.4),
(9,  2, 3, 88, 82, 78, 72, 65, 60, 77.2),
(10, 2, 4, 85, 80, 75, 68, 60, 55, 73.0),
(11, 2, 5, 82, 78, 72, 65, 58, 52, 70.4),
(12, 2, 6, 80, 75, 70, 62, 55, 50, 67.8),
-- 小华（studentId=3）：初二的物理新生
(13, 3, 1, 70, 60, 50, 40, 30, 20, 50.0),   -- 自然数（基础）
(14, 3, 2, 65, 55, 45, 35, 25, 20, 45.5);

INSERT INTO `edu_study_record` (`id`, `student_id`, `knowledge_id`, `record_type`, `question_id`, `score`, `accuracy`, `duration_sec`) VALUES
-- ===== 小明：学习绝对值，反复练习但进步缓慢 =====
(1,  1, 1, 'LEARN',    NULL,  NULL,   NULL,   1800),
(2,  1, 2, 'LEARN',    NULL,  NULL,   NULL,   1500),
(3,  1, 3, 'LEARN',    NULL,  NULL,   NULL,   1200),
(4,  1, 4, 'LEARN',    NULL,  NULL,   NULL,   900),
(5,  1, 5, 'LEARN',    NULL,  NULL,   NULL,   2400),
(6,  1, 5, 'PRACTICE', 3,    80,     0.80,   600),
(7,  1, 5, 'PRACTICE', 2,    60,     0.60,   300),
(8,  1, 4, 'PRACTICE', 1,    90,     0.90,   120),
(9,  1, 5, 'PRACTICE', 9,    40,     0.40,   480),   -- 难题做错
(10, 1, 5, 'REVIEW',   NULL,  NULL,   NULL,   600),
(11, 1, 5, 'PRACTICE', 6,    70,     0.70,   360),   -- 有进步
(12, 1, 3, 'PRACTICE', 4,    85,     0.85,   240),
-- ===== 小红：学习效率高，一次学会 =====
(13, 2, 1, 'LEARN',    NULL,  NULL,   NULL,   1200),
(14, 2, 2, 'LEARN',    NULL,  NULL,   NULL,   900),
(15, 2, 3, 'LEARN',    NULL,  NULL,   NULL,   800),
(16, 2, 4, 'LEARN',    NULL,  NULL,   NULL,   600),
(17, 2, 4, 'PRACTICE', 1,    100,    1.00,   60),
(18, 2, 5, 'LEARN',    NULL,  NULL,   NULL,   1500),
(19, 2, 5, 'PRACTICE', 3,    100,    1.00,   90),
(20, 2, 5, 'PRACTICE', 5,    90,     0.90,   180),
(21, 2, 5, 'PRACTICE', 11,   80,     0.80,   300),   -- 竞赛题也能做
(22, 2, 6, 'LEARN',    NULL,  NULL,   NULL,   1800),
(23, 2, 6, 'PRACTICE', 10,   95,     0.95,   240),
-- ===== 小华：学习物理 =====
(24, 3, 1, 'LEARN',    NULL,  NULL,   NULL,   1200),
(25, 3, 1, 'PRACTICE', 12,   80,     0.80,   180),
(26, 3, 2, 'LEARN',    NULL,  NULL,   NULL,   900);

INSERT INTO `edu_review_task` (`id`, `student_id`, `knowledge_id`, `review_date`, `review_round`, `status`, `result_score`, `completed_at`) VALUES
-- 小明 - 绝对值 复习任务（全部待完成）
(1,  1, 5, DATEADD('DAY', 1, CURRENT_DATE),  1, 'PENDING',   NULL, NULL),
(2,  1, 5, DATEADD('DAY', 3, CURRENT_DATE),  2, 'PENDING',   NULL, NULL),
(3,  1, 5, DATEADD('DAY', 7, CURRENT_DATE),  3, 'PENDING',   NULL, NULL),
(4,  1, 5, DATEADD('DAY', 15, CURRENT_DATE), 4, 'PENDING',   NULL, NULL),
(5,  1, 5, DATEADD('DAY', 30, CURRENT_DATE), 5, 'PENDING',   NULL, NULL),
(6,  1, 5, DATEADD('DAY', 90, CURRENT_DATE), 6, 'PENDING',   NULL, NULL),
-- 小红 - 绝对值 复习（已完成第一轮，第二轮待做）
(7,  2, 5, DATEADD('DAY', -3, CURRENT_DATE),  1, 'COMPLETED', 92, DATEADD('DAY', -3, CURRENT_TIMESTAMP)),
(8,  2, 5, DATEADD('DAY', -1, CURRENT_DATE),  2, 'OVERDUE',   NULL, NULL),
(9,  2, 5, DATEADD('DAY', 3, CURRENT_DATE),   3, 'PENDING',   NULL, NULL),
-- 小红 - 有理数 复习
(10, 2, 6, DATEADD('DAY', 1, CURRENT_DATE),  1, 'PENDING',   NULL, NULL),
(11, 2, 6, DATEADD('DAY', 3, CURRENT_DATE),  2, 'PENDING',   NULL, NULL),
-- 小明 - 相反数复习
(12, 1, 4, DATEADD('DAY', -2, CURRENT_DATE), 1, 'OVERDUE',   NULL, NULL),
(13, 1, 4, DATEADD('DAY', 4, CURRENT_DATE),  2, 'PENDING',   NULL, NULL),
(14, 1, 4, DATEADD('DAY', 8, CURRENT_DATE),  3, 'PENDING',   NULL, NULL);

INSERT INTO `edu_study_plan` (`id`, `student_id`, `target_knowledge_id`, `name`, `start_date`, `end_date`, `status`) VALUES
(1, 1, 8, '小明 — 二次函数突破计划',    CURRENT_DATE, DATEADD('DAY', 14, CURRENT_DATE), 'ACTIVE'),
(2, 2, 10, '小红 — 导数入门学习计划',  DATEADD('DAY', 7, CURRENT_DATE), DATEADD('DAY', 28, CURRENT_DATE), 'ACTIVE'),
(3, 1, 5, '小明 — 绝对值巩固计划',     DATEADD('DAY', -3, CURRENT_DATE), DATEADD('DAY', 11, CURRENT_DATE), 'ACTIVE');

INSERT INTO `edu_study_plan_item` (`id`, `plan_id`, `knowledge_id`, `plan_date`, `order_no`, `status`) VALUES
-- 小明二次函数计划：7(一次函数)→9(函数)→8(二次函数)
(1, 1, 7, CURRENT_DATE,                          1, 'IN_PROGRESS'),
(2, 1, 9, DATEADD('DAY', 3, CURRENT_DATE),       2, 'PENDING'),
(3, 1, 9, DATEADD('DAY', 5, CURRENT_DATE),       3, 'PENDING'),
(4, 1, 8, DATEADD('DAY', 7, CURRENT_DATE),       4, 'PENDING'),
(5, 1, 8, DATEADD('DAY', 10, CURRENT_DATE),      5, 'PENDING'),
(6, 1, 8, DATEADD('DAY', 12, CURRENT_DATE),      6, 'PENDING'),
-- 小红导数计划：8(二次函数)→10(导数)
(7,  2, 8,  DATEADD('DAY', 7, CURRENT_DATE),     1, 'PENDING'),
(8,  2, 8,  DATEADD('DAY', 10, CURRENT_DATE),    2, 'PENDING'),
(9,  2, 10, DATEADD('DAY', 17, CURRENT_DATE),    3, 'PENDING'),
(10, 2, 10, DATEADD('DAY', 21, CURRENT_DATE),    4, 'PENDING'),
-- 小明绝对值巩固计划
(11, 3, 5, DATEADD('DAY', -3, CURRENT_DATE),    1, 'COMPLETED'),
(12, 3, 5, DATEADD('DAY', -1, CURRENT_DATE),    2, 'COMPLETED'),
(13, 3, 5, DATEADD('DAY', 2, CURRENT_DATE),     3, 'PENDING'),
(14, 3, 5, DATEADD('DAY', 5, CURRENT_DATE),     4, 'PENDING'),
(15, 3, 5, DATEADD('DAY', 8, CURRENT_DATE),     5, 'PENDING'),
(16, 3, 5, DATEADD('DAY', 11, CURRENT_DATE),    6, 'PENDING');

INSERT INTO `wrong_question` (`id`, `student_id`, `question_id`, `knowledge_id`, `student_answer`, `correct_times`) VALUES
-- 小明错题
(1, 1, 8, 5, 'A', 0),      -- 绝对值比较大小选错了
(2, 1, 5, 5, '3', 1),      -- 绝对值方程漏解（连续正确1次）
(3, 1, 2, 5, 'A', 2),      -- 绝对值定义（已连续正确2次，即将出列）
(4, 1, 9, 5, '1', 0),      -- 非负性条件理解错误
(5, 1, 10, 6, '-3/4 < -4/5', 0),  -- 分数比较大小概念混淆
-- 小红错题（极少）
(6, 2, 11, 5, '2', 1);

