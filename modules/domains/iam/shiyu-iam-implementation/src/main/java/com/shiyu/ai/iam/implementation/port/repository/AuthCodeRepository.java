package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.AuthCodeBO;
import com.shiyu.ai.iam.implementation.domain.model.RoleScopeAuthCodeBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantAuthCodeBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 认证 Code 的持久化查询、保存和删除，并维护数据访问边界。
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
     * 查询 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AuthCodeBO> selectByRoleIdAndTenantId(Long roleId, TenantId tenantId);

    /**
     * 查询 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 认证 Code 相关操作生成的结果数据。
     */
    AuthCodeBO selectById(Long id);

    /**
     * 创建或保存 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 认证 Code 相关操作生成的结果数据。
     */
    AuthCodeBO insert(AuthCodeBO code);

    /**
     * 更新或设置 认证 Code 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     */
    void update(AuthCodeBO code);

    /**
     * 执行 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param excludeId 用于定位exclude的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 校验或判断 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param authCodeId 用于定位auth Code的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
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
     * 校验或判断 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param authCodeId 用于定位auth Code的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean hasRoleAssignments(Long authCodeId);

    /**
     * 创建或保存 认证 Code 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param assignment 用于完成本次业务处理的 assignment 参数。
     */
    void insertTenantCode(TenantAuthCodeBO assignment);

    /**
     * 删除或移除 认证 Code 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param authCodeId 用于定位auth Code的标识。
     */
    void deleteTenantCode(TenantId tenantId, Long authCodeId);

    /**
     * 执行 认证 Code 相关业务数据，并返回处理结果。
     *
     * @param authCodeId 用于定位auth Code的标识。
     * @return 返回 认证 Code 相关操作生成的结果数据。
     */
    long countActiveTenantLinks(Long authCodeId);

    /**
     * 创建或保存 认证 Code 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param assignments 用于完成本次业务处理的 assignments 参数。
     */
    void insertRoleAssignments(List<RoleScopeAuthCodeBO> assignments);

    /**
     * 删除或移除 认证 Code 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param roleId 用于定位role的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param authCodeId 用于定位auth Code的标识。
     */
    void deleteRoleAssignments(Long roleId, TenantId tenantId, Long authCodeId);
}
