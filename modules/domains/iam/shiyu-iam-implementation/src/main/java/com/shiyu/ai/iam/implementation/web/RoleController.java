package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.request.AssignUserRolesRequest;
import com.shiyu.ai.iam.implementation.request.RolePageRequest;
import com.shiyu.ai.iam.implementation.request.RoleRequest;
import com.shiyu.ai.iam.implementation.service.RoleService;
import com.shiyu.ai.iam.implementation.vo.RoleVO;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 角色 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/iam/roles")
@RequiredArgsConstructor
public class RoleController {
    /**
     * 角色服务，表示当前对象中的对应属性。
     */
    private final RoleService roleService;

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public Result<PageData<RoleVO>> getRoleList(@Valid RolePageRequest r) {
        return Result.success(
                roleService.getRoleList(
                        ActorContextHttpAdapter.currentActor(),
                        r.getPageNum(),
                        r.getPageSize(),
                        r.getName()));
    }

    /**
     * 查询 角色 相关业务数据，并返回处理结果。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/all")
    public Result<List<RoleVO>> getAllRoles(
            @RequestParam(required = false) String status, @RequestParam Long tenantId) {
        return Result.success(
                roleService.allRolesView(
                        ActorContextHttpAdapter.currentActor(), status, tenantId(tenantId)));
    }

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param list 用于完成本次业务处理的 list 参数。
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/detail")
    public Result<RoleVO> getRoleDetail(@RequestParam Long id, @RequestParam Long tenantId) {
        var v =
                roleService.detailView(
                        ActorContextHttpAdapter.currentActor(), id, tenantId(tenantId));
        return v == null ? Result.fail("角色不存在") : Result.success(v);
    }

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     */
    @SaCheckPermission("system:role:create")
    @PostMapping("/create")
    public Result<Void> createRole(@Valid @RequestBody RoleRequest r) {
        return roleService.createRole(ActorContextHttpAdapter.currentActor(), r)
                ? Result.success()
                : Result.fail("新增失败");
    }

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param update 用于完成本次业务处理的 update 参数。
     */
    @SaCheckPermission("system:role:update")
    @PostMapping("/update")
    public Result<Void> updateRole(@RequestParam Long id, @Valid @RequestBody RoleRequest r) {
        return roleService.updateRole(ActorContextHttpAdapter.currentActor(), id, r)
                ? Result.success()
                : Result.fail("角色不存在");
    }

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param assign 用于完成本次业务处理的 assign 参数。
     */
    @SaCheckPermission("system:role:assign")
    @PostMapping("/menus/replace")
    public Result<Void> replaceRoleMenus(
            @RequestParam Long id, @RequestParam Long tenantId, @RequestBody List<Long> menuIds) {
        return roleService.replaceRoleMenus(
                        ActorContextHttpAdapter.currentActor(), id, tenantId(tenantId), menuIds)
                ? Result.success()
                : Result.fail("授权失败");
    }

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param delete 用于完成本次业务处理的 delete 参数。
     */
    @SaCheckPermission("system:role:delete")
    @PostMapping("/delete")
    public Result<Void> deleteRole(@RequestParam Long id) {
        return roleService.deleteRole(ActorContextHttpAdapter.currentActor(), id)
                ? Result.success()
                : Result.fail("角色不存在");
    }

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param assign 用于完成本次业务处理的 assign 参数。
     */
    @SaCheckPermission("system:role:assign")
    @PostMapping("/users/remove")
    public Result<Void> removeUserRoles(
            @RequestParam Long id, @Valid @RequestBody AssignUserRolesRequest r) {
        return roleService.removeUserRoles(
                        ActorContextHttpAdapter.currentActor(),
                        id,
                        tenantId(r.getTenantId()),
                        r.getUserIds())
                ? Result.success()
                : Result.fail("取消分配失败");
    }

    /**
     * 执行 角色 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param assign 用于完成本次业务处理的 assign 参数。
     */
    @SaCheckPermission("system:role:assign")
    @PostMapping("/users/add")
    public Result<Void> assignUserRoles(
            @RequestParam Long id, @Valid @RequestBody AssignUserRolesRequest r) {
        return roleService.assignUserRoles(
                        ActorContextHttpAdapter.currentActor(),
                        id,
                        tenantId(r.getTenantId()),
                        r.getUserIds())
                ? Result.success()
                : Result.fail("分配失败");
    }

    private static TenantId tenantId(Long value) {
        return value == null ? null : new TenantId(value);
    }
}
