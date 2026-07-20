package com.shiyu.ai.dal.education.bo;

import com.shiyu.ai.dal.education.dataobject.CourseKnowledgeDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * CourseKnowledge 业务对象
 */
@AutoMapper(target = CourseKnowledgeDO.class, reverseConvertGenerate = true)
@Data
public class CourseKnowledgeBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long courseId;

    private Long knowledgeId;

    private Long sectionId;

    private Integer sortOrder;

}
