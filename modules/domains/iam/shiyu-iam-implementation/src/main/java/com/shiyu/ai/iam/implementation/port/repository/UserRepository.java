package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 负责 用户 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface UserRepository {
    /**
     * 查询 用户 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param username 用于完成本次业务处理的 username 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<UserBO>> selectPage(
            TenantId tenantId, Number pageNo, Number pageSize, String username);

    /**
     * 查询 用户 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    UserBO selectByUsername(String username);

    /**
     * 查询 用户 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    UserBO selectById(Long id);

    /**
     * 创建或保存 用户 相关业务数据，并返回处理结果。
     *
     * @param userBO 用于完成本次业务处理的 userBO 参数。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    UserBO insert(UserBO userBO);

    /**
     * 更新或设置 用户 相关业务数据，并返回处理结果。
     *
     * @param userBO 用于完成本次业务处理的 userBO 参数。
     * @return 返回本次条件判断是否成立。
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
     * 查询 用户 相关业务数据，并返回处理结果。
     *
     * @param email 用于完成本次业务处理的 email 参数。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    UserBO selectByEmail(String email);

    /**
     * 查询 用户 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    UserBO selectActiveUserByUsername(String username);

    /**
     * 校验或判断 用户 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param currentTenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean isUserInScope(Long userId, TenantId currentTenantId);
}
