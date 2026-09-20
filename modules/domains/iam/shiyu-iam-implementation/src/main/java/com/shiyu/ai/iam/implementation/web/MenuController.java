package com.shiyu.ai.iam.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.request.MenuPageRequest;
import com.shiyu.ai.iam.implementation.request.MenuRequest;
import com.shiyu.ai.iam.implementation.service.MenuService;
import com.shiyu.ai.iam.implementation.vo.MenuVO;
import com.shiyu.ai.iam.implementation.vo.RouteMenuVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 Menu 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@Tag(name = "Menu", description = "Menu")
@RestController
@RequestMapping("/api/iam/menus")
public class MenuController {

    /**
     * menuService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MenuService menuService;

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param menuService 用于完成本次业务处理的 menuService 参数。
     */
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Menus 用于完成本次业务处理的 Menus 参数。
     */
    @Operation(summary = "Get All Menus")
    @GetMapping("/all")
    public Result<List<RouteMenuVO>> getAllMenus() {
        log.info("getAllMenus");
        try {
            return Result.success(
                    menuService.routeMenusView(ActorContextHttpAdapter.currentActor()));
        } catch (Exception e) {
            log.error("操作失败", e);
            return Result.fail("操作失败");
        }
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param List 用于完成本次业务处理的 List 参数。
     */
    @Operation(summary = "Get System Menu List")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public Result<List<MenuVO>> getSystemMenuList() {
        return Result.success(menuService.allTreeView(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Page 分页页码，从 1 开始。
     */
    @Operation(summary = "Get System Menu Page")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/page")
    public Result<PageData<MenuVO>> getMenuPage(@Valid MenuPageRequest request) {
        return Result.success(
                menuService.getMenuPage(
                        ActorContextHttpAdapter.currentActor(),
                        request.getPageNum(),
                        request.getPageSize(),
                        request.getName(),
                        request.getCode(),
                        request.getType(),
                        request.getStatus()));
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Roots 用于完成本次业务处理的 Roots 参数。
     */
    @Operation(summary = "Get Menu Roots")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/roots")
    public Result<List<RouteMenuVO>> getMenuRoots() {
        return Result.success(menuService.menuRootsView(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Children 用于完成本次业务处理的 Children 参数。
     */
    @Operation(summary = "Get Menu Children")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/children")
    public Result<List<RouteMenuVO>> getMenuChildren(@RequestParam Long parentId) {
        return Result.success(
                menuService.childrenView(ActorContextHttpAdapter.currentActor(), parentId));
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Tree 用于完成本次业务处理的 Tree 参数。
     */
    @Operation(summary = "Get Menu Permissions Tree")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/permissions")
    public Result<List<RouteMenuVO>> getMenuPermissionsTree() {
        return Result.success(menuService.permissionsView(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Tree 用于完成本次业务处理的 Tree 参数。
     */
    @Operation(summary = "Get All Tree")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public Result<List<RouteMenuVO>> getAllTree() {
        return Result.success(menuService.treeView(ActorContextHttpAdapter.currentActor()));
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Menu 用于完成本次业务处理的 Menu 参数。
     */
    @Operation(summary = "Delete Menu")
    @SaCheckPermission("system:menu:delete")
    @PostMapping("/delete")
    public Result<Void> deleteMenu(@RequestParam Long id) {
        return menuService.deleteMenu(ActorContextHttpAdapter.currentActor(), id)
                ? Result.success()
                : Result.fail("delete fail");
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Menu 用于完成本次业务处理的 Menu 参数。
     */
    @Operation(summary = "Create Menu")
    @SaCheckPermission("system:menu:create")
    @PostMapping("/create")
    public Result<Void> createMenu(@Valid @RequestBody MenuRequest request) {
        return menuService.createMenu(ActorContextHttpAdapter.currentActor(), request)
                ? Result.success()
                : Result.fail("create fail");
    }

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Menu 用于完成本次业务处理的 Menu 参数。
     */
    @Operation(summary = "Update Menu")
    @SaCheckPermission("system:menu:update")
    @PostMapping("/update")
    public Result<Void> updateMenu(@RequestParam Long id, @Valid @RequestBody MenuRequest request) {
        return menuService.updateMenu(ActorContextHttpAdapter.currentActor(), id, request)
                ? Result.success()
                : Result.fail("update fail");
    }

    /**
     * {@code isMenuNameExists} 校验当前操作的输入或状态是否满足约束。
     *
     * @param name 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Is Menu Name Exists")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/name-exists")
    public Result<Boolean> isMenuNameExists(
            @RequestParam String name, @RequestParam(required = false) Long id) {
        return Result.success(
                menuService.isMenuNameExists(ActorContextHttpAdapter.currentActor(), name, id));
    }

    /**
     * {@code isMenuPathExists} 校验当前操作的输入或状态是否满足约束。
     *
     * @param path 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Is Menu Path Exists")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/path-exists")
    public Result<Boolean> isMenuPathExists(
            @RequestParam String path, @RequestParam(required = false) Long id) {
        return Result.success(
                menuService.isMenuPathExists(ActorContextHttpAdapter.currentActor(), path, id));
    }
}
