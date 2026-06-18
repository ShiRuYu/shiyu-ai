package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.bo.SysRoleBO;
import com.shiyu.ai.auth.service.SysRoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器
 * @author shiyu-ai
 */
@Tag(name = "角色管理", description = "角色权限接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * 获取角色列表
     */
    @GetMapping
    public Result<Pair<Long, List<SysRoleBO>>> getRoles(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysRoleService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{roleId}")
    public Result<SysRoleBO> getRole(@PathVariable Long roleId) {
        return Result.success(sysRoleService.getById(roleId));
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Result<SysRoleBO> createRole(@Valid @RequestBody SysRoleBO sysRoleBO) {
        return Result.success(sysRoleService.create(sysRoleBO));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{roleId}")
    public Result<SysRoleBO> updateRole(@PathVariable Long roleId, @Valid @RequestBody SysRoleBO sysRoleBO) {
        sysRoleBO.setRoleId(roleId);
        return Result.success(sysRoleService.update(sysRoleBO));
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        sysRoleService.deleteById(roleId);
        return Result.success();
    }
}


