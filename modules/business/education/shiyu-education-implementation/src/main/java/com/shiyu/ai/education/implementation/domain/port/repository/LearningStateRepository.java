package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.LearningStateBO;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * 负责 Learning State 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface LearningStateRepository {
    /**
     * 查询 Learning State 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回 Learning State 相关操作生成的结果数据。
     */
    LearningStateBO selectByStudentAndKnowledge(
            TenantId tenantId, Long studentId, Long knowledgeId);

    /**
     * 执行 Learning State 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param state 用于完成本次业务处理的 state 参数。
     * @return 返回 Learning State 相关操作生成的结果数据。
     */
    int upsert(TenantId tenantId, LearningStateBO state);
}
