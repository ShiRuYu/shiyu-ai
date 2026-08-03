package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CourseChapter 业务对象
 */
@Data
public class CourseChapterBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long courseId;

    private String name;

    private Integer orderNo;

    private LocalDateTime createdAt;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
