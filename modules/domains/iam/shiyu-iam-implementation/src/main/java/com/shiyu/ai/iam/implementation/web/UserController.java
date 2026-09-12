package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.request.*;
import com.shiyu.ai.iam.implementation.service.AuthService;
import com.shiyu.ai.iam.implementation.service.UserService;
import com.shiyu.ai.iam.implementation.vo.UserTenantAssignmentVO;
import com.shiyu.ai.iam.implementation.vo.UserVO;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * {@code UserController} 是Web模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/iam/users")
@RequiredArgsConstructor
public class UserController {
    /**
     * 用户服务，表示当前对象中的对应属性。
     */
    private final UserService userService;
    /**
     * authService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthService authService;

    /**
     * {@code getUserInfo} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/detail")
    public Result<UserVO> getUserInfo() {
        var actor = ActorContextHttpAdapter.currentActor();
        long id = actor.userId().value();
        UserVO v = userService.detailView(actor, id);
        if (v == null) return Result.fail("用户不存在");
        v.setTenants(authService.getUserTenants(actor, id));
        v.setCurrentTenantId(actor.tenantId().value());
        if (ActorContextHttpAdapter.homeTenantId() != null)
            v.setHomeTenantId(ActorContextHttpAdapter.homeTenantId());
        if (ActorContextHttpAdapter.switchMode() != null)
            v.setSwitchMode(ActorContextHttpAdapter.switchMode());
        return Result.success(v);
    }

    /**
     * {@code getUserList} 查询并返回当前操作所需的数据。
     *
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public Result<PageData<UserVO>> getUserList(@Valid UserPageRequest r) {
        return Result.success(
                userService.getUserList(
                        ActorContextHttpAdapter.currentActor(),
                        r.getUsername(),
                        r.getPageNum(),
                        r.getPageSize()));
    }

    /**
     * {@code createUser} 写入或更新当前模块中的业务数据。
     *
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:user:create")
    @PostMapping("/create")
    public Result<Map<String, Object>> createUser(@Valid @RequestBody UserRequest r) {
        return Result.success(
                userService.createUser(
                        ActorContextHttpAdapter.currentActor(),
                        r,
                        r.getRoleIds(),
                        r.getTenantId()));
    }

    /**
     * {@code updateUser} 写入或更新当前模块中的业务数据。
     *
     * @param userId 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:user:update")
    @PostMapping("/update")
    public Result<Void> updateUser(@RequestParam Long userId, @Valid @RequestBody UserRequest r) {
        return userService.updateUser(
                        ActorContextHttpAdapter.currentActor(),
                        userId,
                        r,
                        r.getRoleIds(),
                        r.getTenantId())
                ? Result.success()
                : Result.fail("用户不存在");
    }

    /**
     * {@code getTenantAssignments} 查询并返回当前操作所需的数据。
     *
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/tenant-assignments")
    public Result<List<UserTenantAssignmentVO>> getTenantAssignments(@RequestParam Long userId) {
        return Result.success(
                userService.getTenantAssignments(ActorContextHttpAdapter.currentActor(), userId));
    }

    /**
     * {@code replaceTenantAssignments} 执行当前类型定义的业务操作。
     *
     * @param userId 参数值，用于执行当前操作。
     * @param a 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:user:update")
    @PostMapping("/tenant-assignments/replace")
    public Result<Void> replaceTenantAssignments(
            @RequestParam Long userId, @RequestBody List<UserTenantRoleRequest> a) {
        return userService.replaceTenantAssignments(
                        ActorContextHttpAdapter.currentActor(), userId, a)
                ? Result.success()
                : Result.fail("租户分配失败");
    }

    /**
     * {@code deleteUser} 释放或移除当前操作涉及的资源。
     *
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:user:delete")
    @PostMapping("/delete")
    public Result<Void> deleteUser(@RequestParam Long userId) {
        return userService.deleteUser(ActorContextHttpAdapter.currentActor(), userId)
                ? Result.success()
                : Result.fail("用户不存在");
    }

    /**
     * {@code resetPassword} 执行当前类型定义的业务操作。
     *
     * @param userId 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:user:password")
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(
            @RequestParam Long userId, @Valid @RequestBody ResetPasswordRequest r) {
        return userService.resetUserPassword(
                                ActorContextHttpAdapter.currentActor(), userId, r.getPassword())
                        == null
                ? Result.fail("用户不存在")
                : Result.success();
    }

    /**
     * {@code changePassword} 执行当前类型定义的业务操作。
     *
     * @param userId 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/password/change")
    public Result<Void> changePassword(
            @RequestParam Long userId, @Valid @RequestBody ChangePasswordRequest r) {
        return userService.changePassword(
                        ActorContextHttpAdapter.currentActor(),
                        userId,
                        r.getOldPassword(),
                        r.getNewPassword())
                ? Result.success()
                : Result.fail("密码修改失败");
    }
}
