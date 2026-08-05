package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ExamRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    @NotBlank(message = "考试名称不能为空")
    private String name;
    @NotBlank(message = "考试类型不能为空")
    private String type;
    @NotBlank(message = "学科编码不能为空")
    private String subjectCode;
    @NotNull(message = "年级不能为空")
    private Integer grade;
    private Long teacherId;
    @NotNull(message = "考试时长不能为空")
    private Integer durationMin;
    @NotNull(message = "总分不能为空")
    private Integer totalScore;
    private Integer status;
}
