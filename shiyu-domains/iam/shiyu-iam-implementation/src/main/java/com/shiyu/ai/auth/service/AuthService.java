package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.auth.vo.TenantInfoVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.List;

/**
 * 认证服务
 * 提供用户登录、登出等认证功能
 */
public interface AuthService {
    
    LoginResponseVO login(String username, String password);

    LoginResponseVO login(String username, String password, Long roleId);

    /**
     * Authenticates a user with transport metadata supplied by the HTTP adapter.
     * The service never reads request/thread context itself.
     */
    LoginResponseVO login(String username, String password, Long roleId, String loginIp);
    
    List<String> getAuthCodes(ActorContext actor, String username);
    
    List<String> getAuthCodesByUserId(ActorContext actor, UserId userId);
    
    String refreshToken(String refreshToken);
    
    void logout(String refreshToken);

    boolean switchCurrentRole(Long userId, Long roleId);

    boolean switchCurrentTenant(Long userId, TenantId tenantId);

    List<TenantInfoVO> getUserTenants(ActorContext actor, Long userId);

    LoginResponseVO register(String username, String password, String email);

    LoginResponseVO codeLogin(String phone, String code, String captchaKey);

    boolean forgetPassword(String email, String newPassword, String code, String captchaKey);

}
