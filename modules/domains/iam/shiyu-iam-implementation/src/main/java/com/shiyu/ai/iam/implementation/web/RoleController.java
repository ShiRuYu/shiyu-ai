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
 * {@code RoleController} 是Web模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
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
     * {@code getRoleList} 查询并返回当前操作所需的数据。
     *
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code getAllRoles} 查询并返回当前操作所需的数据。
     *
     * @param status 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code getRoleDetail} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code createRole} 写入或更新当前模块中的业务数据。
     *
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:role:create")
    @PostMapping("/create")
    public Result<Void> createRole(@Valid @RequestBody RoleRequest r) {
        return roleService.createRole(ActorContextHttpAdapter.currentActor(), r)
                ? Result.success()
                : Result.fail("新增失败");
    }

    /**
     * {@code updateRole} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:role:update")
    @PostMapping("/update")
    public Result<Void> updateRole(@RequestParam Long id, @Valid @RequestBody RoleRequest r) {
        return roleService.updateRole(ActorContextHttpAdapter.currentActor(), id, r)
                ? Result.success()
                : Result.fail("角色不存在");
    }

    /**
     * {@code replaceRoleMenus} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param menuIds 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code deleteRole} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @SaCheckPermission("system:role:delete")
    @PostMapping("/delete")
    public Result<Void> deleteRole(@RequestParam Long id) {
        return roleService.deleteRole(ActorContextHttpAdapter.currentActor(), id)
                ? Result.success()
                : Result.fail("角色不存在");
    }

    /**
     * {@code removeUserRoles} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code assignUserRoles} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param r 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
