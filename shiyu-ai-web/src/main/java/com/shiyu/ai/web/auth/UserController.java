package com.shiyu.ai.web.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.*;
import com.shiyu.ai.dal.auth.bo.UserBO;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.api.PageData;
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

            // 当前租户以请求上下文为准。切换到子租户时，extInfo/session 可能仍是旧快照，
            // 不能因此把 user/detail 返回成登录租户。
            Long contextTenantId = LoginContextHolder.getCurrentTenantId();
            if (contextTenantId != null) {
                userVO.setCurrentTenantId(contextTenantId);
            }
            Long contextHomeTenantId = LoginContextHolder.getHomeTenantId();
            if (contextHomeTenantId != null) {
                userVO.setHomeTenantId(contextHomeTenantId);
            }
            String contextSwitchMode = LoginContextHolder.getSwitchMode();
            if (contextSwitchMode != null) {
                userVO.setSwitchMode(contextSwitchMode);
            }

            if (userVO.getExtInfo() != null) {
                var extMap = com.shiyu.ai.common.core.utils.JSONUtils.parseObject(
                        userVO.getExtInfo(), java.util.Map.class);
                if (extMap != null) {
                    if (userVO.getCurrentTenantId() == null) {
                        Object tid = extMap.get("currentTenantId");
                        if (tid instanceof Number) userVO.setCurrentTenantId(((Number) tid).longValue());
                    }
                    if (userVO.getHomeTenantId() == null) {
                        Object homeTid = extMap.get("homeTenantId");
                        if (homeTid instanceof Number) userVO.setHomeTenantId(((Number) homeTid).longValue());
                    }
                    if (userVO.getSwitchMode() == null) {
                        Object mode = extMap.get("switchMode");
                        if (mode instanceof String) userVO.setSwitchMode((String) mode);
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
     * GET /auth/user/list
     */
    @Operation(summary = "Get User List")
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public Result<PageData<UserVO>> getUserList(@Valid UserPageRequest request) {
        log.info("获取用户列表，username: {}, pageNum: {}, pageSize: {}",
                request.getUsername(), request.getPageNum(), request.getPageSize());
        return Result.success(userService.getUserList(
                request.getUsername(), request.getPageNum(), request.getPageSize()));
    }

    /**
     * 新增用户
     * POST /auth/user/create
     */
    @Operation(summary = "Create User")
    @SaCheckPermission("system:user:create")
    @PostMapping("/create")
    public Result<Map<String, Object>> createUser(@Valid @RequestBody UserRequest request) {
        log.info("新增用户");
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        Map<String, Object> result = userService.createUser(
                userBO, request.getRoleIds(), request.getTenantId());
        return Result.success(result);
    }

    /**
     * 修改用户
     * POST /auth/user/update?userId=
     */
    @Operation(summary = "Update User")
    @SaCheckPermission("system:user:update")
    @PostMapping("/update")
    public Result<Void> updateUser(@RequestParam Long userId, @Valid @RequestBody UserRequest request) {
        log.info("修改用户，userId: {}", userId);
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        return userService.updateUser(userId, userBO, request.getRoleIds(), request.getTenantId())
                ? Result.success()
                : Result.fail("用户不存在");
    }

    @Operation(summary = "Get User Tenant Assignments")
    @SaCheckPermission("system:user:list")
    @GetMapping("/tenant-assignments")
    public Result<List<com.shiyu.ai.auth.vo.UserTenantAssignmentVO>> getTenantAssignments(
            @RequestParam Long userId) {
        return Result.success(userService.getTenantAssignments(userId));
    }

    @Operation(summary = "Replace User Tenant Assignments")
    @SaCheckPermission("system:user:update")
    @PostMapping("/tenant-assignments/replace")
    public Result<Void> replaceTenantAssignments(
            @RequestParam Long userId,
            @RequestBody List<com.shiyu.ai.auth.request.UserTenantRoleRequest> assignments) {
        return userService.replaceTenantAssignments(userId, assignments)
                ? Result.success() : Result.fail("租户分配失败");
    }

    /**
     * 删除用户
     * POST /auth/user/delete?userId=
     */
    @Operation(summary = "Delete User")
    @SaCheckPermission("system:user:delete")
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
    @SaCheckPermission("system:user:password")
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
