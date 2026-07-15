package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class QuestionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "题目标题不能为空")
    private String title;

    @NotBlank(message = "题目类型不能为空")
    private String type;

    private String code;
    private String subjectCode;
    private Integer grade;
    private Integer difficulty;
    private String options;
    private String answer;
    private String analysis;
    private String tags;
    private Integer status;
}
