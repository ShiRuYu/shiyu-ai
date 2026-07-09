package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.request.AssignUserRolesRequest;
import com.shiyu.ai.auth.bo.RoleBO;
import com.shiyu.ai.auth.vo.RolePageResponse;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.common.core.api.PageQuery;
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
@RequestMapping("/auth/role")
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
    public Result<RolePageResponse> getRoleList(
            @RequestParam(required = false) String name,
            PageQuery pageQuery) {
        Integer pageNo = pageQuery != null && pageQuery.getPageNum() != null ? pageQuery.getPageNum() : 1;
        Integer pageSize = pageQuery != null && pageQuery.getPageSize() != null ? pageQuery.getPageSize() : 10;
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        RolePageResponse pageResponse = roleService.getRoleList(pageNo, pageSize, name);
        
        return Result.success(pageResponse);
    }

    /**
     * 角色列表-all
     */
    @Operation(summary = "Get All Roles")
    @GetMapping("/all")
    public Result<List<RoleBO>> getAllRoles(
            @RequestParam(required = false) String status) {
        log.info("获取所有角色，status: {}", status);
        
        List<RoleBO> roles = roleService.getAllRoles(status);
        
        return Result.success(roles);
    }

    /**
     * 修改角色
     */
    @Operation(summary = "Get Role Detail")
    @GetMapping("/detail")
    public Result<RoleBO> getRoleDetail(@RequestParam Long id) {
        log.info("获取角色详情，id: {}", id);
        RoleBO role = roleService.getRoleDetail(id);
        return role != null ? Result.success(role) : Result.fail("角色不存在");
    }

    @Operation(summary = "Update Role")
    @PostMapping("/update")
    public Result<Void> updateRole(
            @RequestParam Long id,
            @Valid @RequestBody RoleRequest request) {
        log.info("修改角色，id: {}", id);
        
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        boolean success = roleService.updateRole(id, roleBO);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("角色不存在");
        }
    }

    /**
     * 删除角色
     */
    @Operation(summary = "Delete Role")
    @PostMapping("/delete")
    public Result<Void> deleteRole(@RequestParam Long id) {
        log.info("删除角色，id: {}", id);
        
        boolean success = roleService.deleteRole(id);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("角色不存在");
        }
    }

    /**
     * 取消分配角色 - 批量
     */
    @Operation(summary = "Remove User Roles")
    @PostMapping("/user/remove")
    public Result<Void> removeUserRoles(
            @RequestParam Long id,
            @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("取消分配角色，id: {}, userIds: {}", id, request.getUserIds());
        
        boolean success = roleService.removeUserRoles(id, request.getUserIds());
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("取消分配失败");
        }
    }

    /**
     * 分配角色 - 批量
     */
    @Operation(summary = "Assign User Roles")
    @PostMapping("/user/add")
    public Result<Void> assignUserRoles(
            @RequestParam Long id,
            @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("分配角色，id: {}, userIds: {}", id, request.getUserIds());
        
        boolean success = roleService.assignUserRoles(id, request.getUserIds());
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("分配失败");
        }
    }

    /**
     * 新增角色
     */
    @Operation(summary = "Create Role")
    @PostMapping("/create")
    public Result<Void> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("新增角色");
        
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        boolean success = roleService.createRole(roleBO);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("新增失败");
        }
    }
}
