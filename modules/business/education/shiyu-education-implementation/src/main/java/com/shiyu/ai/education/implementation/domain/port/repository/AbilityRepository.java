package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.AbilityBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 Ability 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AbilityRepository {
    /**
     * 查询 Ability 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param studentId 用于定位student的标识。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回 Ability 相关操作生成的结果数据。
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
     * 创建或保存 Ability 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ability 用于完成本次业务处理的 ability 参数。
     * @return 返回 Ability 相关操作生成的结果数据。
     */
    int insert(TenantId tenantId, AbilityBO ability);

    /**
     * 更新或设置 Ability 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ability 用于完成本次业务处理的 ability 参数。
     * @return 返回 Ability 相关操作生成的结果数据。
     */
    int update(TenantId tenantId, AbilityBO ability);
}
