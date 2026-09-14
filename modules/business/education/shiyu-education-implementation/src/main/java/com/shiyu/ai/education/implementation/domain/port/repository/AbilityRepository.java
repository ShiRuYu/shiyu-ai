package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.AbilityBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * AbilityRepository 仓储接口，负责访问和持久化教育领域聚合数据。
 */
public interface AbilityRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param studentId 学生标识。
     * @param knowledgeId 知识点标识。
     *
     * @return 操作结果。
     */
    AbilityBO selectByStudentAndKnowledge(TenantId tenantId, Long studentId, Long knowledgeId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param studentId 学生标识。
     *
     * @return 符合条件的结果集合。
     */
    List<AbilityBO> selectByStudent(TenantId tenantId, Long studentId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param ability 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int insert(TenantId tenantId, AbilityBO ability);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param ability 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(TenantId tenantId, AbilityBO ability);
}
