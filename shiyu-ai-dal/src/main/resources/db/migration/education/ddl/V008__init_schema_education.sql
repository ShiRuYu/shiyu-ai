-- ============================================
-- Schema: schema_education
-- ============================================


CREATE TABLE IF NOT EXISTS `edu_ability`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `remember`        DOUBLE       DEFAULT 0 COMMENT '记忆',
    `understand`      DOUBLE       DEFAULT 0 COMMENT '理解',
    `apply`           DOUBLE       DEFAULT 0 COMMENT '应用',
    `analyze`         DOUBLE       DEFAULT 0 COMMENT '分析',
    `evaluate`        DOUBLE       DEFAULT 0 COMMENT '评价',
    `create_score`    DOUBLE       DEFAULT 0 COMMENT '创造',
    `overall_mastery` DOUBLE       DEFAULT 0 COMMENT '总体掌握度',
    `last_update`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ability_student_knowledge` (`student_id`, `knowledge_id`)
);

COMMENT ON TABLE `edu_ability` IS '能力值表';


CREATE TABLE IF NOT EXISTS `edu_textbook`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(128) NOT NULL COMMENT '教材名称',
    `subject_code`    VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `grade`           INT          NOT NULL COMMENT '年级',
    `publisher`       VARCHAR(64)  NOT NULL COMMENT '出版社(人教版/北师大版等)',
    `isbn`            VARCHAR(32)  DEFAULT NULL COMMENT 'ISBN',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

COMMENT ON TABLE `edu_textbook` IS '教材版本表';


CREATE TABLE IF NOT EXISTS `edu_chapter`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `textbook_id`     BIGINT       NOT NULL COMMENT '教材ID',
    `parent_id`       BIGINT       DEFAULT NULL COMMENT '父章节ID',
    `name`            VARCHAR(128) NOT NULL COMMENT '章节名称',
    `chapter_order`   INT          DEFAULT 0 COMMENT '排序',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_chapter_textbook` (`textbook_id`),
    KEY `idx_chapter_parent` (`parent_id`)
);

COMMENT ON TABLE `edu_chapter` IS '章节表';


CREATE TABLE IF NOT EXISTS `edu_knowledge_textbook`(
    `id`              BIGINT NOT NULL AUTO_INCREMENT,
    `knowledge_id`    BIGINT NOT NULL COMMENT '知识点ID',
    `textbook_id`     BIGINT NOT NULL COMMENT '教材ID',
    `chapter_id`      BIGINT DEFAULT NULL COMMENT '章节ID',
    `tenant_id`       BIGINT NOT NULL COMMENT '租户ID',
    `status`          TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64) COMMENT '创建者',
    `create_time`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64) COMMENT '更新者',
    `update_time`     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_kt_knowledge_textbook` (`knowledge_id`, `textbook_id`)
);

COMMENT ON TABLE `edu_knowledge_textbook` IS '知识点-教材章节关联表';


CREATE TABLE IF NOT EXISTS `edu_student`(
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
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_user` (`user_id`),
    KEY `idx_student_grade` (`grade`)
);

COMMENT ON TABLE `edu_student` IS '学生表';


CREATE TABLE IF NOT EXISTS `edu_teacher`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL COMMENT '关联用户ID',
    `teacher_no`      VARCHAR(50)  DEFAULT NULL COMMENT '工号',
    `name`            VARCHAR(100) NOT NULL COMMENT '姓名',
    `subject`         VARCHAR(50)  DEFAULT NULL COMMENT '学科',
    `school`          VARCHAR(200) DEFAULT NULL COMMENT '学校',
    `title`           VARCHAR(100) DEFAULT NULL COMMENT '职称',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_teacher_user` (`user_id`)
);

COMMENT ON TABLE `edu_teacher` IS '教师表';


CREATE TABLE IF NOT EXISTS `edu_subject`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `code`            VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `name`            VARCHAR(50)  NOT NULL COMMENT '学科名称',
    `grade_level`     VARCHAR(10)  DEFAULT 'ALL' COMMENT '学段 K0/K1/K2/K3/ALL',
    `icon`            VARCHAR(200) DEFAULT NULL COMMENT '图标',
    `sort_order`      INT          DEFAULT 0 COMMENT '排序',
    `status`          TINYINT      DEFAULT 1 COMMENT '0=禁用 1=启用',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_subject_code` (`code`)
);

COMMENT ON TABLE `edu_subject` IS '学科表';


CREATE TABLE IF NOT EXISTS `edu_course`(
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
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_subject` (`subject_code`),
    KEY `idx_course_textbook` (`textbook_id`)
);

COMMENT ON TABLE `edu_course` IS '课程表';


CREATE TABLE IF NOT EXISTS `edu_course_chapter`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `course_id`       BIGINT       NOT NULL COMMENT '课程ID',
    `name`            VARCHAR(200) NOT NULL COMMENT '章节名称',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_chapter_course` (`course_id`)
);

COMMENT ON TABLE `edu_course_chapter` IS '课程章节表';


CREATE TABLE IF NOT EXISTS `edu_course_section`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `chapter_id`      BIGINT       NOT NULL COMMENT '章节ID',
    `name`            VARCHAR(200) NOT NULL COMMENT '小节名称',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `content_url`     VARCHAR(500) DEFAULT NULL COMMENT '内容链接',
    `video_url`       VARCHAR(500) DEFAULT NULL COMMENT '视频链接',
    `duration_min`    INT          DEFAULT NULL COMMENT '时长(分钟)',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_section_chapter` (`chapter_id`)
);

COMMENT ON TABLE `edu_course_section` IS '课程小节表';


CREATE TABLE IF NOT EXISTS `edu_course_knowledge`(
    `course_id`       BIGINT       NOT NULL COMMENT '课程ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `section_id`      BIGINT       DEFAULT NULL COMMENT '小节ID',
    `sort_order`      INT          DEFAULT 0 COMMENT '排序',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`course_id`, `knowledge_id`),
    KEY `idx_ck_knowledge` (`knowledge_id`)
);

COMMENT ON TABLE `edu_course_knowledge` IS '课程-知识点关联表';


CREATE TABLE IF NOT EXISTS `edu_resource`(
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
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_resource_subject` (`subject_code`)
);

COMMENT ON TABLE `edu_resource` IS '资源表';


CREATE TABLE IF NOT EXISTS `edu_resource_knowledge`(
    `resource_id`     BIGINT       NOT NULL COMMENT '资源ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `sort_order`      INT          DEFAULT 0 COMMENT '排序',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`resource_id`, `knowledge_id`),
    KEY `idx_rk_knowledge` (`knowledge_id`)
);

COMMENT ON TABLE `edu_resource_knowledge` IS '资源-知识点关联表';


CREATE TABLE IF NOT EXISTS `edu_question`(
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
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_code` (`code`),
    KEY `idx_question_subject` (`subject_code`),
    KEY `idx_question_difficulty` (`difficulty`)
);

COMMENT ON TABLE `edu_question` IS '题目表';


CREATE TABLE IF NOT EXISTS `edu_question_knowledge`(
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `weight`          DOUBLE       DEFAULT 1.0 COMMENT '权重',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`question_id`, `knowledge_id`),
    KEY `idx_qk_knowledge` (`knowledge_id`)
);

COMMENT ON TABLE `edu_question_knowledge` IS '题目-知识点关联表';


CREATE TABLE IF NOT EXISTS `edu_exam`(
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
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_exam_subject` (`subject_code`)
);

COMMENT ON TABLE `edu_exam` IS '考试表';


CREATE TABLE IF NOT EXISTS `edu_exam_section`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_id`         BIGINT       NOT NULL COMMENT '考试ID',
    `name`            VARCHAR(100) NOT NULL COMMENT '大题名称',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `score_per_q`     DECIMAL(5,2) NOT NULL COMMENT '每题分值',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_exam_section_exam` (`exam_id`)
);

COMMENT ON TABLE `edu_exam_section` IS '考试大题表';


CREATE TABLE IF NOT EXISTS `edu_exam_question`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_id`         BIGINT       NOT NULL COMMENT '考试ID',
    `section_id`      BIGINT       NOT NULL COMMENT '大题ID',
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `score`           DECIMAL(5,2) DEFAULT NULL COMMENT '实际得分',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_eq_exam` (`exam_id`),
    KEY `idx_eq_section` (`section_id`),
    KEY `idx_eq_question` (`question_id`)
);

COMMENT ON TABLE `edu_exam_question` IS '考试-题目关联表';


CREATE TABLE IF NOT EXISTS `edu_study_record`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `record_type`     VARCHAR(20)  NOT NULL COMMENT '类型 LEARN/PRACTICE/REVIEW/EXAM',
    `question_id`     BIGINT       DEFAULT NULL COMMENT '题目ID',
    `score`           DOUBLE       DEFAULT NULL COMMENT '得分',
    `accuracy`        DOUBLE       DEFAULT NULL COMMENT '正确率',
    `duration_sec`    INT          DEFAULT NULL COMMENT '时长(秒)',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sr_student` (`student_id`),
    KEY `idx_sr_knowledge` (`knowledge_id`)
);

COMMENT ON TABLE `edu_study_record` IS '学习记录表';


CREATE TABLE IF NOT EXISTS `edu_review_task`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `review_date`     DATE         NOT NULL COMMENT '复习日期',
    `review_round`    INT          NOT NULL COMMENT '复习轮次 1-6',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态(0待复习 1复习中 2已完成 3未通过 4已过期)',
    `result_score`    DOUBLE       DEFAULT NULL COMMENT '复习得分',
    `completed_at`    TIMESTAMP    NULL COMMENT '完成时间',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_rt_student_date` (`student_id`, `review_date`),
    KEY `idx_rt_knowledge` (`knowledge_id`)
);

COMMENT ON TABLE `edu_review_task` IS '复习任务表';


CREATE TABLE IF NOT EXISTS `edu_study_plan`(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`          BIGINT       NOT NULL COMMENT '学生ID',
    `target_knowledge_id` BIGINT       DEFAULT NULL COMMENT '目标知识点ID',
    `name`                VARCHAR(200) DEFAULT NULL COMMENT '计划名称',
    `start_date`          DATE         DEFAULT NULL COMMENT '开始日期',
    `end_date`            DATE         DEFAULT NULL COMMENT '结束日期',
    `status`              TINYINT      DEFAULT 0 COMMENT '状态(0进行中 1已完成 2已放弃)',
    `create_time`         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time`         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id`     BIGINT       NOT NULL COMMENT '租户ID',
    `create_by`     VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`     VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `del_flag`      TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_sp_student` (`student_id`)
);

COMMENT ON TABLE `edu_study_plan` IS '学习计划表';


CREATE TABLE IF NOT EXISTS `edu_study_plan_item`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `plan_id`         BIGINT       NOT NULL COMMENT '计划ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `plan_date`       DATE         NOT NULL COMMENT '计划日期',
    `order_no`        INT          NOT NULL COMMENT '排序',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态(0待处理 1进行中 2已完成 3已跳过)',
    `completed_at`    TIMESTAMP    NULL COMMENT '完成时间',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_spi_plan` (`plan_id`),
    KEY `idx_spi_knowledge` (`knowledge_id`)
);

COMMENT ON TABLE `edu_study_plan_item` IS '学习计划明细表';


CREATE TABLE IF NOT EXISTS `edu_wrong_question`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `knowledge_id`    BIGINT       DEFAULT NULL COMMENT '知识点ID',
    `student_answer`  TEXT         DEFAULT NULL COMMENT '学生答案',
    `correct_times`   INT          DEFAULT 0 COMMENT '连续正确次数',
    `created_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_wq_student` (`student_id`),
    KEY `idx_wq_question` (`question_id`)
);

COMMENT ON TABLE `edu_wrong_question` IS '错题本表';



CREATE TABLE IF NOT EXISTS `edu_learning_state`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `state`           VARCHAR(20)  NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态: NOT_STARTED/LEARNING/MASTERED/PROFICIENT/FORGOTTEN/REVIEWING',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_learning_state` (`student_id`, `knowledge_id`)
);
COMMENT ON TABLE `edu_learning_state` IS '知识点学习状态表（持久化 LearningStateMachine）';

CREATE TABLE IF NOT EXISTS `edu_achievement`(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `code`            VARCHAR(50)  NOT NULL COMMENT '成就编码',
    `name`            VARCHAR(100) NOT NULL COMMENT '成就名称',
    `description`     VARCHAR(500) DEFAULT NULL COMMENT '成就描述',
    `icon`            VARCHAR(200) DEFAULT NULL COMMENT '成就图标',
    `earned_at`       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_achievement` (`student_id`, `code`)
);
COMMENT ON TABLE `edu_achievement` IS '成就表（成长档案）';

