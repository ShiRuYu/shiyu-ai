package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.request.RolePageRequest;
import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.request.AssignUserRolesRequest;
import com.shiyu.ai.dal.bo.auth.RoleBO;
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

    /**
     * 角色列表 - 分页
     */
    @Operation(summary = "Get Role List")
    @GetMapping("/list")
    public Result<RolePageResponse> getRoleList(@Valid RolePageRequest request) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}",
                request.getPageNo(), request.getPageSize(), request.getName());
        if (request.getPageNo() == null) request.setPageNo(1);
        if (request.getPageSize() == null) request.setPageSize(10);
        return Result.success(roleService.getRoleList(request.getPageNo(), request.getPageSize(), request.getName()));
    }

    /**
     * 角色列表-all
     */
    @Operation(summary = "Get All Roles")
    @GetMapping("")
    public Result<List<RoleBO>> getAllRoles(@RequestParam(required = false) String status) {
        log.info("获取所有角色，status: {}", status);
        return Result.success(roleService.getAllRoles(status));
    }

    /**
     * 修改角色
     */
    @Operation(summary = "Update Role")
    @PatchMapping("/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        log.info("修改角色，id: {}", id);
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        return roleService.updateRole(id, roleBO) ? Result.success() : Result.fail("角色不存在");
    }

    /**
     * 修改角色（PUT）
     */
    @Operation(summary = "Put Role")
    @PutMapping("/{id}")
    public Result<Void> putRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        log.info("修改角色，id: {}", id);
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        return roleService.updateRole(id, roleBO) ? Result.success() : Result.fail("角色不存在");
    }

    /**
     * 删除角色
     */
    @Operation(summary = "Delete Role")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        log.info("删除角色，id: {}", id);
        return roleService.deleteRole(id) ? Result.success() : Result.fail("角色不存在");
    }

    /**
     * 取消分配角色 - 批量
     */
    @Operation(summary = "Remove User Roles")
    @PatchMapping("/users/remove/{id}")
    public Result<Void> removeUserRoles(@PathVariable Long id, @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("取消分配角色，id: {}, userIds: {}", id, request.getUserIds());
        return roleService.removeUserRoles(id, request.getUserIds()) ? Result.success() : Result.fail("取消分配失败");
    }

    /**
     * 分配角色 - 批量
     */
    @Operation(summary = "Assign User Roles")
    @PatchMapping("/users/add/{id}")
    public Result<Void> assignUserRoles(@PathVariable Long id, @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("分配角色，id: {}, userIds: {}", id, request.getUserIds());
        return roleService.assignUserRoles(id, request.getUserIds()) ? Result.success() : Result.fail("分配失败");
    }

    /**
     * 新增角色
     */
    @Operation(summary = "Create Role")
    @PostMapping("")
    public Result<Void> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("新增角色");
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        return roleService.createRole(roleBO) ? Result.success() : Result.fail("新增失败");
    }
}
