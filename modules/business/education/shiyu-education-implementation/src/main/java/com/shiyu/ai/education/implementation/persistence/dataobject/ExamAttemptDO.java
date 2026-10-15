package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 每名学生在每场考试中仅有一条持久化作答记录。 */
@Data
@Table("edu_exam_attempt")
public class ExamAttemptDO {
    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(tenantId = true)
    private Long tenantId;

    private Long examId;
    private Long studentId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime deadlineAt;
    private LocalDateTime submittedAt;
    private BigDecimal score;
    private BigDecimal totalScore;
}
