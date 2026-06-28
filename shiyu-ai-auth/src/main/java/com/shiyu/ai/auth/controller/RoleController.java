package com.shiyu.ai.auth.controller;

import com.shiyu.ai.model.request.RoleRequest;
import com.shiyu.ai.model.request.AssignUserRolesRequest;
import com.shiyu.ai.model.bo.RoleBO;
import com.shiyu.ai.model.vo.RolePageResponse;
import com.shiyu.ai.auth.service.RoleService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 瑙掕壊绠＄悊 Controller
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
     * 瑙掕壊鍒楄〃 - 鍒嗛〉
     */
    @GetMapping("/list")
    public Result<RolePageResponse> getRoleList(
            @RequestParam(required = false,name = "page") Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String name) {
        log.info("鑾峰彇瑙掕壊鍒楄〃锛宲ageNo: {}, pageSize: {}, name: {}", pageNo, pageSize, name);
        
        // 璁剧疆榛樿鍊?
        if (pageNo == null) pageNo = 1;
        if (pageSize == null) pageSize = 10;
        
        RolePageResponse pageResponse = roleService.getRoleList(pageNo, pageSize, name);
        
        return Result.success(pageResponse);
    }

    /**
     * 瑙掕壊鍒楄〃-all
     */
    @GetMapping("")
    public Result<List<RoleBO>> getAllRoles(
            @RequestParam(required = false) String status) {
        log.info("鑾峰彇鎵€鏈夎鑹诧紝status: {}", status);
        
        List<RoleBO> roles = roleService.getAllRoles(status);
        
        return Result.success(roles);
    }

    /**
     * 淇敼瑙掕壊
     */
    @PatchMapping("/{id}")
    public Result<Void> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        log.info("淇敼瑙掕壊锛宨d: {}", id);
        
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        boolean success = roleService.updateRole(id, roleBO);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("瑙掕壊涓嶅瓨鍦?);
        }
    }

    /**
     * 淇敼瑙掕壊
     */
    @PutMapping("/{id}")
    public Result<Void> putRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        log.info("淇敼瑙掕壊锛宨d: {}", id);

        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        boolean success = roleService.updateRole(id, roleBO);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("瑙掕壊涓嶅瓨鍦?);
        }
    }

    /**
     * 鍒犻櫎瑙掕壊
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        log.info("鍒犻櫎瑙掕壊锛宨d: {}", id);
        
        boolean success = roleService.deleteRole(id);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("瑙掕壊涓嶅瓨鍦?);
        }
    }

    /**
     * 鍙栨秷鍒嗛厤瑙掕壊 - 鎵归噺
     */
    @PatchMapping("/users/remove/{id}")
    public Result<Void> removeUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("鍙栨秷鍒嗛厤瑙掕壊锛宨d: {}, userIds: {}", id, request.getUserIds());
        
        boolean success = roleService.removeUserRoles(id, request.getUserIds());
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鍙栨秷鍒嗛厤澶辫触");
        }
    }

    /**
     * 鍒嗛厤瑙掕壊 - 鎵归噺
     */
    @PatchMapping("/users/add/{id}")
    public Result<Void> assignUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignUserRolesRequest request) {
        log.info("鍒嗛厤瑙掕壊锛宨d: {}, userIds: {}", id, request.getUserIds());
        
        boolean success = roleService.assignUserRoles(id, request.getUserIds());
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鍒嗛厤澶辫触");
        }
    }

    /**
     * 鏂板瑙掕壊
     */
    @PostMapping("")
    public Result<Void> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("鏂板瑙掕壊");
        
        RoleBO roleBO = MapstructUtils.convert(request, RoleBO.class);
        boolean success = roleService.createRole(roleBO);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鏂板澶辫触");
        }
    }
}
