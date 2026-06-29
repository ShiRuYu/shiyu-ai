package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户上下文测试控制器
 * 用于演示如何通过 LoginContextHolder 获取当前登录用户信息
 */
@Slf4j
@RestController
@RequestMapping("/test/user-context")
public class UserContextTestController {

    /**
     * 获取当前登录用户信息
     * GET /test/user-context/info
     * 
     * @return 当前登录用户的详细信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentUserInfo() {
        log.info("收到获取当前用户信息请求");
        
        try {
            // 检查是否已登录
            if (!LoginContextHolder.isLogin()) {
                return Result.fail("用户未登录");
            }
            
            // 获取登录用户信息
            LoginUser loginUser = LoginContextHolder.getLoginUser();
            
            // 构建响应数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", LoginContextHolder.getUserId());
            userInfo.put("username", LoginContextHolder.getUsername());
            userInfo.put("userType", LoginContextHolder.getUserType());
            
            if (loginUser != null) {
                userInfo.put("token", loginUser.getToken());
                userInfo.put("loginTime", loginUser.getLoginTime());
                userInfo.put("expireTime", loginUser.getExpireTime());
                userInfo.put("ipaddr", loginUser.getIpaddr());
                userInfo.put("loginLocation", loginUser.getLoginLocation());
                userInfo.put("browser", loginUser.getBrowser());
                userInfo.put("os", loginUser.getOs());
                userInfo.put("nickName", loginUser.getNickName());
                userInfo.put("avatar", loginUser.getAvatar());
                userInfo.put("extInfo", loginUser.getExtInfo());
            }
            
            log.info("成功获取用户信息: userId={}", LoginContextHolder.getUserId());
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败");
        }
    }

    /**
     * 测试在业务逻辑中使用 LoginContextHolder
     * GET /test/user-context/demo
     * 
     * @return 演示结果
     */
    @GetMapping("/demo")
    public Result<Map<String, Object>> demoUsage() {
        log.info("演示 LoginContextHolder 的使用");
        
        Map<String, Object> result = new HashMap<>();
        
        // 1. 检查登录状态
        boolean isLogin = LoginContextHolder.isLogin();
        result.put("isLogin", isLogin);
        
        if (isLogin) {
            // 2. 获取用户 ID（最常用）
            Long userId = LoginContextHolder.getUserId();
            result.put("userId", userId);
            
            // 3. 获取用户名
            String username = LoginContextHolder.getUsername();
            result.put("username", username);
            
            // 4. 获取完整用户对象（需要更多信息时）
            LoginUser loginUser = LoginContextHolder.getLoginUser();
            if (loginUser != null) {
                result.put("message", String.format("欢迎回来，%s！您的 IP 是：%s", 
                    username != null ? username : "用户", 
                    loginUser.getIpaddr() != null ? loginUser.getIpaddr() : "未知"));
                
                result.put("device", String.format("浏览器：%s, 操作系统：%s", 
                    loginUser.getBrowser(), loginUser.getOs()));
            }
            
            log.info("演示完成：userId={}, username={}", userId, username);
        } else {
            result.put("message", "请先登录");
        }
        
        return Result.success(result);
    }
}
