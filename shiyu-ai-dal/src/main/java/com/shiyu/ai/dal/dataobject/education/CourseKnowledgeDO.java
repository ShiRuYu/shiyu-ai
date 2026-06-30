package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Table("course_knowledge")
public class CourseKnowledgeDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long courseId;
    private Long knowledgeId;
    private Long sectionId;
    private Integer sortOrder;
}
