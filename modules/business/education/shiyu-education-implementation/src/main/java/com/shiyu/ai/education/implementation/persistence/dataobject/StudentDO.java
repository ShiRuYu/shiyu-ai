package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * {@code StudentDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_student")
@AutoMapper(target = StudentBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class StudentDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;
    /**
     * studentNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String studentNo;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * gender 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer gender;
    /**
     * birthDate 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDate birthDate;
    /**
     * grade 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer grade;
    /**
     * gradeLevel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String gradeLevel;
    /**
     * school 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String school;
    /**
     * className 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String className;
    /**
     * 父级标识，表示当前对象中的对应属性。
     */
    private Long parentId;
    /**
     * learningStyle 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String learningStyle;
    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间，表示当前对象中的对应属性。
     */
    private LocalDateTime updatedAt;
}
