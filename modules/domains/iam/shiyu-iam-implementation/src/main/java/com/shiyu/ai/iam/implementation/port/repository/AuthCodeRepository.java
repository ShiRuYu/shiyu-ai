package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.AuthCodeBO;
import com.shiyu.ai.iam.implementation.domain.model.RoleScopeAuthCodeBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantAuthCodeBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * AuthCodeRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface AuthCodeRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<AuthCodeBO> selectByTenantId(TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<AuthCodeBO> selectByRoleIdAndTenantId(Long roleId, TenantId tenantId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AuthCodeBO selectById(Long id);

    /**
     * 创建并保存业务对象。
     *
     * @param code 方法参数。
     *
     * @return 操作结果。
     */
    AuthCodeBO insert(AuthCodeBO code);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param code 方法参数。
     */
    void update(AuthCodeBO code);

    /**
     * 判断当前条件是否满足。
     *
     * @param code 方法参数。
     * @param excludeId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 判断当前条件是否满足。
     *
     * @param authCodeId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 条件是否满足。
     */
    boolean isAvailable(Long authCodeId, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param ids 目标对象标识集合。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<AuthCodeBO> selectAvailableByIds(List<Long> ids, TenantId tenantId);

    /**
     * 判断当前条件是否满足。
     *
     * @param authCodeId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean hasRoleAssignments(Long authCodeId);

    /**
     * 创建并保存业务对象。
     *
     * @param assignment 方法参数。
     */
    void insertTenantCode(TenantAuthCodeBO assignment);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param authCodeId 方法参数。
     */
    void deleteTenantCode(TenantId tenantId, Long authCodeId);

    /**
     * 统计符合条件的数据。
     *
     * @param authCodeId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    long countActiveTenantLinks(Long authCodeId);

    /**
     * 创建并保存业务对象。
     *
     * @param assignments 方法参数。
     */
    void insertRoleAssignments(List<RoleScopeAuthCodeBO> assignments);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param roleId 方法参数。
     * @param tenantId 租户标识。
     * @param authCodeId 方法参数。
     */
    void deleteRoleAssignments(Long roleId, TenantId tenantId, Long authCodeId);
}
