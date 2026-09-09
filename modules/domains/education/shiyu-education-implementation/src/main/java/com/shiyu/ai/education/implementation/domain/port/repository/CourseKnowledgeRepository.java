package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.CourseKnowledgeBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface CourseKnowledgeRepository {
    List<CourseKnowledgeBO> selectByCourseId(TenantId tenantId, Long courseId);
}

