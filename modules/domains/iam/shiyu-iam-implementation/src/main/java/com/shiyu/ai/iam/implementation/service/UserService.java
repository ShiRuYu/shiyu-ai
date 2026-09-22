package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.iam.implementation.request.UserRequest;
import com.shiyu.ai.iam.implementation.request.UserTenantRoleRequest;
import com.shiyu.ai.iam.implementation.vo.UserTenantAssignmentVO;
import com.shiyu.ai.iam.implementation.vo.UserVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Map;

/**
 * 提供 用户 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface UserService {
    /**
     * 查询 用户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    UserVO detailView(ActorContext actor, Long userId);

    /**
     * 创建或保存 用户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param roleIds 待处理的业务对象标识集合。
     * @param targetTenantId 当前操作涉及的租户标识。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    Map<String, Object> createUser(
            ActorContext actor, UserRequest request, Long[] roleIds, Long targetTenantId);

    /**
     * 更新或设置 用户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param roleIds 待处理的业务对象标识集合。
     * @param targetTenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean updateUser(
            ActorContext actor,
            Long userId,
            UserRequest request,
            Long[] roleIds,
            Long targetTenantId);

    /**
     * 查询 用户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param username 用于完成本次业务处理的 username 参数。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    PageData<UserVO> getUserList(
            ActorContext actor, String username, Number pageNum, Number pageSize);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteUser(ActorContext actor, Long userId);

    /**
     * 执行 用户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @param password 用于完成本次业务处理的 password 参数。
     * @return 返回 用户 相关操作生成的结果数据。
     */
    String resetUserPassword(ActorContext actor, Long userId, String password);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<UserTenantAssignmentVO> getTenantAssignments(ActorContext actor, Long userId);

    /**
     * 执行 用户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @param assignments 用于完成本次业务处理的 assignments 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean replaceTenantAssignments(
            ActorContext actor, Long userId, List<UserTenantRoleRequest> assignments);

    /**
     * 执行 用户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @param oldPassword 用于完成本次业务处理的 oldPassword 参数。
     * @param newPassword 用于完成本次业务处理的 newPassword 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean changePassword(ActorContext actor, Long userId, String oldPassword, String newPassword);
}
