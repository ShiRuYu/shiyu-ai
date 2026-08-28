package com.shiyu.ai.auth.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.MenuRequest;
import com.shiyu.ai.auth.vo.RouteMenuVO;
import com.shiyu.ai.auth.vo.MenuVO;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.auth.request.MenuPageRequest;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu Controller
 */
@Slf4j
@Tag(name = "Menu", description = "Menu")
@RestController
@RequestMapping("/api/iam/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "Get All Menus")
    @GetMapping("/all")
    public Result<List<RouteMenuVO>> getAllMenus() {
        log.info("getAllMenus");
        try {
            return Result.success(menuService.routeMenusView(ActorContextHttpAdapter.currentActor()));
        } catch (Exception e) {
            log.error("操作失败", e);
            return Result.fail("操作失败");
        }
    }

    @Operation(summary = "Get System Menu List")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public Result<List<MenuVO>> getSystemMenuList() {
        return Result.success(menuService.allTreeView(ActorContextHttpAdapter.currentActor()));
    }

    @Operation(summary = "Get System Menu Page")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/page")
    public Result<PageData<MenuVO>> getMenuPage(@Valid MenuPageRequest request) {
        return Result.success(menuService.getMenuPage(ActorContextHttpAdapter.currentActor(),
                request.getPageNum(), request.getPageSize(), request.getName(),
                request.getCode(), request.getType(), request.getStatus()));
    }

    @Operation(summary = "Get Menu Roots")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/roots")
    public Result<List<RouteMenuVO>> getMenuRoots() {
        return Result.success(menuService.menuRootsView(ActorContextHttpAdapter.currentActor()));
    }

    @Operation(summary = "Get Menu Children")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/children")
    public Result<List<RouteMenuVO>> getMenuChildren(@RequestParam Long parentId) {
        return Result.success(menuService.childrenView(ActorContextHttpAdapter.currentActor(), parentId));
    }

    @Operation(summary = "Get Menu Permissions Tree")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/permissions")
    public Result<List<RouteMenuVO>> getMenuPermissionsTree() {
        return Result.success(menuService.permissionsView(ActorContextHttpAdapter.currentActor()));
    }

    @Operation(summary = "Get All Tree")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public Result<List<RouteMenuVO>> getAllTree() {
        return Result.success(menuService.treeView(ActorContextHttpAdapter.currentActor()));
    }

    @Operation(summary = "Delete Menu")
    @SaCheckPermission("system:menu:delete")
    @PostMapping("/delete")
    public Result<Void> deleteMenu(@RequestParam Long id) {
        return menuService.deleteMenu(ActorContextHttpAdapter.currentActor(), id) ? Result.success() : Result.fail("delete fail");
    }

    @Operation(summary = "Create Menu")
    @SaCheckPermission("system:menu:create")
    @PostMapping("/create")
    public Result<Void> createMenu(@Valid @RequestBody MenuRequest request) {
        return menuService.createMenu(ActorContextHttpAdapter.currentActor(), request) ? Result.success() : Result.fail("create fail");
    }

    @Operation(summary = "Update Menu")
    @SaCheckPermission("system:menu:update")
    @PostMapping("/update")
    public Result<Void> updateMenu(@RequestParam Long id, @Valid @RequestBody MenuRequest request) {
        return menuService.updateMenu(ActorContextHttpAdapter.currentActor(), id, request) ? Result.success() : Result.fail("update fail");
    }

    @Operation(summary = "Is Menu Name Exists")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/name-exists")
    public Result<Boolean> isMenuNameExists(@RequestParam String name, @RequestParam(required = false) Long id) {
        return Result.success(menuService.isMenuNameExists(ActorContextHttpAdapter.currentActor(), name, id));
    }

    @Operation(summary = "Is Menu Path Exists")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/path-exists")
    public Result<Boolean> isMenuPathExists(@RequestParam String path, @RequestParam(required = false) Long id) {
        return Result.success(menuService.isMenuPathExists(ActorContextHttpAdapter.currentActor(), path, id));
    }

}
