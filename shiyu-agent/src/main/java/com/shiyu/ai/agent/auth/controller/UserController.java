package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.request.UserRequest;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.UserPageResponse;
import com.shiyu.ai.agent.domain.vo.UserVO;
import com.shiyu.ai.agent.auth.service.UserService;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户详情
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getUserDetail() {
        log.info("获取用户详情");
        
        // 模拟从 token 中获取用户 ID，这里默认返回用户 ID 为 1 的用户
        Long userId = 1L;
        UserBO userBO = userService.getUserDetail(userId);
        
        if (userBO == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "用户不存在",
                    "data", null
            ));
        }
        
        UserVO userVO = MapstructUtils.convert(userBO, UserVO.class);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", userVO);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户列表 - 分页
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam Integer pageNo,
            @RequestParam Integer pageSize) {
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        UserPageResponse pageResponse = userService.getUserList(username, pageNo, pageSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "OK");
        response.put("data", pageResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        log.info("删除用户，userId: {}", userId);
        
        boolean success = userService.deleteUser(userId);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "用户不存在"
            ));
        }
    }

    /**
     * 修改用户
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequest request) {
        log.info("修改用户，userId: {}", userId);
        
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        boolean success = userService.updateUser(userId, userBO);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "修改成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "用户不存在"
            ));
        }
    }

    /**
     * 重置用户密码
     */
    @PatchMapping("/{userId}/password/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> passwordMap) {
        log.info("重置用户密码，userId: {}", userId);
        
        String password = passwordMap.get("password");
        boolean success = userService.resetUserPassword(userId, password);
        
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "重置成功");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 1,
                    "message", "用户不存在"
            ));
        }
    }

    /**
     * 新增用户
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequest request) {
        log.info("新增用户");
        
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        Long userId = userService.createUser(userBO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "新增成功");
        response.put("data", userId);
        
        return ResponseEntity.ok(response);
    }
}
