package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import lombok.Data;

import java.math.BigDecimal;

/** 不可变的题目快照及学生提交的答案。 */
@Data
@Table("edu_exam_attempt_question")
public class ExamAttemptQuestionDO {
    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column(tenantId = true)
    private Long tenantId;

    private Long attemptId;
    private Long examQuestionId;
    private Long questionId;
    private Long sectionId;
    private String sectionName;
    private Integer sectionOrderNo;
    private Integer orderNo;
    private String title;
    private String type;
    private String optionsJson;
    private String correctAnswer;
    private String analysis;
    private BigDecimal maxScore;
    private String answer;
    private BigDecimal awardedScore;
}
