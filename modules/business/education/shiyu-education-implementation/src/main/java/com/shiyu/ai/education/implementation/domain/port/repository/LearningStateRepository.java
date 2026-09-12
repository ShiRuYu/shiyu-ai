package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.LearningStateBO;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * LearningStateRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface LearningStateRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     *
     * @return 操作结果。
     */
    LearningStateBO selectByStudentAndKnowledge(
            TenantId tenantId, Long studentId, Long knowledgeId);

    /**
     * 保存或更新业务对象。
     *
     * @param tenantId 租户标识。
     * @param state 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int upsert(TenantId tenantId, LearningStateBO state);
}
