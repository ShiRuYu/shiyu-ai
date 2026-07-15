package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SubjectRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "科目编码不能为空")
    private String code;

    @NotBlank(message = "科目名称不能为空")
    private String name;

    private String gradeLevel;
    private String description;
    private Integer status;
}
