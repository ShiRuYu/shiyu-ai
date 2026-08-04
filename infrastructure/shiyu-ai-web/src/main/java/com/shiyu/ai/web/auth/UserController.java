package com.shiyu.ai.web.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.*;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.vo.UserTenantAssignmentVO;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    @GetMapping("/detail") public Result<UserVO> getUserInfo() { Long id=UserContextHolder.getUserId(); if(id==null)return Result.fail("用户未登录"); UserVO v=userService.detailView(id); if(v==null)return Result.fail("用户不存在"); v.setTenants(authService.getUserTenants(id)); if(UserContextHolder.getCurrentTenantId()!=null)v.setCurrentTenantId(UserContextHolder.getCurrentTenantId()); if(UserContextHolder.getHomeTenantId()!=null)v.setHomeTenantId(UserContextHolder.getHomeTenantId()); if(UserContextHolder.getSwitchMode()!=null)v.setSwitchMode(UserContextHolder.getSwitchMode()); return Result.success(v); }
    @SaCheckPermission("system:user:list") @GetMapping("/list") public Result<PageData<UserVO>> getUserList(@Valid UserPageRequest r) { return Result.success(userService.getUserList(r.getUsername(),r.getPageNum(),r.getPageSize())); }
    @SaCheckPermission("system:user:create") @PostMapping("/create") public Result<Map<String,Object>> createUser(@Valid @RequestBody UserRequest r) { return Result.success(userService.createUser(r,r.getRoleIds(),r.getTenantId())); }
    @SaCheckPermission("system:user:update") @PostMapping("/update") public Result<Void> updateUser(@RequestParam Long userId,@Valid @RequestBody UserRequest r) { return userService.updateUser(userId,r,r.getRoleIds(),r.getTenantId())?Result.success():Result.fail("用户不存在"); }
    @SaCheckPermission("system:user:list") @GetMapping("/tenant-assignments") public Result<List<UserTenantAssignmentVO>> getTenantAssignments(@RequestParam Long userId) { return Result.success(userService.getTenantAssignments(userId)); }
    @SaCheckPermission("system:user:update") @PostMapping("/tenant-assignments/replace") public Result<Void> replaceTenantAssignments(@RequestParam Long userId,@RequestBody List<UserTenantRoleRequest> a) { return userService.replaceTenantAssignments(userId,a)?Result.success():Result.fail("租户分配失败"); }
    @SaCheckPermission("system:user:delete") @PostMapping("/delete") public Result<Void> deleteUser(@RequestParam Long userId) { return userService.deleteUser(userId)?Result.success():Result.fail("用户不存在"); }
    @SaCheckPermission("system:user:password") @PostMapping("/password/reset") public Result<Void> resetPassword(@RequestParam Long userId,@Valid @RequestBody ResetPasswordRequest r) { return userService.resetUserPassword(userId,r.getPassword())==null?Result.fail("用户不存在"):Result.success(); }
    @PostMapping("/password/change") public Result<Void> changePassword(@RequestParam Long userId,@Valid @RequestBody ChangePasswordRequest r) { return userService.changePassword(userId,r.getOldPassword(),r.getNewPassword())?Result.success():Result.fail("密码修改失败"); }
}
