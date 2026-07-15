package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
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

    private Long userId;
    private String studentNo;
    private Integer status;
}
