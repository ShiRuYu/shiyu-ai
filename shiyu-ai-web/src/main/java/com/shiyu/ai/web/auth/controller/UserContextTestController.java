package com.shiyu.ai.web.auth.controller;

import com.shiyu.ai.auth.vo.UserContextVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 用户上下文测试控制器
 * 用于演示如何通过 LoginContextHolder 获取当前登录用户信息
 */
@Slf4j
@Tag(name = "User Context Test", description = "User Context Test")
@RestController
@RequestMapping("/test/user-context")
public class UserContextTestController {

    /**
     * 获取当前登录用户信息
     * GET /test/user-context/info
     */
    @Operation(summary = "Get Current User Info")
    @GetMapping("/info")
    public Result<UserContextVO> getCurrentUserInfo() {
        log.info("收到获取当前用户信息请求");
        
        try {
            if (!LoginContextHolder.isLogin()) {
                return Result.fail("用户未登录");
            }
            
            LoginUser loginUser = LoginContextHolder.getLoginUser();
            
            UserContextVO vo = new UserContextVO();
            vo.setUserId(LoginContextHolder.getUserId());
            vo.setUsername(LoginContextHolder.getUsername());
            vo.setUserType(LoginContextHolder.getUserType() != null ? 
                    LoginContextHolder.getUserType().name() : null);
            vo.setIsLogin(true);
            
            if (loginUser != null) {
                vo.setToken(loginUser.getToken());
                vo.setLoginTime(loginUser.getLoginTime());
                vo.setExpireTime(loginUser.getExpireTime());
                vo.setIpaddr(loginUser.getIpaddr());
                vo.setLoginLocation(loginUser.getLoginLocation());
                vo.setBrowser(loginUser.getBrowser());
                vo.setOs(loginUser.getOs() != null ? loginUser.getOs().name() : null);
                vo.setNickName(loginUser.getNickName());
                vo.setAvatar(loginUser.getAvatar());
                vo.setExtInfo(com.shiyu.ai.common.core.utils.JSONUtils.toJsonString(loginUser.getExtInfo()));
            }
            
            log.info("成功获取用户信息: userId={}", LoginContextHolder.getUserId());
            return Result.success(vo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败");
        }
    }

    /**
     * 测试在业务逻辑中使用 LoginContextHolder
     * GET /test/user-context/demo
     */
    @Operation(summary = "Demo Usage")
    @GetMapping("/demo")
    public Result<UserContextVO> demoUsage() {
        log.info("演示 LoginContextHolder 的使用");
        
        UserContextVO vo = new UserContextVO();
        vo.setIsLogin(LoginContextHolder.isLogin());
        
        if (Boolean.TRUE.equals(vo.getIsLogin())) {
            Long userId = LoginContextHolder.getUserId();
            String username = LoginContextHolder.getUsername();
            LoginUser loginUser = LoginContextHolder.getLoginUser();
            
            vo.setUserId(userId);
            vo.setUsername(username);
            vo.setMessage(String.format("欢迎回来，%s！", username != null ? username : "用户"));
            
            if (loginUser != null) {
                vo.setDeviceInfo(String.format("浏览器：%s, 操作系统：%s", 
                    loginUser.getBrowser(), loginUser.getOs()));
                vo.setIpaddr(loginUser.getIpaddr());
            }
            
            log.info("演示完成：userId={}, username={}", userId, username);
        } else {
            vo.setMessage("请先登录");
        }
        
        return Result.success(vo);
    }
}
