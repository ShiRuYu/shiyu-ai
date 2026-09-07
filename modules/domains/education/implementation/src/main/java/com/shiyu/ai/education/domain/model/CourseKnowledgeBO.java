package com.shiyu.ai.education.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * CourseKnowledge 业务对象
 */
@Data
public class CourseKnowledgeBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long courseId;

    private Long knowledgeId;

    private Long sectionId;

    private Integer sortOrder;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;


}
