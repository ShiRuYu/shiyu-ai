package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.request.RoleRequest;
import com.shiyu.ai.agent.domain.request.AssignUserRolesRequest;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.vo.RolePageResponse;
import com.shiyu.ai.agent.auth.service.RoleService;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/system/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 角色列表 - 分页
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getRoleList(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String name) {
        log.info("获取角色列表，pageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        // 设置默认值
        if (pageNo == null) pageNo = 1;
        if (pageSize == null) pageSize = 10;
        
        RolePageResponse pageResponse = roleService.getRoleList(pageNo, pageSize, name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", pageResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 角色列表-all
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllRoles(
            @RequestParam(required = false) Integer enable) {
        log.info("获取所有角色，enable: {}", enable);
        
        Boolean enableBool = null;
        if (enable != null) {
            enableBool = enable == 1;
        }
        
        List<RoleBO> roles = roleService.getAllRoles(enableBool);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", roles);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 修改角色
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestBody RoleRequest request) {
        log.info("修改角色，id: {}", id);
        
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        boolean success = roleService.updateRole(id, roleBO);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "修改成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "角色不存在"
            ));
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        log.info("删除角色，id: {}", id);
        
        boolean success = roleService.deleteRole(id);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "角色不存在"
            ));
        }
    }

    /**
     * 取消分配角色 - 批量
     */
    @PatchMapping("/users/remove/{id}")
    public ResponseEntity<Map<String, Object>> removeUserRoles(
            @PathVariable Long id,
            @RequestBody AssignUserRolesRequest request) {
        log.info("取消分配角色，id: {}, userIds: {}", id, request.getUserIds());
        
        boolean success = roleService.removeUserRoles(id, request.getUserIds());
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "取消分配成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "取消分配失败"
            ));
        }
    }

    /**
     * 分配角色 - 批量
     */
    @PatchMapping("/users/add/{id}")
    public ResponseEntity<Map<String, Object>> assignUserRoles(
            @PathVariable Long id,
            @RequestBody AssignUserRolesRequest request) {
        log.info("分配角色，id: {}, userIds: {}", id, request.getUserIds());
        
        boolean success = roleService.assignUserRoles(id, request.getUserIds());
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "分配成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "分配失败"
            ));
        }
    }

    /**
     * 新增角色
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody RoleRequest request) {
        log.info("新增角色");
        
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        boolean success = roleService.createRole(roleBO);
        
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
}
