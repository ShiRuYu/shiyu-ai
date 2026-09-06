package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student 业务对象
 */
@Data
public class StudentBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

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
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
