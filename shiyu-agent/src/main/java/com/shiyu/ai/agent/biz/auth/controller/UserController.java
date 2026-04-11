package com.shiyu.ai.agent.biz.auth.controller;

import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.request.UserRequest;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.UserPageResponse;
import com.shiyu.ai.agent.domain.vo.UserVO;
import com.shiyu.ai.agent.domain.vo.UserInfoVO;
import com.shiyu.ai.agent.biz.auth.service.UserService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginHelper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * 获取当前用户信息
     * GET /user/info
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("获取当前用户信息");
        
        try {
            // 从 LoginHelper 获取当前登录用户 ID
            Long userId = LoginHelper.getUserId();
            if (userId == null) {
                return Result.fail("用户未登录");
            }
            
            UserBO userBO = userService.getUserDetail(userId);
            
            if (userBO == null) {
                return Result.fail("用户不存在");
            }
            
            // 转换为 UserInfoVO
            UserInfoVO userInfoVO = new UserInfoVO();
            userInfoVO.setId(userBO.getId());
            userInfoVO.setUsername(userBO.getUsername());
            userInfoVO.setRealName(userBO.getNickName());
            userInfoVO.setPassword(""); // 密码字段为空或隐藏
            userInfoVO.setHomePath("/dashboard");
            
            // 设置角色列表
            if (userBO.getRoles() != null && !userBO.getRoles().isEmpty()) {
                userInfoVO.setRoles(userBO.getRoles().stream()
                        .map(RoleBO::getCode)
                        .collect(Collectors.toList()));
            } else {
                userInfoVO.setRoles(List.of());
            }
            
            return Result.success(userInfoVO);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 用户详情
     */
    @GetMapping("/detail")
    public Result<UserVO> getUserDetail() {
        log.info("获取用户详情");
        
        // 从 LoginHelper 获取当前登录用户 ID
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        
        UserBO userBO = userService.getUserDetail(userId);
        
        if (userBO == null) {
            return Result.fail("用户不存在");
        }
        
        UserVO userVO = MapstructUtils.convert(userBO, UserVO.class);
        
        return Result.success(userVO);
    }

    /**
     * 用户列表 - 分页
     */
    @GetMapping("")
    public Result<UserPageResponse> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam Integer pageNo,
            @RequestParam Integer pageSize) {
        log.info("获取用户列表，username: {}, pageNo: {}, pageSize: {}", username, pageNo, pageSize);
        
        UserPageResponse pageResponse = userService.getUserList(username, pageNo, pageSize);
        
        return Result.success(pageResponse);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        log.info("删除用户，userId: {}", userId);
        
        boolean success = userService.deleteUser(userId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 修改用户
     */
    @PatchMapping("/{userId}")
    public Result<Void> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequest request) {
        log.info("修改用户，userId: {}", userId);
        
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        boolean success = userService.updateUser(userId, userBO);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 重置用户密码
     */
    @PatchMapping("/{userId}/password/reset")
    public Result<Void> resetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> passwordMap) {
        log.info("重置用户密码，userId: {}", userId);
        
        String password = passwordMap.get("password");
        boolean success = userService.resetUserPassword(userId, password);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 新增用户
     */
    @PostMapping("")
    public Result<Long> createUser(@RequestBody UserRequest request) {
        log.info("新增用户");
        
        UserBO userBO = MapstructUtils.convert(request, UserBO.class);
        Long userId = userService.createUser(userBO);
        
        return Result.success(userId);
    }
}
