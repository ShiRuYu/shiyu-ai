package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

/**
 * Course 业务对象
 */
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
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
