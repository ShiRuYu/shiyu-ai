package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@Table("course_knowledge")
@EqualsAndHashCode(callSuper = true)
public class CourseKnowledgeDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long courseId;
    private Long knowledgeId;
    private Long sectionId;
    private Integer sortOrder;
}
