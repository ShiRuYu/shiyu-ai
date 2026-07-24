package com.shiyu.ai.web.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.auth.request.MenuRequest;
import com.shiyu.ai.dal.auth.bo.MenuBO;
import com.shiyu.ai.auth.vo.RouteMenuVO;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
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
@RequestMapping("/menu")
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
            Long userId = LoginContextHolder.getUserId();
            List<MenuBO> menuBOs = menuService.getRouteMenusByUserId(userId);
            return Result.success(convertToRouteMenuVO(menuBOs));
        } catch (Exception e) {
            log.error("鎿嶄綔澶辫触", e);
            return Result.fail("鎿嶄綔澶辫触");
        }
    }

    @Operation(summary = "Get System Menu List")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    public Result<List<RouteMenuVO>> getSystemMenuList() {
        return Result.success(convertToRouteMenuVO(menuService.getAllTree()));
    }

    @Operation(summary = "Get Menu Roots")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/roots")
    public Result<List<RouteMenuVO>> getMenuRoots() {
        return Result.success(convertToRouteMenuVO(menuService.getMenuRoots()));
    }

    @Operation(summary = "Get Menu Children")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/children")
    public Result<List<RouteMenuVO>> getMenuChildren(@RequestParam Long parentId) {
        return Result.success(convertToRouteMenuVO(menuService.getChildrenByParentId(parentId)));
    }

    private List<RouteMenuVO> convertToRouteMenuVO(List<MenuBO> menuBOs) {
        if (menuBOs == null || menuBOs.isEmpty()) return new ArrayList<>();
        List<RouteMenuVO> result = new ArrayList<>();
        for (MenuBO menuBO : menuBOs) {
            RouteMenuVO vo = new RouteMenuVO();
            vo.setId(menuBO.getId());
            vo.setPid(menuBO.getParentId());
            vo.setName(menuBO.getCode());
            vo.setPath(menuBO.getPath());
            vo.setComponent(menuBO.getComponent());
            vo.setRedirect(menuBO.getRedirect());
            String type = menuBO.getType();
            if ("MENU".equals(type)) vo.setType("menu");
            else if ("CATALOG".equals(type)) vo.setType("catalog");
            else vo.setType(type != null ? type.toLowerCase() : "menu");
            vo.setStatus(menuBO.getStatus());
            // 菜单只负责展示，后端权限统一由 auth_code 表提供。
            vo.setAuthCode(null);
            vo.setIcon(menuBO.getIcon());
            RouteMenuVO.MetaVO meta = new RouteMenuVO.MetaVO();
            meta.setTitle(menuBO.getName());
            meta.setIcon(menuBO.getIcon());
            meta.setOrder(menuBO.getOrder());
            meta.setKeepAlive(menuBO.getKeepAlive());
            if (menuBO.getShow() != null && !menuBO.getShow()) meta.setHideInMenu(true);
            String layout = menuBO.getLayout();
            if ("none".equalsIgnoreCase(layout) || "false".equalsIgnoreCase(layout)) meta.setNoBasicLayout(true);
            vo.setMeta(meta);
            if (menuBO.getChildren() != null && !menuBO.getChildren().isEmpty())
                vo.setChildren(convertToRouteMenuVO(menuBO.getChildren()));
            result.add(vo);
        }
        return result;
    }

    @Operation(summary = "Get Menu Permissions Tree")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/permissions")
    public Result<List<RouteMenuVO>> getMenuPermissionsTree() {
        return Result.success(convertToRouteMenuVO(menuService.getMenuPermissionsTree()));
    }

    @Operation(summary = "Get All Tree")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/tree")
    public Result<List<RouteMenuVO>> getAllTree() {
        return Result.success(convertToRouteMenuVO(menuService.getAllTree()));
    }

    @Operation(summary = "Delete Menu")
    @SaCheckPermission("system:menu:delete")
    @PostMapping("/delete")
    public Result<Void> deleteMenu(@RequestParam Long id) {
        return menuService.deleteMenu(id) ? Result.success() : Result.fail("delete fail");
    }

    @Operation(summary = "Create Menu")
    @SaCheckPermission("system:menu:create")
    @PostMapping("/create")
    public Result<Void> createMenu(@Valid @RequestBody MenuRequest request) {
        return menuService.createMenu(MapstructUtils.convert(request, MenuBO.class)) ? Result.success() : Result.fail("create fail");
    }

    @Operation(summary = "Update Menu")
    @SaCheckPermission("system:menu:update")
    @PostMapping("/update")
    public Result<Void> updateMenu(@RequestParam Long id, @Valid @RequestBody MenuRequest request) {
        return menuService.updateMenu(id, MapstructUtils.convert(request, MenuBO.class)) ? Result.success() : Result.fail("update fail");
    }

    @Operation(summary = "Is Menu Name Exists")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/name-exists")
    public Result<Boolean> isMenuNameExists(@RequestParam String name, @RequestParam(required = false) Long id) {
        return Result.success(menuService.isMenuNameExists(name, id));
    }

    @Operation(summary = "Is Menu Path Exists")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/path-exists")
    public Result<Boolean> isMenuPathExists(@RequestParam String path, @RequestParam(required = false) Long id) {
        return Result.success(menuService.isMenuPathExists(path, id));
    }

    @Operation(summary = "Get Buttons By Parent Id")
    @SaCheckPermission("system:menu:list")
    @GetMapping("/buttons")
    public Result<List<RouteMenuVO>> getButtonsByParentId(@RequestParam Long parentId) {
        return Result.success(convertToRouteMenuVO(menuService.getButtonsByParentId(parentId)));
    }
}
