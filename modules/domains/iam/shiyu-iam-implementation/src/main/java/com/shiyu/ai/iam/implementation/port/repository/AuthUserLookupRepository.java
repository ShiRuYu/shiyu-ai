package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Set;

/**
 * 负责 认证 用户 Lookup 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AuthUserLookupRepository {
    /**
     * 查询 认证 用户 Lookup 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 认证 用户 Lookup 相关操作生成的结果数据。
     */
    UserBO selectUserById(Long userId);

    /**
     * 更新或设置 认证 用户 Lookup 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param extInfo 用于完成本次业务处理的 extInfo 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean updateUserExtInfo(Long userId, String extInfo);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<UserScopeRoleBO> selectUserScopeRoles(Long userId);

    /**
     * 查询 认证 用户 Lookup 相关业务数据，并返回处理结果。
     *
     * @param roleId 用于定位role的标识。
     * @return 返回 认证 用户 Lookup 相关操作生成的结果数据。
     */
    RoleBO selectRoleById(Long roleId);

    /**
     * 查询 认证 用户 Lookup 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 认证 用户 Lookup 相关操作生成的结果数据。
     */
    RoleBO selectTenantSuperRole(TenantId tenantId);

    /**
     * 查询 认证 用户 Lookup 相关业务数据，并返回处理结果。
     *
     * @param roleIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<RoleBO> selectRolesByIds(Set<Long> roleIds);

    /**
     * 查询 认证 用户 Lookup 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 认证 用户 Lookup 相关操作生成的结果数据。
     */
    TenantBO selectTenantById(TenantId tenantId);
}
