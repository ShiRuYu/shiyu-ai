package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CourseRequest implements Serializable {

    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String subjectCode;
    private Integer grade;
    private String description;
    private String coverUrl;
    private Long textbookId;
    private Long teacherId;
    private Integer totalHours;
    private Integer status;
}
