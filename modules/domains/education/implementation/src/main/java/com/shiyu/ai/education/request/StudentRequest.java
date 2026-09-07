package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class StudentRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "学生姓名不能为空")
    private String name;

    @NotNull(message = "用户ID不能为空")
    private Long userId;
    private String studentNo;

    @NotNull(message = "年级不能为空")
    private Integer grade;
    private String gradeLevel;
    private String school;
    private String className;
    private Integer status;
}
