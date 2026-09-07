package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.UserRequest;
import com.shiyu.ai.auth.request.UserTenantRoleRequest;
import com.shiyu.ai.auth.vo.UserTenantAssignmentVO;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Map;

/** User application contract. */
public interface UserService {
    UserVO detailView(ActorContext actor, Long userId);
    Map<String, Object> createUser(ActorContext actor, UserRequest request, Long[] roleIds, Long targetTenantId);
    boolean updateUser(ActorContext actor, Long userId, UserRequest request, Long[] roleIds, Long targetTenantId);
    PageData<UserVO> getUserList(ActorContext actor, String username, Number pageNum, Number pageSize);
    boolean deleteUser(ActorContext actor, Long userId);
    String resetUserPassword(ActorContext actor, Long userId, String password);
    List<UserTenantAssignmentVO> getTenantAssignments(ActorContext actor, Long userId);
    boolean replaceTenantAssignments(ActorContext actor, Long userId, List<UserTenantRoleRequest> assignments);
    boolean changePassword(ActorContext actor, Long userId, String oldPassword, String newPassword);
}
