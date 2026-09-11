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

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_student")
@AutoMapper(target = StudentBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class StudentDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;
    private String studentNo;
    private String name;
    private Integer gender;
    private LocalDate birthDate;
    private Integer grade;
    private String gradeLevel;
    private String school;
    private String className;
    private Long parentId;
    private String learningStyle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
