package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.request.MenuRequest;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.agent.auth.service.MenuService;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 角色权限树-by token
     */
    @GetMapping("/role/permissions/tree")
    public ResponseEntity<Map<String, Object>> getMenuPermissionsTree() {
        log.info("获取角色权限树-by token");
        
        List<MenuBO> menus = menuService.getMenuPermissionsTree();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", menus);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 权限树 - 菜单
     */
    @GetMapping("/menu/tree")
    public ResponseEntity<Map<String, Object>> getMenuTree() {
        log.info("获取权限树 - 菜单");
        
        List<MenuBO> menus = menuService.getMenuTree();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", menus);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 权限树-all
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getAllTree() {
        log.info("获取权限树-all");
        
        List<MenuBO> menus = menuService.getAllTree();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", menus);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Long id) {
        log.info("删除菜单，id: {}", id);
        
        boolean success = menuService.deleteMenu(id);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "删除失败"
            ));
        }
    }

    /**
     * 新增菜单
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createMenu(@RequestBody MenuRequest request) {
        log.info("新增菜单");
        
        MenuBO menuBO = MapstructUtils.convert(request, MenuBO.class);
        boolean success = menuService.createMenu(menuBO);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "新增成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "新增失败"
            ));
        }
    }

    /**
     * 修改菜单
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMenu(
            @PathVariable Long id,
            @RequestBody MenuRequest request) {
        log.info("修改菜单，id: {}", id);
        
        MenuBO menuBO = MapstructUtils.convert(request, MenuBO.class);
        boolean success = menuService.updateMenu(id, menuBO);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "修改成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "修改失败"
            ));
        }
    }

    /**
     * 按钮权限-by parentId
     */
    @GetMapping("/button/{parentId}")
    public ResponseEntity<Map<String, Object>> getButtonsByParentId(@PathVariable Long parentId) {
        log.info("获取按钮权限-by parentId: {}", parentId);
        
        List<MenuBO> buttons = menuService.getButtonsByParentId(parentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", buttons);
        
        return ResponseEntity.ok(response);
    }
}
