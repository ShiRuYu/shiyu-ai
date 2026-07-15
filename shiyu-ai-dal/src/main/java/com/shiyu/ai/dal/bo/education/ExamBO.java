package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.ExamDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * Exam 业务对象
 */
@AutoMapper(target = ExamDO.class, reverseConvertGenerate = true)
@Data
public class ExamBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String type;

    private String subjectCode;

    private Integer grade;

    private Integer durationMin;

    private Integer totalScore;

    private Long teacherId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;

}
