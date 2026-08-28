package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.WrongQuestionBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_wrong_question")
@AutoMapper(target = WrongQuestionBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class WrongQuestionDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long questionId;
    private Long knowledgeId;
    private String studentAnswer;
    private Integer correctTimes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

