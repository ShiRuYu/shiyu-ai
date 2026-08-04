package com.shiyu.ai.web.auth;

import com.shiyu.ai.auth.vo.UserContextVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.domain.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 用户上下文测试控制器
 * 用于演示如何通过 UserContextHolder 获取当前登录用户信息
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
            if (!UserContextHolder.isLogin()) {
                return Result.fail("用户未登录");
            }
            
            UserContext userContext = UserContextHolder.getUserContext();
            
            UserContextVO vo = new UserContextVO();
            vo.setUserId(UserContextHolder.getUserId());
            vo.setUsername(UserContextHolder.getUsername());
            vo.setUserType(UserContextHolder.getUserType() != null ? 
                    UserContextHolder.getUserType().name() : null);
            vo.setIsLogin(true);
            
            if (userContext != null) {
                vo.setToken(userContext.getToken());
                vo.setLoginTime(userContext.getLoginTime());
                vo.setExpireTime(userContext.getExpireTime());
                vo.setIpaddr(userContext.getIpaddr());
                vo.setLoginLocation(userContext.getLoginLocation());
                vo.setBrowser(userContext.getBrowser());
                vo.setOs(userContext.getOs() != null ? userContext.getOs().name() : null);
                vo.setNickName(userContext.getNickName());
                vo.setAvatar(userContext.getAvatar());
                vo.setExtInfo(com.shiyu.ai.common.core.utils.JSONUtils.toJsonString(userContext.getExtInfo()));
            }
            
            log.info("成功获取用户信息: userId={}", UserContextHolder.getUserId());
            return Result.success(vo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.fail("获取用户信息失败");
        }
    }

    /**
     * 测试在业务逻辑中使用 UserContextHolder
     * GET /test/user-context/demo
     */
    @Operation(summary = "Demo Usage")
    @GetMapping("/demo")
    public Result<UserContextVO> demoUsage() {
        log.info("演示 UserContextHolder 的使用");
        
        UserContextVO vo = new UserContextVO();
        vo.setIsLogin(UserContextHolder.isLogin());
        
        if (Boolean.TRUE.equals(vo.getIsLogin())) {
            Long userId = UserContextHolder.getUserId();
            String username = UserContextHolder.getUsername();
            UserContext userContext = UserContextHolder.getUserContext();
            
            vo.setUserId(userId);
            vo.setUsername(username);
            vo.setMessage(String.format("欢迎回来，%s！", username != null ? username : "用户"));
            
            if (userContext != null) {
                vo.setDeviceInfo(String.format("浏览器：%s, 操作系统：%s", 
                    userContext.getBrowser(), userContext.getOs()));
                vo.setIpaddr(userContext.getIpaddr());
            }
            
            log.info("演示完成：userId={}, username={}", userId, username);
        } else {
            vo.setMessage("请先登录");
        }
        
        return Result.success(vo);
    }
}
