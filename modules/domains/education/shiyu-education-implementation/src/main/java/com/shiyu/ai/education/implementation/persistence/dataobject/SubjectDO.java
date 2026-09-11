package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.SubjectBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_subject")
@AutoMapper(target = SubjectBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class SubjectDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String code;
    private String name;
    private String gradeLevel;
    private String icon;
    private Integer sortOrder;
}
