package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.LearningStateBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@Table("edu_learning_state")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = LearningStateBO.class, reverseConvertGenerate = true)
public class LearningStateDO extends TenantEntity {

    @Serial private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long knowledgeId;
    private String state;
}
