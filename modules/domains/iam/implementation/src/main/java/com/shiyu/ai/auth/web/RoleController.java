package com.shiyu.ai.auth.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.AssignUserRolesRequest;
import com.shiyu.ai.auth.request.RolePageRequest;
import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/iam/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    @SaCheckPermission("system:role:list") @GetMapping("/list")
    public Result<PageData<RoleVO>> getRoleList(@Valid RolePageRequest r) { return Result.success(roleService.getRoleList(ActorContextHttpAdapter.currentActor(), r.getPageNum(), r.getPageSize(), r.getName())); }
    @SaCheckPermission("system:role:list") @GetMapping("/all")
    public Result<List<RoleVO>> getAllRoles(@RequestParam(required=false) String status, @RequestParam Long tenantId) { return Result.success(roleService.allRolesView(ActorContextHttpAdapter.currentActor(), status, tenantId(tenantId))); }
    @SaCheckPermission("system:role:list") @GetMapping("/detail")
    public Result<RoleVO> getRoleDetail(@RequestParam Long id, @RequestParam Long tenantId) { var v=roleService.detailView(ActorContextHttpAdapter.currentActor(), id,tenantId(tenantId)); return v==null?Result.fail("角色不存在"):Result.success(v); }
    @SaCheckPermission("system:role:create") @PostMapping("/create")
    public Result<Void> createRole(@Valid @RequestBody RoleRequest r) { return roleService.createRole(ActorContextHttpAdapter.currentActor(), r)?Result.success():Result.fail("新增失败"); }
    @SaCheckPermission("system:role:update") @PostMapping("/update")
    public Result<Void> updateRole(@RequestParam Long id,@Valid @RequestBody RoleRequest r) { return roleService.updateRole(ActorContextHttpAdapter.currentActor(), id,r)?Result.success():Result.fail("角色不存在"); }
    @SaCheckPermission("system:role:assign") @PostMapping("/menus/replace")
    public Result<Void> replaceRoleMenus(@RequestParam Long id,@RequestParam Long tenantId,@RequestBody List<Long> menuIds) { return roleService.replaceRoleMenus(ActorContextHttpAdapter.currentActor(), id,tenantId(tenantId),menuIds)?Result.success():Result.fail("授权失败"); }
    @SaCheckPermission("system:role:delete") @PostMapping("/delete")
    public Result<Void> deleteRole(@RequestParam Long id) { return roleService.deleteRole(ActorContextHttpAdapter.currentActor(), id)?Result.success():Result.fail("角色不存在"); }
    @SaCheckPermission("system:role:assign") @PostMapping("/users/remove")
    public Result<Void> removeUserRoles(@RequestParam Long id,@Valid @RequestBody AssignUserRolesRequest r) { return roleService.removeUserRoles(ActorContextHttpAdapter.currentActor(), id,tenantId(r.getTenantId()),r.getUserIds())?Result.success():Result.fail("取消分配失败"); }
    @SaCheckPermission("system:role:assign") @PostMapping("/users/add")
    public Result<Void> assignUserRoles(@RequestParam Long id,@Valid @RequestBody AssignUserRolesRequest r) { return roleService.assignUserRoles(ActorContextHttpAdapter.currentActor(), id,tenantId(r.getTenantId()),r.getUserIds())?Result.success():Result.fail("分配失败"); }

    private static TenantId tenantId(Long value) { return value == null ? null : new TenantId(value); }
}
