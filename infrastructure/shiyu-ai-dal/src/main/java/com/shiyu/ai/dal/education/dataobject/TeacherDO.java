package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.education.domain.model.TeacherBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("edu_teacher")
@AutoMapper(target = TeacherBO.class, reverseConvertGenerate = true)
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
