-- ============================================
-- 教育业务域扩展表初始化脚本
-- 使用 agent 数据源
-- ============================================

-- ============================================
-- 1. 学生表
-- ============================================
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL COMMENT '关联用户ID',
    `student_no`      VARCHAR(50)  DEFAULT NULL COMMENT '学号',
    `name`            VARCHAR(100) NOT NULL COMMENT '姓名',
    `gender`          TINYINT      DEFAULT NULL COMMENT '1=男 2=女',
    `birth_date`      DATE         DEFAULT NULL COMMENT '出生日期',
    `grade`           INT          NOT NULL COMMENT '年级 0-12',
    `grade_level`     VARCHAR(10)  DEFAULT NULL COMMENT '学段 K0/K1/K2/K3',
    `school`          VARCHAR(200) DEFAULT NULL COMMENT '学校',
    `class_name`      VARCHAR(50)  DEFAULT NULL COMMENT '班级',
    `parent_id`       BIGINT       DEFAULT NULL COMMENT '家长用户ID',
    `learning_style`  VARCHAR(20)  DEFAULT NULL COMMENT '学习风格 visual/auditory/kinesthetic',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_user` (`user_id`),
    KEY `idx_student_grade` (`grade`)
);
COMMENT ON TABLE `student` IS '学生表';

-- ============================================
-- 2. 教师表
-- ============================================
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL COMMENT '关联用户ID',
    `teacher_no`      VARCHAR(50)  DEFAULT NULL COMMENT '工号',
    `name`            VARCHAR(100) NOT NULL COMMENT '姓名',
    `subject`         VARCHAR(50)  DEFAULT NULL COMMENT '学科',
    `school`          VARCHAR(200) DEFAULT NULL COMMENT '学校',
    `title`           VARCHAR(100) DEFAULT NULL COMMENT '职称',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_teacher_user` (`user_id`)
);
COMMENT ON TABLE `teacher` IS '教师表';

-- ============================================
-- 3. 学科表
-- ============================================
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `code`            VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `name`            VARCHAR(50)  NOT NULL COMMENT '学科名称',
    `grade_level`     VARCHAR(10)  DEFAULT 'ALL' COMMENT '学段 K0/K1/K2/K3/ALL',
    `icon`            VARCHAR(200) DEFAULT NULL COMMENT '图标',
    `sort_order`      INT          DEFAULT 0 COMMENT '排序',
    `status`          TINYINT      DEFAULT 1 COMMENT '0=禁用 1=启用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_subject_code` (`code`)
);
COMMENT ON TABLE `subject` IS '学科表';

-- ============================================
-- 4. 课程表
-- ============================================
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(200) NOT NULL COMMENT '课程名称',
    `description`     TEXT         DEFAULT NULL COMMENT '课程描述',
    `subject_code`    VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `grade`           INT          DEFAULT NULL COMMENT '年级',
    `textbook_id`     BIGINT       DEFAULT NULL COMMENT '教材ID',
    `teacher_id`      BIGINT       DEFAULT NULL COMMENT '教师ID',
    `cover_url`       VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `total_hours`     INT          DEFAULT NULL COMMENT '总课时',
    `status`          TINYINT      DEFAULT 1 COMMENT '0=草稿 1=发布 2=下线',
    `view_count`      BIGINT       DEFAULT 0 COMMENT '浏览次数',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_course_subject` (`subject_code`),
    KEY `idx_course_textbook` (`textbook_id`)
);
COMMENT ON TABLE `course` IS '课程表';

-- ============================================
-- 5. 课程章节表
-- ============================================
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `course_id`       BIGINT       NOT NULL COMMENT '课程ID',
    `name`            VARCHAR(200) NOT NULL COMMENT '章节名称',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_course_chapter_course` (`course_id`)
);
COMMENT ON TABLE `course_chapter` IS '课程章节表';

-- ============================================
-- 6. 课程小节表
-- ============================================
DROP TABLE IF EXISTS `course_section`;
CREATE TABLE `course_section` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `chapter_id`      BIGINT       NOT NULL COMMENT '章节ID',
    `name`            VARCHAR(200) NOT NULL COMMENT '小节名称',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `content_url`     VARCHAR(500) DEFAULT NULL COMMENT '内容链接',
    `video_url`       VARCHAR(500) DEFAULT NULL COMMENT '视频链接',
    `duration_min`    INT          DEFAULT NULL COMMENT '时长(分钟)',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_course_section_chapter` (`chapter_id`)
);
COMMENT ON TABLE `course_section` IS '课程小节表';

-- ============================================
-- 7. 课程-知识点关联表
-- ============================================
DROP TABLE IF EXISTS `course_knowledge`;
CREATE TABLE `course_knowledge` (
    `course_id`       BIGINT       NOT NULL COMMENT '课程ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `section_id`      BIGINT       DEFAULT NULL COMMENT '小节ID',
    `sort_order`      INT          DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`course_id`, `knowledge_id`),
    KEY `idx_ck_knowledge` (`knowledge_id`)
);
COMMENT ON TABLE `course_knowledge` IS '课程-知识点关联表';

-- ============================================
-- 8. 资源表
-- ============================================
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(200) NOT NULL COMMENT '资源名称',
    `type`            VARCHAR(20)  NOT NULL COMMENT '类型 VIDEO/PDF/PPT/IMAGE/AUDIO/ANIMATION/QUIZ/EXPERIMENT/GAME',
    `url`             VARCHAR(500) NOT NULL COMMENT '资源链接',
    `size_bytes`      BIGINT       DEFAULT NULL COMMENT '文件大小(字节)',
    `duration_sec`    INT          DEFAULT NULL COMMENT '时长(秒)',
    `subject_code`    VARCHAR(20)  DEFAULT NULL COMMENT '学科编码',
    `grade`           INT          DEFAULT NULL COMMENT '年级',
    `difficulty`      TINYINT      DEFAULT NULL COMMENT '难度',
    `cover_url`       VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    `description`     TEXT         DEFAULT NULL COMMENT '描述',
    `status`          TINYINT      DEFAULT 1 COMMENT '0=禁用 1=启用',
    `view_count`      BIGINT       DEFAULT 0 COMMENT '浏览次数',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resource_subject` (`subject_code`)
);
COMMENT ON TABLE `resource` IS '资源表';

-- ============================================
-- 9. 资源-知识点关联表
-- ============================================
DROP TABLE IF EXISTS `resource_knowledge`;
CREATE TABLE `resource_knowledge` (
    `resource_id`     BIGINT       NOT NULL COMMENT '资源ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `sort_order`      INT          DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`resource_id`, `knowledge_id`),
    KEY `idx_rk_knowledge` (`knowledge_id`)
);
COMMENT ON TABLE `resource_knowledge` IS '资源-知识点关联表';

-- ============================================
-- 10. 题目表
-- ============================================
DROP TABLE IF EXISTS `edu_question`;
CREATE TABLE `edu_question` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `code`            VARCHAR(50)  DEFAULT NULL COMMENT '题目编号',
    `type`            VARCHAR(20)  NOT NULL COMMENT '类型 CHOICE/FILL/SOLVE/JUDGE/ESSAY/EXPERIMENT',
    `subject_code`    VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `grade`           INT          NOT NULL COMMENT '年级',
    `difficulty`      TINYINT      NOT NULL COMMENT '难度 1=基础 2=中等 3=困难 4=竞赛',
    `ability_dimension` VARCHAR(20) DEFAULT NULL COMMENT '能力维度 remember/understand/apply/analyze/evaluate/create',
    `title`           TEXT         NOT NULL COMMENT '题目内容',
    `options`         TEXT         DEFAULT NULL COMMENT '选项(JSON数组)',
    `answer`          TEXT         NOT NULL COMMENT '答案',
    `analysis`        TEXT         DEFAULT NULL COMMENT '解析',
    `source`          VARCHAR(200) DEFAULT NULL COMMENT '来源',
    `tags`            TEXT         DEFAULT NULL COMMENT '标签(JSON数组)',
    `status`          TINYINT      DEFAULT 1 COMMENT '0=禁用 1=启用',
    `used_count`      BIGINT       DEFAULT 0 COMMENT '使用次数',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_code` (`code`),
    KEY `idx_question_subject` (`subject_code`),
    KEY `idx_question_difficulty` (`difficulty`)
);
COMMENT ON TABLE `edu_question` IS '题目表';

-- ============================================
-- 11. 题目-知识点关联表
-- ============================================
DROP TABLE IF EXISTS `edu_question_knowledge`;
CREATE TABLE `edu_question_knowledge` (
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `weight`          DOUBLE       DEFAULT 1.0 COMMENT '权重',
    PRIMARY KEY (`question_id`, `knowledge_id`),
    KEY `idx_qk_knowledge` (`knowledge_id`)
);
COMMENT ON TABLE `edu_question_knowledge` IS '题目-知识点关联表';

-- ============================================
-- 12. 考试表
-- ============================================
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(200) NOT NULL COMMENT '考试名称',
    `type`            VARCHAR(20)  NOT NULL COMMENT '类型 DAILY_QUIZ/UNIT_TEST/MIDTERM/FINAL/MOCK/AI_GENERATED',
    `subject_code`    VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `grade`           INT          NOT NULL COMMENT '年级',
    `duration_min`    INT          NOT NULL COMMENT '时长(分钟)',
    `total_score`     INT          NOT NULL COMMENT '总分',
    `status`          TINYINT      DEFAULT 0 COMMENT '0=草稿 1=进行中 2=已结束',
    `teacher_id`      BIGINT       DEFAULT NULL COMMENT '教师ID',
    `start_time`      DATETIME     DEFAULT NULL COMMENT '开始时间',
    `end_time`        DATETIME     DEFAULT NULL COMMENT '结束时间',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_exam_subject` (`subject_code`)
);
COMMENT ON TABLE `exam` IS '考试表';

-- ============================================
-- 13. 考试大题表
-- ============================================
DROP TABLE IF EXISTS `exam_section`;
CREATE TABLE `exam_section` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_id`         BIGINT       NOT NULL COMMENT '考试ID',
    `name`            VARCHAR(100) NOT NULL COMMENT '大题名称',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `score_per_q`     DECIMAL(5,2) NOT NULL COMMENT '每题分值',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_exam_section_exam` (`exam_id`)
);
COMMENT ON TABLE `exam_section` IS '考试大题表';

-- ============================================
-- 14. 考试-题目关联表
-- ============================================
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_id`         BIGINT       NOT NULL COMMENT '考试ID',
    `section_id`      BIGINT       NOT NULL COMMENT '大题ID',
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `score`           DECIMAL(5,2) DEFAULT NULL COMMENT '实际得分',
    PRIMARY KEY (`id`),
    KEY `idx_eq_exam` (`exam_id`),
    KEY `idx_eq_section` (`section_id`),
    KEY `idx_eq_question` (`question_id`)
);
COMMENT ON TABLE `exam_question` IS '考试-题目关联表';

-- ============================================
-- 15. 学习记录表
-- ============================================
DROP TABLE IF EXISTS `edu_study_record`;
CREATE TABLE `edu_study_record` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `record_type`     VARCHAR(20)  NOT NULL COMMENT '类型 LEARN/PRACTICE/REVIEW/EXAM',
    `question_id`     BIGINT       DEFAULT NULL COMMENT '题目ID',
    `score`           DOUBLE       DEFAULT NULL COMMENT '得分',
    `accuracy`        DOUBLE       DEFAULT NULL COMMENT '正确率',
    `duration_sec`    INT          DEFAULT NULL COMMENT '时长(秒)',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sr_student` (`student_id`),
    KEY `idx_sr_knowledge` (`knowledge_id`)
);
COMMENT ON TABLE `edu_study_record` IS '学习记录表';

-- ============================================
-- 16. 复习任务表
-- ============================================
DROP TABLE IF EXISTS `edu_review_task`;
CREATE TABLE `edu_review_task` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `review_date`     DATE         NOT NULL COMMENT '复习日期',
    `review_round`    INT          NOT NULL COMMENT '复习轮次 1-6',
    `status`          VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态 PENDING/IN_REVIEW/COMPLETED/FAILED/OVERDUE',
    `result_score`    DOUBLE       DEFAULT NULL COMMENT '复习得分',
    `completed_at`    TIMESTAMP    NULL COMMENT '完成时间',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_rt_student_date` (`student_id`, `review_date`),
    KEY `idx_rt_knowledge` (`knowledge_id`)
);
COMMENT ON TABLE `edu_review_task` IS '复习任务表';

-- ============================================
-- 17. 学习计划表
-- ============================================
DROP TABLE IF EXISTS `edu_study_plan`;
CREATE TABLE `edu_study_plan` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`          BIGINT       NOT NULL COMMENT '学生ID',
    `target_knowledge_id` BIGINT       DEFAULT NULL COMMENT '目标知识点ID',
    `name`                VARCHAR(200) DEFAULT NULL COMMENT '计划名称',
    `start_date`          DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`            DATE         DEFAULT NULL COMMENT '结束日期',
    `status`              VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态 ACTIVE/COMPLETED/ABANDONED',
    `create_time`         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sp_student` (`student_id`)
);
COMMENT ON TABLE `edu_study_plan` IS '学习计划表';

-- ============================================
-- 18. 学习计划明细表
-- ============================================
DROP TABLE IF EXISTS `edu_study_plan_item`;
CREATE TABLE `edu_study_plan_item` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `plan_id`         BIGINT       NOT NULL COMMENT '计划ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `plan_date`       DATE         NOT NULL COMMENT '计划日期',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `status`          VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态 PENDING/IN_PROGRESS/COMPLETED/SKIPPED',
    `completed_at`    TIMESTAMP    NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    KEY `idx_spi_plan` (`plan_id`),
    KEY `idx_spi_knowledge` (`knowledge_id`)
);
COMMENT ON TABLE `edu_study_plan_item` IS '学习计划明细表';

-- ============================================
-- 19. 错题本表
-- ============================================
DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `knowledge_id`    BIGINT       DEFAULT NULL COMMENT '知识点ID',
    `student_answer`  TEXT         DEFAULT NULL COMMENT '学生答案',
    `correct_times`   INT          DEFAULT 0 COMMENT '连续正确次数',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_wq_student` (`student_id`),
    KEY `idx_wq_question` (`question_id`)
);
COMMENT ON TABLE `wrong_question` IS '错题本表';
