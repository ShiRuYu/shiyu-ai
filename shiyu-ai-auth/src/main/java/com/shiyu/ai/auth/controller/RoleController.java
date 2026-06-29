package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.request.AssignUserRolesRequest;
import com.shiyu.ai.auth.bo.RoleBO;
import com.shiyu.ai.auth.vo.RolePageResponse;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 Controller
 */
@Slf4j
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
    @GetMapping("/list")
    public Result<RolePageResponse> getRoleList(
            @RequestParam(required = false,name = "page") Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String name) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        // 设置默认值
        if (pageNo == null) pageNo = 1;
        if (pageSize == null) pageSize = 10;
        
        RolePageResponse pageResponse = roleService.getRoleList(pageNo, pageSize, name);
        
        return Result.success(pageResponse);
    }

    /**
     * 角色列表-all
     */
    @GetMapping("")
    public Result<List<RoleBO>> getAllRoles(
            @RequestParam(required = false) String status) {
        log.info("获取所有角色，status: {}", status);
        
        List<RoleBO> roles = roleService.getAllRoles(status);
        
        return Result.success(roles);
    }

    /**
     * 修改角色
     */
    @PatchMapping("/{id}")
    public Result<Void> updateRole(
            @PathVariable Long id,
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
     * 修改角色
     */
    @PutMapping("/{id}")
    public Result<Void> putRole(
            @PathVariable Long id,
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
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
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
    @PatchMapping("/users/remove/{id}")
    public Result<Void> removeUserRoles(
            @PathVariable Long id,
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
    @PatchMapping("/users/add/{id}")
    public Result<Void> assignUserRoles(
            @PathVariable Long id,
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
    @PostMapping("")
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
