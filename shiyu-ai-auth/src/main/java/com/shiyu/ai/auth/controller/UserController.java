package com.shiyu.ai.auth.controller;

import com.shiyu.ai.model.bo.RoleBO;
import com.shiyu.ai.model.request.UserRequest;
import com.shiyu.ai.model.bo.UserBO;
import com.shiyu.ai.model.vo.UserPageResponse;
import com.shiyu.ai.model.vo.UserVO;
import com.shiyu.ai.model.vo.WorkspaceContextVO;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 鐢ㄦ埛绠＄悊 Controller
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * 鑾峰彇褰撳墠鐢ㄦ埛淇℃伅
     * GET /user/info
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("鑾峰彇褰撳墠鐢ㄦ埛淇℃伅");

        Long userId = LoginContextHolder.getUserId();
        if (userId == null) {
            return Result.fail("鐢ㄦ埛鏈櫥褰?);
        }
        
        UserBO userBO = userService.getUserDetail(userId);
        
        if (userBO == null) {
            return Result.fail("鐢ㄦ埛涓嶅瓨鍦?);
        }
        
        UserVO userVO = MapstructUtils.convert(userBO, UserVO.class);

        // 濉厖绉熸埛鍜屽伐浣滅┖闂翠俊鎭?
        try {
            userVO.setTenants(authService.getUserTenants(userId));
            List<WorkspaceContextVO> workspaces = authService.getUserWorkspaces(userId);
            userVO.setWorkspaces(workspaces);

            // 浠?extInfo 瑙ｆ瀽褰撳墠绉熸埛鍜屽伐浣滅┖闂?ID
            if (userVO.getExtInfo() != null) {
                var extMap = com.shiyu.ai.common.core.utils.JSONUtils.parseObject(
                        userVO.getExtInfo(), java.util.Map.class);
                if (extMap != null) {
                    Object tid = extMap.get("currentTenantId");
                    if (tid instanceof Number) {
                        userVO.setCurrentTenantId(((Number) tid).longValue());
                    }
                    Object wid = extMap.get("currentWorkspaceId");
                    if (wid instanceof Number) {
                        userVO.setCurrentWorkspaceId(((Number) wid).longValue());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("鑾峰彇鐢ㄦ埛绉熸埛/宸ヤ綔绌洪棿淇℃伅澶辫触: {}", e.getMessage());
        }

        return Result.success(userVO);
    }

    /**
     * 鐢ㄦ埛鍒楄〃 - 鍒嗛〉
     */
    @GetMapping("")
    public Result<UserPageResponse> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam Integer pageNo,
            @RequestParam Integer pageSize) {
        log.info("鑾峰彇鐢ㄦ埛鍒楄〃锛寀sername: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        UserPageResponse pageResponse = userService.getUserList(username, pageNo, pageSize);
        
        return Result.success(pageResponse);
    }

    /**
     * 鍒犻櫎鐢ㄦ埛
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        log.info("鍒犻櫎鐢ㄦ埛锛寀serId: {}", userId);
        
        boolean success = userService.deleteUser(userId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鐢ㄦ埛涓嶅瓨鍦?);
        }
    }

    /**
     * 淇敼鐢ㄦ埛
     */
    @PatchMapping("/{userId}")
    public Result<Void> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequest request) {
        log.info("淇敼鐢ㄦ埛锛寀serId: {}", userId);
        
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        boolean success = userService.updateUser(userId, userBO);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鐢ㄦ埛涓嶅瓨鍦?);
        }
    }

    /**
     * 閲嶇疆鐢ㄦ埛瀵嗙爜
     */
    @PatchMapping("/{userId}/password/reset")
    public Result<Void> resetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> passwordMap) {
        log.info("閲嶇疆鐢ㄦ埛瀵嗙爜锛寀serId: {}", userId);
        
        String password = passwordMap.get("password");
        boolean success = userService.resetUserPassword(userId, password);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鐢ㄦ埛涓嶅瓨鍦?);
        }
    }

    /**
     * 淇敼瀵嗙爜锛堥渶鏍￠獙鏃у瘑鐮侊級
     */
    @PatchMapping("/{userId}/password")
    public Result<Void> changePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        log.info("淇敼瀵嗙爜锛寀serId: {}", userId);

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.isEmpty() ||
            newPassword == null || newPassword.isEmpty()) {
            return Result.fail("鏃у瘑鐮佸拰鏂板瘑鐮佷笉鑳戒负绌?);
        }

        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鏃у瘑鐮侀敊璇垨鐢ㄦ埛涓嶅瓨鍦?);
        }
    }

    /**
     * 鏂板鐢ㄦ埛
     */
    @PostMapping("")
    public Result<Long> createUser(@Valid @RequestBody UserRequest request) {
        log.info("鏂板鐢ㄦ埛");
        
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        Long userId = userService.createUser(userBO);
        
        return Result.success(userId);
    }
}
