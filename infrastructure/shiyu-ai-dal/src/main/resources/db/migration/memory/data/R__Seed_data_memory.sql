-- ============================================
-- Data: memory — 对话消息/长期记忆
-- ============================================

-- 对话消息
INSERT IGNORE INTO `memory_conversation_message` (`id`, `session_id`, `user_id`, `agent_id`, `tenant_id`, `role`, `content`, `create_time`) VALUES
(1,  'session-math-001', 4, 'tutor-bot',       1, 'user',     '老师，什么是绝对值？能给我讲一下吗？',  '2026-06-28 09:00:00'),
(2,  'session-math-001', 4, 'tutor-bot',       1, 'assistant','绝对值就是一个数在数轴上到原点的距离，用符号 |a| 表示。比如 |-5| = 5。', '2026-06-28 09:01:00'),
(3,  'session-math-001', 4, 'tutor-bot',       1, 'user',     '绝对值有什么性质呢？', '2026-06-28 09:03:00'),
(4,  'session-math-001', 4, 'tutor-bot',       1, 'assistant','绝对值有三个重要性质：\n1. 非负性：|a| ≥ 0\n2. 互为相反数的绝对值相等：|-a| = |a|\n3. |ab| = |a| × |b|', '2026-06-28 09:03:30'),
(5,  'session-phys-001', 4, 'knowledge-tutor', 1, 'user',     '声音是怎么产生的？', '2026-06-29 14:00:00'),
(6,  'session-phys-001', 4, 'knowledge-tutor', 1, 'assistant','声音是由物体振动产生的。比如敲击音叉，音叉的振动会引起周围空气的振动，形成声波传播到我们的耳朵。', '2026-06-29 14:01:00'),
(7,  'session-phys-001', 4, 'knowledge-tutor', 1, 'user',     '声音在真空中能传播吗？', '2026-06-29 14:02:00'),
(8,  'session-phys-001', 4, 'knowledge-tutor', 1, 'assistant','不能。声音的传播需要介质，真空中没有介质，所以声音无法传播。', '2026-06-29 14:02:30');

-- 长期记忆
INSERT IGNORE INTO `memory_long_term_memory` (`id`, `user_id`, `agent_id`, `tenant_id`, `category`, `memory_key`, `content`, `importance`, `create_by`, `update_by`) VALUES
(1, 4, 'tutor-bot', 1, 'learning', 'math_abs_progress', '学生正在学习绝对值，理解程度中等', 0.7, 'system', 'system'),
(2, 4, 'knowledge-tutor', 1, 'preference', 'learning_style', '学生偏好直观的举例说明', 0.5, 'system', 'system');
