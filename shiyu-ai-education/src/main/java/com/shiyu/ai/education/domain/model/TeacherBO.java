package com.shiyu.ai.education.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherBO extends TenantModel {
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
