package com.shiyu.ai.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.RolePageRequest;
import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.request.AssignUserRolesRequest;
import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.auth.vo.RolePageResponse;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * 角色管理 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Role", description = "Role")
@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get Role List")
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public Result<RolePageResponse> getRoleList(@Valid RolePageRequest request) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}",
                request.getPageNo(), request.getPageSize(), request.getName());
        if (request.getPageNo() == null) request.setPageNo(1);
        if (request.getPageSize() == null) request.setPageSize(10);
        return Result.success(roleService.getRoleList(request.getPageNo(), request.getPageSize(), request.getName()));
    }

    @Operation(summary = "Get All Roles")
    @SaCheckPermission("system:role:list")
    @GetMapping("/all")
    public Result<List<RoleBO>> getAllRoles(@RequestParam(required = false) String status) {
        log.info("获取所有角色，status: {}", status);
        return Result.success(roleService.getAllRoles(status));
    }

    @Operation(summary = "Get Role Detail")
    @SaCheckPermission("system:role:list")
    @GetMapping("/detail")
    public Result<RoleVO> getRoleDetail(@RequestParam Long id) {
        log.info("查询角色详情，id: {}", id);
        RoleBO bo = roleService.getRoleDetail(id);
        if (bo == null) return Result.fail("角色不存在");
        return Result.success(MapstructUtils.convert(bo, RoleVO.class));
    }

    @Operation(summary = "Create Role")
    @SaCheckPermission("system:role:create")
    @PostMapping("/create")
    public Result<Void> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("新增角色");
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        return roleService.createRole(roleBO) ? Result.success() : Result.fail("新增失败");
    }

    @Operation(summary = "Update Role")
    @SaCheckPermission("system:role:update")
    @PostMapping("/update")
    public Result<Void> updateRole(@RequestParam Long id, @Valid @RequestBody RoleRequest request) {
        log.info("修改角色，id: {}", id);
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        return roleService.updateRole(id, roleBO) ? Result.success() : Result.fail("角色不存在");
    }

    @Operation(summary = "Delete Role")
    @SaCheckPermission("system:role:delete")
    @PostMapping("/delete")
    public Result<Void> deleteRole(@RequestParam Long id) {
        log.info("删除角色，id: {}", id);
        return roleService.deleteRole(id) ? Result.success() : Result.fail("角色不存在");
    }

    @Operation(summary = "Remove User Roles")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/users/remove")
    public Result<Void> removeUserRoles(@RequestParam Long id, @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("取消分配角色，id: {}, userIds: {}", id, request.getUserIds());
        return roleService.removeUserRoles(id, request.getUserIds()) ? Result.success() : Result.fail("取消分配失败");
    }

    @Operation(summary = "Assign User Roles")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/users/add")
    public Result<Void> assignUserRoles(@RequestParam Long id, @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("分配角色，id: {}, userIds: {}", id, request.getUserIds());
        return roleService.assignUserRoles(id, request.getUserIds()) ? Result.success() : Result.fail("分配失败");
    }
}
