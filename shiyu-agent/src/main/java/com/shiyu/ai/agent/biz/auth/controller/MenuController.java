package com.shiyu.ai.agent.biz.auth.controller;

import com.shiyu.ai.model.request.MenuRequest;
import com.shiyu.ai.model.bo.MenuBO;
import com.shiyu.ai.model.vo.RouteMenuVO;
import com.shiyu.ai.agent.biz.auth.service.MenuService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu Controller
 */
@Slf4j
@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/all")
    public Result<List<RouteMenuVO>> getAllMenus(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("getAllMenus");
        try {
            Long userId = LoginContextHolder.getUserId();
            List<MenuBO> menuBOs = menuService.getRouteMenusByUserId(userId);
            return Result.success(convertToRouteMenuVO(menuBOs));
        } catch (Exception e) {
            log.error("操作失败", e);
            return Result.fail("操作失败");
        }
    }

    @GetMapping("/list")
    public Result<List<RouteMenuVO>> getSystemMenuList() {
        return Result.success(convertToRouteMenuVO(menuService.getAllTree()));
    }

    @GetMapping("/list/roots")
    public Result<List<RouteMenuVO>> getMenuRoots() {
        return Result.success(convertToRouteMenuVO(menuService.getMenuRoots()));
    }

    @GetMapping("/list/children/{parentId}")
    public Result<List<RouteMenuVO>> getMenuChildren(@PathVariable Long parentId) {
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
            else if ("BUTTON".equals(type)) vo.setType("button");
            else vo.setType(type != null ? type.toLowerCase() : "menu");
            vo.setStatus(menuBO.getStatus());
            vo.setAuthCode(menuBO.getCode());
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

    @GetMapping("/role/permissions/tree")
    public Result<List<RouteMenuVO>> getMenuPermissionsTree() {
        return Result.success(convertToRouteMenuVO(menuService.getMenuPermissionsTree()));
    }

    @GetMapping("/menu/tree")
    public Result<List<RouteMenuVO>> getMenuTree() {
        return Result.success(convertToRouteMenuVO(menuService.getMenuTree()));
    }

    @GetMapping("/tree")
    public Result<List<RouteMenuVO>> getAllTree() {
        return Result.success(convertToRouteMenuVO(menuService.getAllTree()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        return menuService.deleteMenu(id) ? Result.success() : Result.fail("delete fail");
    }

    @PostMapping("")
    public Result<Void> createMenu(@Valid @RequestBody MenuRequest request) {
        return menuService.createMenu(MapstructUtils.convert(request, MenuBO.class)) ? Result.success() : Result.fail("create fail");
    }

    @PatchMapping("/{id}")
    public Result<Void> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        return menuService.updateMenu(id, MapstructUtils.convert(request, MenuBO.class)) ? Result.success() : Result.fail("update fail");
    }

    @GetMapping("/name-exists")
    public Result<Boolean> isMenuNameExists(@RequestParam String name, @RequestParam(required = false) Long id) {
        return Result.success(menuService.isMenuNameExists(name, id));
    }

    @GetMapping("/path-exists")
    public Result<Boolean> isMenuPathExists(@RequestParam String path, @RequestParam(required = false) Long id) {
        return Result.success(menuService.isMenuPathExists(path, id));
    }

    @GetMapping("/button/{parentId}")
    public Result<List<RouteMenuVO>> getButtonsByParentId(@PathVariable Long parentId) {
        return Result.success(convertToRouteMenuVO(menuService.getButtonsByParentId(parentId)));
    }
}
