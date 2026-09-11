package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.TeacherBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_teacher")
@AutoMapper(target = TeacherBO.class, reverseConvertGenerate = true)
@SuppressWarnings("serial")
public class TeacherDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;
    private String teacherNo;
    private String name;
    private String subject;
    private String school;
    private String title;
    private String phone;
    private LocalDateTime createdAt;
}
