package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.UserRequest;
import com.shiyu.ai.auth.request.UserTenantRoleRequest;
import com.shiyu.ai.auth.vo.UserTenantAssignmentVO;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.api.PageData;

import java.util.List;
import java.util.Map;

/** User application contract. */
public interface UserService {
    UserVO detailView(Long userId);
    Map<String, Object> createUser(UserRequest request, Long[] roleIds, Long targetTenantId);
    boolean updateUser(Long userId, UserRequest request, Long[] roleIds, Long targetTenantId);
    PageData<UserVO> getUserList(String username, Number pageNum, Number pageSize);
    boolean deleteUser(Long userId);
    String resetUserPassword(Long userId, String password);
    List<UserTenantAssignmentVO> getTenantAssignments(Long userId);
    boolean replaceTenantAssignments(Long userId, List<UserTenantRoleRequest> assignments);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
