package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.bo.SysMenuBO;
import com.shiyu.ai.auth.service.SysMenuService;
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
 * 菜单管理控制器
 *
 * @author shiyu-ai
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/menus")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 获取菜单列表
     */
    @GetMapping
    public Result<Pair<Long, List<SysMenuBO>>> getMenus(@RequestParam(defaultValue = "1") Number pageNumber,
                                                         @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysMenuService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{menuId}")
    public Result<SysMenuBO> getMenu(@PathVariable Long menuId) {
        return Result.success(sysMenuService.getById(menuId));
    }

    /**
     * 创建菜单
     */
    @PostMapping
    public Result<SysMenuBO> createMenu(@RequestBody SysMenuBO sysMenuBO) {
        return Result.success(sysMenuService.create(sysMenuBO));
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{menuId}")
    public Result<SysMenuBO> updateMenu(@PathVariable Long menuId, @RequestBody SysMenuBO sysMenuBO) {
        sysMenuBO.setMenuId(menuId);
        return Result.success(sysMenuService.update(sysMenuBO));
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{menuId}")
    public Result<Void> deleteMenu(@PathVariable Long menuId) {
        sysMenuService.deleteById(menuId);
        return Result.success();
    }
}
