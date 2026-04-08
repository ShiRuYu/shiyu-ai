package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.request.MenuRequest;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.agent.domain.vo.MenuVO;
import com.shiyu.ai.agent.domain.vo.RouteMenuVO;
import com.shiyu.ai.agent.domain.vo.SystemMenuVO;
import com.shiyu.ai.agent.auth.service.MenuService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/system/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 获取当前用户菜单
     * GET /menu/all
     */
    @GetMapping("/all")
    public ResponseEntity<Result<List<RouteMenuVO>>> getAllMenus(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("获取当前用户菜单");
        
        try {
            // TODO: 从 token 中解析用户 ID，这里暂时使用固定值
            Long userId = 1L;
            
            // 从数据库查询用户的菜单树
            List<MenuBO> menuBOs = menuService.getMenuTreeByUserId(userId);
            
            // 转换为 RouteMenuVO
            List<RouteMenuVO> routeMenus = convertToRouteMenuVO(menuBOs);
            
            return ResponseEntity.ok(Result.success(routeMenus));
            
        } catch (Exception e) {
            log.error("获取菜单失败", e);
            return ResponseEntity.status(401).body(Result.fail("获取菜单失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取系统菜单列表
     * GET /system/menu/list
     */
    @GetMapping("/list")
    public ResponseEntity<Result<List<MenuVO>>> getSystemMenuList(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("获取系统菜单列表");
        
        try {
            // 从服务层获取所有菜单
            List<MenuBO> menuBOs = menuService.getAllTree();
            
            // 转换为 MenuVO
            List<MenuVO> menuVOs = convertToMenuVO(menuBOs);
            
            return ResponseEntity.ok(Result.success(menuVOs));
            
        } catch (Exception e) {
            log.error("获取系统菜单列表失败", e);
            return ResponseEntity.status(401).body(Result.fail("获取系统菜单列表失败：" + e.getMessage()));
        }
    }
    
    /**
     * 将 MenuBO 列表转换为 MenuVO 列表
     */
    private List<MenuVO> convertToMenuVO(List<MenuBO> menuBOs) {
        if (menuBOs == null || menuBOs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<MenuVO> result = new ArrayList<>();
        for (MenuBO menuBO : menuBOs) {
            MenuVO vo = MapstructUtils.convert(menuBO, MenuVO.class);
            
            // 递归处理子菜单
            if (menuBO.getChildren() != null && !menuBO.getChildren().isEmpty()) {
                vo.setChildren(convertToMenuVO(menuBO.getChildren()));
            }
            
            result.add(vo);
        }
        
        return result;
    }
    
    /**
     * 将 MenuBO 列表转换为 RouteMenuVO 列表
     */
    private List<RouteMenuVO> convertToRouteMenuVO(List<MenuBO> menuBOs) {
        if (menuBOs == null || menuBOs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<RouteMenuVO> result = new ArrayList<>();
        for (MenuBO menuBO : menuBOs) {
            RouteMenuVO vo = new RouteMenuVO();
            vo.setId(menuBO.getId());
            vo.setPid(menuBO.getParentId());
            vo.setName(menuBO.getCode()); // 使用 code 作为路由名称
            vo.setPath(menuBO.getPath());
            vo.setComponent(menuBO.getComponent());
            
            // 转换类型：MENU -> menu, CATALOG -> catalog, BUTTON -> button
            String type = menuBO.getType();
            if ("MENU".equals(type)) {
                vo.setType("menu");
            } else if ("CATALOG".equals(type)) {
                vo.setType("catalog");
            } else if ("BUTTON".equals(type)) {
                vo.setType("button");
            } else {
                vo.setType(type != null ? type.toLowerCase() : "menu");
            }
            
            // 设置状态：enable=true -> status=1, enable=false -> status=0
            vo.setStatus(Boolean.TRUE.equals(menuBO.getEnable()) ? 1 : 0);
            
            // 设置权限码（使用 code 字段）
            vo.setAuthCode(menuBO.getCode());
            
            // 设置图标
            vo.setIcon(menuBO.getIcon());
            
            // 设置元数据
            RouteMenuVO.MetaVO meta = new RouteMenuVO.MetaVO();
            meta.setTitle(menuBO.getName());
            meta.setIcon(menuBO.getIcon());
            meta.setOrder(menuBO.getOrder());
            vo.setMeta(meta);
            
            // 递归处理子菜单
            if (menuBO.getChildren() != null && !menuBO.getChildren().isEmpty()) {
                vo.setChildren(convertToRouteMenuVO(menuBO.getChildren()));
            }
            
            result.add(vo);
        }
        
        return result;
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
