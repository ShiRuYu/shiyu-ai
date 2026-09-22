package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 用户 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param detail 用于完成本次业务处理的 detail 参数。
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
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("system:user:list")
    @GetMapping
    public Result<PageData<UserVO>> getUserList(@Valid UserPageRequest r) {
        return Result.success(
                userService.getUserList(
                        ActorContextHttpAdapter.currentActor(),
                        r.getUsername(),
                        r.getPageNum(),
                        r.getPageSize()));
    }

    /**
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @SaCheckPermission("system:user:create")
    @PostMapping
    public Result<Map<String, Object>> createUser(@Valid @RequestBody UserRequest r) {
        return Result.success(
                userService.createUser(
                        ActorContextHttpAdapter.currentActor(),
                        r,
                        r.getRoleIds(),
                        r.getTenantId()));
    }

    /**
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @SaCheckPermission("system:user:update")
    @PutMapping("/{id}")
    public Result<Void> updateUser(
            @PathVariable("id") Long userId, @Valid @RequestBody UserRequest r) {
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
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/tenant-assignments")
    public Result<List<UserTenantAssignmentVO>> getTenantAssignments(@RequestParam Long userId) {
        return Result.success(
                userService.getTenantAssignments(ActorContextHttpAdapter.currentActor(), userId));
    }

    /**
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
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
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @SaCheckPermission("system:user:delete")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(ActorContextHttpAdapter.currentActor(), userId)
                ? Result.success()
                : Result.fail("用户不存在");
    }

    /**
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param password 用于完成本次业务处理的 password 参数。
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
     * 执行 用户 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param change 用于完成本次业务处理的 change 参数。
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
