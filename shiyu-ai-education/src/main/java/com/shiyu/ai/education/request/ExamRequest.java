package com.shiyu.ai.education.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ExamRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private String subjectCode;
    private Integer grade;
    private Long teacherId;
    private Integer durationMin;
    private Integer totalScore;
    private Integer status;
}
