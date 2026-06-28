package com.shiyu.ai.bootstrap.controller;

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
 * 鐢ㄦ埛涓婁笅鏂囨祴璇曟帶鍒跺櫒
 * 鐢ㄤ簬婕旂ず濡備綍閫氳繃 LoginContextHolder 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛淇℃伅
 */
@Slf4j
@RestController
@RequestMapping("/test/user-context")
public class UserContextTestController {

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛淇℃伅
     * GET /test/user-context/info
     * 
     * @return 褰撳墠鐧诲綍鐢ㄦ埛鐨勮缁嗕俊鎭?
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentUserInfo() {
        log.info("鏀跺埌鑾峰彇褰撳墠鐢ㄦ埛淇℃伅璇锋眰");
        
        try {
            // 妫€鏌ユ槸鍚﹀凡鐧诲綍
            if (!LoginContextHolder.isLogin()) {
                return Result.fail("鐢ㄦ埛鏈櫥褰?);
            }
            
            // 鑾峰彇鐧诲綍鐢ㄦ埛淇℃伅
            LoginUser loginUser = LoginContextHolder.getLoginUser();
            
            // 鏋勫缓鍝嶅簲鏁版嵁
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
            
            log.info("鎴愬姛鑾峰彇鐢ㄦ埛淇℃伅: userId={}", LoginContextHolder.getUserId());
            return Result.success(userInfo);
            
        } catch (Exception e) {
            log.error("鑾峰彇鐢ㄦ埛淇℃伅澶辫触", e);
            return Result.fail("鑾峰彇鐢ㄦ埛淇℃伅澶辫触");
        }
    }

    /**
     * 娴嬭瘯鍦ㄤ笟鍔￠€昏緫涓娇鐢?LoginContextHolder
     * GET /test/user-context/demo
     * 
     * @return 婕旂ず缁撴灉
     */
    @GetMapping("/demo")
    public Result<Map<String, Object>> demoUsage() {
        log.info("婕旂ず LoginContextHolder 鐨勪娇鐢?);
        
        Map<String, Object> result = new HashMap<>();
        
        // 1. 妫€鏌ョ櫥褰曠姸鎬?
        boolean isLogin = LoginContextHolder.isLogin();
        result.put("isLogin", isLogin);
        
        if (isLogin) {
            // 2. 鑾峰彇鐢ㄦ埛 ID锛堟渶甯哥敤锛?
            Long userId = LoginContextHolder.getUserId();
            result.put("userId", userId);
            
            // 3. 鑾峰彇鐢ㄦ埛鍚?
            String username = LoginContextHolder.getUsername();
            result.put("username", username);
            
            // 4. 鑾峰彇瀹屾暣鐢ㄦ埛瀵硅薄锛堥渶瑕佹洿澶氫俊鎭椂锛?
            LoginUser loginUser = LoginContextHolder.getLoginUser();
            if (loginUser != null) {
                result.put("message", String.format("娆㈣繋鍥炴潵锛?s锛佹偍鐨?IP 鏄細%s", 
                    username != null ? username : "鐢ㄦ埛", 
                    loginUser.getIpaddr() != null ? loginUser.getIpaddr() : "鏈煡"));
                
                result.put("device", String.format("娴忚鍣細%s, 鎿嶄綔绯荤粺锛?s", 
                    loginUser.getBrowser(), loginUser.getOs()));
            }
            
            log.info("婕旂ず瀹屾垚锛歶serId={}, username={}", userId, username);
        } else {
            result.put("message", "璇峰厛鐧诲綍");
        }
        
        return Result.success(result);
    }
}
