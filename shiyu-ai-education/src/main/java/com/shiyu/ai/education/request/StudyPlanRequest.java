package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class StudyPlanRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "学生ID不能为空")
    private Long id;
    private Long studentId;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}

