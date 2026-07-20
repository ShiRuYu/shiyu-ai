package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.CourseSectionDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CourseSection 业务对象
 */
@AutoMapper(target = CourseSectionDO.class, reverseConvertGenerate = true)
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

}
