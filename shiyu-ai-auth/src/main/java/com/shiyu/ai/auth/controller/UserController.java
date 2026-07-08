package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.bo.RoleBO;
import com.shiyu.ai.auth.request.UserRequest;
import com.shiyu.ai.auth.bo.UserBO;
import com.shiyu.ai.auth.vo.UserPageResponse;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.auth.vo.WorkspaceContextVO;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理 Controller
 */
@Slf4j
@Tag(name = "User", description = "User")
@RestController
@RequestMapping("/auth/user")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * 获取当前用户信息
     * GET /user/info
     */
    @Operation(summary = "Get User Detail")
    @GetMapping("/detail")
    public Result<UserVO> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("获取当前用户信息");

        Long userId = LoginContextHolder.getUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        
        UserBO userBO = userService.getUserDetail(userId);
        
        if (userBO == null) {
            return Result.fail("用户不存在");
        }
        
        UserVO userVO = MapstructUtils.convert(userBO, UserVO.class);

        // 填充租户和工作空间信息
        try {
            userVO.setTenants(authService.getUserTenants(userId));
            List<WorkspaceContextVO> workspaces = authService.getUserWorkspaces(userId);
            userVO.setWorkspaces(workspaces);

            // 从 extInfo 解析当前租户和工作空间 ID
            if (userVO.getExtInfo() != null) {
                var extMap = com.shiyu.ai.common.core.utils.JSONUtils.parseObject(
                        userVO.getExtInfo(), java.util.Map.class);
                if (extMap != null) {
                    Object tid = extMap.get("currentTenantId");
                    if (tid instanceof Number) {
                        userVO.setCurrentTenantId(((Number) tid).longValue());
                    }
                    Object wid = extMap.get("currentWorkspaceId");
                    if (wid instanceof Number) {
                        userVO.setCurrentWorkspaceId(((Number) wid).longValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取用户租户/工作空间信息失败: {}", e.getMessage());
        }

        return Result.success(userVO);
    }

    /**
     * 用户列表 - 分页
     */
    @Operation(summary = "Get User List")
    @GetMapping("/list")
    public Result<UserPageResponse> getUserList(
            @RequestParam(required = false) String username,
            PageQuery pageQuery) {
        Integer pageNo = pageQuery != null && pageQuery.getPageNum() != null ? pageQuery.getPageNum() : 1;
        Integer pageSize = pageQuery != null && pageQuery.getPageSize() != null ? pageQuery.getPageSize() : 10;
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        UserPageResponse pageResponse = userService.getUserList(username, pageNo, pageSize);
        
        return Result.success(pageResponse);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "Delete User")
    @PostMapping("/delete")
    public Result<Void> deleteUser(@RequestParam Long userId) {
        log.info("删除用户，userId: {}", userId);
        
        boolean success = userService.deleteUser(userId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 修改用户
     */
    @Operation(summary = "Update User")
    @PostMapping("/update")
    public Result<Void> updateUser(
            @RequestParam Long userId,
            @Valid @RequestBody UserRequest request) {
        log.info("修改用户，userId: {}", userId);
        
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        boolean success = userService.updateUser(userId, userBO);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "Reset User Password")
    @PostMapping("/password/reset")
    public Result<String> resetPassword(
            @RequestParam Long userId,
            @RequestBody Map<String, String> passwordMap) {
        log.info("重置用户密码，userId: {}", userId);

        String password = passwordMap.get("password");
        String newPassword = userService.resetUserPassword(userId, password);

        if (newPassword != null) {
            return Result.success(newPassword);
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 修改密码（需校验旧密码）
     */
    @Operation(summary = "Change Password")
    @PostMapping("/password/change")
    public Result<Void> changePassword(
            @RequestParam Long userId,
            @RequestBody Map<String, String> body) {
        log.info("修改密码，userId: {}", userId);

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.isEmpty() ||
            newPassword == null || newPassword.isEmpty()) {
            return Result.fail("旧密码和新密码不能为空");
        }

        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("旧密码错误或用户不存在");
        }
    }

    /**
     * 新增用户
     */
    @Operation(summary = "Create User")
    @PostMapping("/create")
    public Result<java.util.Map<String, Object>> createUser(@Valid @RequestBody UserRequest request) {
        log.info("新增用户");

        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        java.util.Map<String, Object> result = userService.createUser(userBO);

        return Result.success(result);
    }
}
