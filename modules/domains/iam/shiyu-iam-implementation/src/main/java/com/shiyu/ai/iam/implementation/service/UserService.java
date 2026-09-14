package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.iam.implementation.request.UserRequest;
import com.shiyu.ai.iam.implementation.request.UserTenantRoleRequest;
import com.shiyu.ai.iam.implementation.vo.UserTenantAssignmentVO;
import com.shiyu.ai.iam.implementation.vo.UserVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Map;

/**
 * UserService 服务接口，负责执行身份与访问领域相关业务操作。
 */
public interface UserService {
    /**
     * 执行 {@code detailView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     *
     * @return 操作结果。
     */
    UserVO detailView(ActorContext actor, Long userId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     * @param roleIds 方法参数。
     * @param targetTenantId 方法参数。
     *
     * @return 操作结果。
     */
    Map<String, Object> createUser(
            ActorContext actor, UserRequest request, Long[] roleIds, Long targetTenantId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     * @param request 请求参数。
     * @param roleIds 方法参数。
     * @param targetTenantId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean updateUser(
            ActorContext actor,
            Long userId,
            UserRequest request,
            Long[] roleIds,
            Long targetTenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param username 方法参数。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     *
     * @return 操作结果。
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
     * 执行 {@code resetUserPassword} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     * @param password 方法参数。
     *
     * @return 操作结果。
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
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     * @param assignments 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean replaceTenantAssignments(
            ActorContext actor, Long userId, List<UserTenantRoleRequest> assignments);

    /**
     * 执行 {@code changePassword} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param userId 用户标识。
     * @param oldPassword 方法参数。
     * @param newPassword 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean changePassword(ActorContext actor, Long userId, String oldPassword, String newPassword);
}
