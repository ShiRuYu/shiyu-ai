package com.shiyu.ai.dal.bo.education;

import com.shiyu.ai.dal.dataobject.education.CourseDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * Course 业务对象
 */
@AutoMapper(target = CourseDO.class, reverseConvertGenerate = true)
@Data
public class CourseBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String description;

    private String subjectCode;

    private Integer grade;

    private Long textbookId;

    private Long teacherId;

    private String coverUrl;

    private Integer totalHours;

    private Long viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
