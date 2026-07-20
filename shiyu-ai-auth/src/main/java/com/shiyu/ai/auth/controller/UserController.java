package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.request.*;
import com.shiyu.ai.dal.auth.bo.UserBO;
import com.shiyu.ai.auth.vo.UserPageResponse;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.auth.vo.WorkspaceContextVO;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
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

/**
 * 用户管理 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "User", description = "User")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * 获取当前用户信息
     * GET /auth/user/detail
     */
    @Operation(summary = "Get User Info")
    @GetMapping("/detail")
    public Result<UserVO> getUserInfo() {
        log.info("获取当前用户信息");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        
        UserBO userBO = userService.getUserDetail(userId);
        if (userBO == null) return Result.fail("用户不存在");
        
        UserVO userVO = MapstructUtils.convert(userBO, UserVO.class);

        try {
            userVO.setTenants(authService.getUserTenants(userId));
            List<WorkspaceContextVO> workspaces = authService.getUserWorkspaces(userId);
            userVO.setWorkspaces(workspaces);

            if (userVO.getExtInfo() != null) {
                var extMap = com.shiyu.ai.common.core.utils.JSONUtils.parseObject(
                        userVO.getExtInfo(), java.util.Map.class);
                if (extMap != null) {
                    Object tid = extMap.get("currentTenantId");
                    if (tid instanceof Number) userVO.setCurrentTenantId(((Number) tid).longValue());
                    Object wid = extMap.get("currentWorkspaceId");
                    if (wid instanceof Number) userVO.setCurrentWorkspaceId(((Number) wid).longValue());
                }
            }
        } catch (Exception e) {
            log.warn("获取用户租户/工作空间信息失败: {}", e.getMessage());
        }

        return Result.success(userVO);
    }

    /**
     * 用户列表 - 分页
     * GET /auth/user/list
     */
    @Operation(summary = "Get User List")
    @GetMapping("/list")
    public Result<UserPageResponse> getUserList(@Valid UserPageRequest request) {
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}",
                request.getUsername(), request.getPageNo(), request.getPageSize());
        return Result.success(userService.getUserList(
                request.getUsername(), request.getPageNo(), request.getPageSize()));
    }

    /**
     * 新增用户
     * POST /auth/user/create
     */
    @Operation(summary = "Create User")
    @PostMapping("/create")
    public Result<Map<String, Object>> createUser(@Valid @RequestBody UserRequest request) {
        log.info("新增用户");
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        Map<String, Object> result = userService.createUser(userBO);
        return Result.success(result);
    }

    /**
     * 修改用户
     * POST /auth/user/update?userId=
     */
    @Operation(summary = "Update User")
    @PostMapping("/update")
    public Result<Void> updateUser(@RequestParam Long userId, @Valid @RequestBody UserRequest request) {
        log.info("修改用户，userId: {}", userId);
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        return userService.updateUser(userId, userBO) ? Result.success() : Result.fail("用户不存在");
    }

    /**
     * 删除用户
     * POST /auth/user/delete?userId=
     */
    @Operation(summary = "Delete User")
    @PostMapping("/delete")
    public Result<Void> deleteUser(@RequestParam Long userId) {
        log.info("删除用户，userId: {}", userId);
        return userService.deleteUser(userId) ? Result.success() : Result.fail("用户不存在");
    }

    /**
     * 重置用户密码
     * POST /auth/user/password/reset?userId=
     */
    @Operation(summary = "Reset Password")
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@RequestParam Long userId, @Valid @RequestBody ResetPasswordRequest request) {
        log.info("重置用户密码，userId: {}", userId);
        String result = userService.resetUserPassword(userId, request.getPassword());
        if (result == null) {
            return Result.fail("用户不存在");
        }
        return Result.success();
    }

    /**
     * 修改密码（需校验旧密码）
     * POST /auth/user/password/change?userId=
     */
    @Operation(summary = "Change Password")
    @PostMapping("/password/change")
    public Result<Void> changePassword(@RequestParam Long userId, @Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码，userId: {}", userId);
        boolean success = userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return success ? Result.success() : Result.fail("旧密码错误或用户不存在");
    }
}
