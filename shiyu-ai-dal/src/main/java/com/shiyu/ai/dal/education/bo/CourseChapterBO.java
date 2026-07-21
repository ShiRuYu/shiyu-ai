package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.CourseChapterDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CourseChapter 业务对象
 */
@AutoMapper(target = CourseChapterDO.class, reverseConvertGenerate = true)
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
