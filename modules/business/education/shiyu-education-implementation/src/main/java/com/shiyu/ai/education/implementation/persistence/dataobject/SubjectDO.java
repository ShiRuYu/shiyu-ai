package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.SubjectBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code SubjectDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_subject")
@AutoMapper(target = SubjectBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class SubjectDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * gradeLevel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String gradeLevel;
    /**
     * icon 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String icon;
    /**
     * sortOrder 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer sortOrder;
}
