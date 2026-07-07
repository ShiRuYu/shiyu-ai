package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("student")
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
