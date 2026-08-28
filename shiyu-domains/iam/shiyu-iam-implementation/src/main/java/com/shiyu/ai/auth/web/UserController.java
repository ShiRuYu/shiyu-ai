package com.shiyu.ai.auth.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.*;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.vo.UserTenantAssignmentVO;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iam/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    @GetMapping("/detail") public Result<UserVO> getUserInfo() { var actor=ActorContextHttpAdapter.currentActor(); long id=actor.userId().value(); UserVO v=userService.detailView(actor,id); if(v==null)return Result.fail("用户不存在"); v.setTenants(authService.getUserTenants(actor,id)); v.setCurrentTenantId(actor.tenantId().value()); if(ActorContextHttpAdapter.homeTenantId()!=null)v.setHomeTenantId(ActorContextHttpAdapter.homeTenantId()); if(ActorContextHttpAdapter.switchMode()!=null)v.setSwitchMode(ActorContextHttpAdapter.switchMode()); return Result.success(v); }
    @SaCheckPermission("system:user:list") @GetMapping("/list") public Result<PageData<UserVO>> getUserList(@Valid UserPageRequest r) { return Result.success(userService.getUserList(ActorContextHttpAdapter.currentActor(),r.getUsername(),r.getPageNum(),r.getPageSize())); }
    @SaCheckPermission("system:user:create") @PostMapping("/create") public Result<Map<String,Object>> createUser(@Valid @RequestBody UserRequest r) { return Result.success(userService.createUser(ActorContextHttpAdapter.currentActor(),r,r.getRoleIds(),r.getTenantId())); }
    @SaCheckPermission("system:user:update") @PostMapping("/update") public Result<Void> updateUser(@RequestParam Long userId,@Valid @RequestBody UserRequest r) { return userService.updateUser(ActorContextHttpAdapter.currentActor(),userId,r,r.getRoleIds(),r.getTenantId())?Result.success():Result.fail("用户不存在"); }
    @SaCheckPermission("system:user:list") @GetMapping("/tenant-assignments") public Result<List<UserTenantAssignmentVO>> getTenantAssignments(@RequestParam Long userId) { return Result.success(userService.getTenantAssignments(ActorContextHttpAdapter.currentActor(),userId)); }
    @SaCheckPermission("system:user:update") @PostMapping("/tenant-assignments/replace") public Result<Void> replaceTenantAssignments(@RequestParam Long userId,@RequestBody List<UserTenantRoleRequest> a) { return userService.replaceTenantAssignments(ActorContextHttpAdapter.currentActor(),userId,a)?Result.success():Result.fail("租户分配失败"); }
    @SaCheckPermission("system:user:delete") @PostMapping("/delete") public Result<Void> deleteUser(@RequestParam Long userId) { return userService.deleteUser(ActorContextHttpAdapter.currentActor(),userId)?Result.success():Result.fail("用户不存在"); }
    @SaCheckPermission("system:user:password") @PostMapping("/password/reset") public Result<Void> resetPassword(@RequestParam Long userId,@Valid @RequestBody ResetPasswordRequest r) { return userService.resetUserPassword(ActorContextHttpAdapter.currentActor(),userId,r.getPassword())==null?Result.fail("用户不存在"):Result.success(); }
    @PostMapping("/password/change") public Result<Void> changePassword(@RequestParam Long userId,@Valid @RequestBody ChangePasswordRequest r) { return userService.changePassword(ActorContextHttpAdapter.currentActor(),userId,r.getOldPassword(),r.getNewPassword())?Result.success():Result.fail("密码修改失败"); }
}
