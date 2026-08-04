package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.LearningStateBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_learning_state")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = LearningStateBO.class, reverseConvertGenerate = true)
public class LearningStateDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long studentId;
    private Long knowledgeId;
    private String state;
}
