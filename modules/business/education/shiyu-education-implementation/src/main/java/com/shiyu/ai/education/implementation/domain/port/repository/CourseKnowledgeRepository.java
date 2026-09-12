package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.CourseKnowledgeBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * CourseKnowledgeRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface CourseKnowledgeRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param courseId 课程标识。
     *
     * @return 符合条件的结果集合。
     */
    List<CourseKnowledgeBO> selectByCourseId(TenantId tenantId, Long courseId);
}
