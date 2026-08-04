package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CourseSection 业务对象
 */
@Data
public class CourseSectionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long chapterId;

    private String name;

    private Integer orderNo;

    private String contentUrl;

    private String videoUrl;

    private Integer durationMin;

    private LocalDateTime createdAt;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
