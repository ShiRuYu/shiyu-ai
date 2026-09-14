package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * UserRepository 仓储接口，负责访问和持久化身份与访问领域聚合数据。
 */
public interface UserRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param username 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<UserBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String username);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param username 方法参数。
     *
     * @return 操作结果。
     */
    UserBO selectByUsername(String username);

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    UserBO selectById(Long id);

    /**
     * 创建并保存业务对象。
     *
     * @param userBO 方法参数。
     *
     * @return 操作结果。
     */
    UserBO insert(UserBO userBO);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param userBO 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean update(UserBO userBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteById(Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<RoleBO> selectRolesByUserId(Long userId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param email 方法参数。
     *
     * @return 操作结果。
     */
    UserBO selectByEmail(String email);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param username 方法参数。
     *
     * @return 操作结果。
     */
    UserBO selectActiveUserByUsername(String username);

    /**
     * 判断当前条件是否满足。
     *
     * @param userId 用户标识。
     * @param currentTenantId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean isUserInScope(Long userId, TenantId currentTenantId);
}
