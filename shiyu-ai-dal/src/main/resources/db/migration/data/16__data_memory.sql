-- ============================================
-- Data: data_agent_supplement
-- ============================================

INSERT IGNORE INTO `conversation_message` (`id`, `session_id`, `user_id`, `agent_id`, `tenant_id`, `role`, `content`, `create_time`) VALUES
-- 小明（user_id=1）与 TeacherAgent 的数学对话
(1,  'session-math-001', 1, 'tutor-bot',       1, 'user',     '老师，什么是绝对值？能给我讲一下吗？',    '2026-06-28 09:00:00'),
(2,  'session-math-001', 1, 'tutor-bot',       1, 'assistant','绝对值就是一个数在数轴上到原点的距离，用符号 |a| 表示。比如 |-5| = 5，表示-5这个点到原点的距离是5个单位。', '2026-06-28 09:01:00'),
(3,  'session-math-001', 1, 'tutor-bot',       1, 'user',     '那 |3| 等于多少？', '2026-06-28 09:02:00'),
(4,  'session-math-001', 1, 'tutor-bot',       1, 'assistant','|3| = 3。正数的绝对值等于它本身，因为3到原点的距离就是3。', '2026-06-28 09:02:30'),
(5,  'session-math-001', 1, 'tutor-bot',       1, 'user',     '绝对值有什么性质呢？', '2026-06-28 09:03:00'),
(6,  'session-math-001', 1, 'tutor-bot',       1, 'assistant','绝对值有三个重要性质：\n1. 非负性：|a| ≥ 0\n2. 互为相反数的绝对值相等：|-a| = |a|\n3. |ab| = |a| × |b|', '2026-06-28 09:03:30'),
-- 小红（user_id=2）的物理学习对话
(7,  'session-phys-001', 2, 'knowledge-tutor', 1, 'user',     '声音是怎么产生的？', '2026-06-29 14:00:00'),
(8,  'session-phys-001', 2, 'knowledge-tutor', 1, 'assistant','声音是由物体振动产生的。比如敲击音叉，音叉的振动会引起周围空气的振动，形成声波传播到我们的耳朵。', '2026-06-29 14:01:00'),
(9,  'session-phys-001', 2, 'knowledge-tutor', 1, 'user',     '声音在真空中能传播吗？', '2026-06-29 14:02:00'),
(10, 'session-phys-001', 2, 'knowledge-tutor', 1, 'assistant','不能。声音的传播需要介质（固体、液体或气体），真空中没有介质，所以声音无法传播。月球上宇航员之间需要靠无线电通话就是这个原因。', '2026-06-29 14:02:30'),
-- 小华（user_id=0）的英语学习对话
(11, 'session-eng-001', 0, 'knowledge-tutor',  1, 'user',     '你能帮我练习英语自我介绍吗？', '2026-06-30 10:00:00'),
(12, 'session-eng-001', 0, 'knowledge-tutor',  1, 'assistant','当然可以！一个标准的自我介绍：\n\n"Hello everyone, my name is 小华.\nI am 13 years old.\nI am a student at Sunshine Middle School.\nNice to meet you!"\n\n你可以试着跟我练习！', '2026-06-30 10:01:00'),
(13, 'session-eng-001', 0, 'knowledge-tutor',  1, 'user',     '我试试：Hello, my name is Xiaohua. I am 13. Nice to meet you!', '2026-06-30 10:03:00'),
(14, 'session-eng-001', 0, 'knowledge-tutor',  1, 'assistant','非常好！发音基本正确。建议可以把 "I am 13" 改成 "I am 13 years old" 会更完整。继续加油！', '2026-06-30 10:03:30'),
-- 小明练习绝对值题目
(15, 'session-practice-001', 1, 'math-practice-bot', 1, 'user', '给我出几道绝对值的练习题吧', '2026-07-01 08:00:00'),
(16, 'session-practice-001', 1, 'math-practice-bot', 1, 'assistant','好的！请计算：\n1. | -9 | = ?\n2. | 0 | = ?\n3. 如果 |x| = 7，x 可能是多少？', '2026-07-01 08:00:30'),
(17, 'session-practice-001', 1, 'math-practice-bot', 1, 'user',     '1. 9  2. 0  3. 7 或 -7', '2026-07-01 08:01:00'),
(18, 'session-practice-001', 1, 'math-practice-bot', 1, 'assistant','全部正确！🎉 你掌握得很好。特别是第三题，能想到正负两个解，说明你对绝对值的概念理解到位了。', '2026-07-01 08:01:30');

INSERT IGNORE INTO `long_term_memory` (`id`, `user_id`, `agent_id`, `tenant_id`, `category`, `memory_key`, `content`, `importance`, `source`, `create_time`) VALUES
-- 小明（user_id=1）
(1, 1, 'tutor-bot',        1, 'learning_style',  'xiaoming_style',       '小明偏向视觉型学习，喜欢通过图形和数轴理解概念。绝对值概念偏弱，需要多练习。', 0.9, 'session-math-001', '2026-06-28 09:10:00'),
(2, 1, 'tutor-bot',        1, 'weakness',        'xiaoming_abs_weakness','绝对值比较大小、绝对值方程漏解是薄弱环节。建议多出这类题目。', 0.85, 'session-practice-001', '2026-07-01 08:05:00'),
(3, 1, 'math-practice-bot',1, 'practice_history','xiaoming_practice',    '小明已完成绝对值基础练习，准确率70%。需要加强 |a-b| 型题目的训练。', 0.8, 'session-practice-001', '2026-07-01 08:10:00'),
(4, 1, 'tutor-bot',        1, 'preference',      'xiaoming_interest',    '小明对数学应用题和趣味题感兴趣，可以适当穿插生活化的例子提高学习积极性。', 0.7, 'session-math-001', '2026-06-28 09:15:00'),
-- 小红（user_id=2）
(5, 2, 'knowledge-tutor',  1, 'learning_style',  'xiaohong_style',       '小红偏向听觉型学习，喜欢通过讲解和对话掌握知识。学习能力强，可以挑战高难度内容。', 0.9, 'session-phys-001', '2026-06-29 14:10:00'),
(6, 2, 'knowledge-tutor',  1, 'strength',        'xiaohong_strength',    '小红理解能力强，能快速掌握核心概念。物理声学部分已基本掌握，可以进入机械运动章节。', 0.85, 'session-phys-001', '2026-06-29 14:05:00'),
(7, 2, 'knowledge-tutor',  1, 'preference',      'xiaohong_interest',    '小红对科学实验和实际应用感兴趣，建议多提供实验视频和案例分析。', 0.75, 'session-phys-001', '2026-06-29 14:15:00'),
-- 小华（user_id=0）
(8, 0, 'knowledge-tutor',  1, 'learning_style',  'xiaohua_style',        '小华偏向动觉型学习，喜欢通过模仿和练习学习语言。英语基础一般，但学习主动性高。', 0.85, 'session-eng-001', '2026-06-30 10:10:00'),
(9, 0, 'knowledge-tutor',  1, 'weakness',        'xiaohua_eng_weakness', '小华英语口语流利度需加强，词汇量约200词。建议从日常对话入手提升。', 0.8, 'session-eng-001', '2026-06-30 10:08:00'),
(10, 0, 'knowledge-tutor', 1, 'preference',      'xiaohua_goal',         '小华目标是能进行简单的英语日常对话，侧重听说能力的培养。', 0.7, 'session-eng-001', '2026-06-30 10:12:00');

INSERT IGNORE INTO `agent_execution` (`id`, `execution_id`, `agent_id`, `version`, `user_id`, `session_id`, `tenant_id`, `node_id`, `node_type`, `input_data`, `output_data`, `status`, `error_message`, `start_time`, `end_time`, `duration_ms`) VALUES
-- 小明 tutor-bot 执行：teach 节点
(1,  'exec-001-aaaa', 'tutor-bot',  'v1.0.0', 1, 'session-math-001', 1, 'abilityQuery', 'TRANSFORM',
 '{"studentId":1,"knowledgeId":5,"query":"什么是绝对值？"}',
 '{"success":true,"knowledge":{"id":5,"name":"绝对值"},"ability":{"overallScore":48.5}}',
 'SUCCESS', NULL, '2026-06-28 09:00:00', '2026-06-28 09:00:02', 120),
(2,  'exec-001-bbbb', 'tutor-bot',  'v1.0.0', 1, 'session-math-001', 1, 'teach', 'LLM_CALL',
 '{"prompt":"讲解绝对值概念","studentId":1,"knowledgeId":5}',
 '{"success":true,"teachContent":"绝对值就是一个数在数轴上到原点的距离..."}',
 'SUCCESS', NULL, '2026-06-28 09:00:02', '2026-06-28 09:00:08', 5800),
-- 小红 knowledge-tutor 执行
(3,  'exec-002-aaaa', 'knowledge-tutor', 'v1.0.0', 2, 'session-phys-001', 1, 'abilityQuery', 'TRANSFORM',
 '{"studentId":2,"knowledgeId":1,"query":"声音是怎么产生的？"}',
 '{"success":true,"knowledge":{"id":1,"name":"自然数"},"ability":{"overallScore":83.5}}',
 'SUCCESS', NULL, '2026-06-29 14:00:00', '2026-06-29 14:00:01', 80),
(4,  'exec-002-bbbb', 'knowledge-tutor', 'v1.0.0', 2, 'session-phys-001', 1, 'teach', 'LLM_CALL',
 '{"prompt":"讲解声音的产生与传播","studentId":2,"knowledgeId":1}',
 '{"success":true,"teachContent":"声音是由物体振动产生的..."}',
 'SUCCESS', NULL, '2026-06-29 14:00:01', '2026-06-29 14:00:07', 5200),
-- 小明 math-practice-bot 执行
(5,  'exec-003-aaaa', 'math-practice-bot', 'v1.0.0', 1, 'session-practice-001', 1, 'abilityQuery', 'TRANSFORM',
 '{"studentId":1,"knowledgeId":5,"query":"出几道练习题","count":3}',
 '{"success":true,"knowledge":{"id":5,"name":"绝对值"},"overallScore":48.5}',
 'SUCCESS', NULL, '2026-07-01 08:00:00', '2026-07-01 08:00:02', 150),
(6,  'exec-003-bbbb', 'math-practice-bot', 'v1.0.0', 1, 'session-practice-001', 1, 'practice', 'LLM_CALL',
 '{"prompt":"生成绝对值练习题","difficultyName":"中等","practiceCount":3}',
 '{"success":true,"questionCount":3,"practiceDone":true}',
 'SUCCESS', NULL, '2026-07-01 08:00:02', '2026-07-01 08:00:07', 4800),
-- tutor-bot 完整流程（含失败记录）
(7,  'exec-004-aaaa', 'tutor-bot',  'v1.0.0', 1, 'session-math-002', 1, 'scoreAnalysis', 'TRANSFORM',
 '{"practiceScore":85,"practiceAccuracy":0.85,"studentId":1,"knowledgeId":5}',
 '{"success":true,"score":85,"reviewNeeded":false}',
 'SUCCESS', NULL, '2026-06-30 15:00:00', '2026-06-30 15:00:01', 80),
(8,  'exec-004-bbbb', 'tutor-bot',  'v1.0.0', 1, 'session-math-002', 1, 'reviewSchedule', 'TRANSFORM',
 '{"studentId":1,"knowledgeId":5}',
 '{"success":true,"reviewCount":6}',
 'SUCCESS', NULL, '2026-06-30 15:00:01', '2026-06-30 15:00:02', 50),
-- 小华英语学习
(9,  'exec-005-aaaa', 'knowledge-tutor', 'v1.0.0', 0, 'session-eng-001', 1, 'teach', 'LLM_CALL',
 '{"prompt":"英语自我介绍练习","studentId":0}',
 '{"success":true,"teachContent":"当然可以！一个标准的自我介绍..."}',
 'SUCCESS', NULL, '2026-06-30 10:00:00', '2026-06-30 10:00:06', 5500),
-- 小红的知识点搜索执行（含失败）
(10, 'exec-006-aaaa', 'knowledge-tutor', 'v1.0.0', 2, 'session-phys-002', 1, 'prereqCheck', 'TRANSFORM',
 '{"studentId":2,"knowledgeId":99}',
 '{"success":false,"msg":"知识点不存在: 99"}',
 'FAILED', '知识点ID 99 不存在', '2026-06-29 15:00:00', '2026-06-29 15:00:00', 10);

