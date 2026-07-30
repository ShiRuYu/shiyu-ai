package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.auth.vo.TenantInfoVO;

import java.util.List;

/**
 * 认证服务
 * 提供用户登录、登出等认证功能
 */
public interface AuthService {
    
    LoginResponseVO login(String username, String password);

    LoginResponseVO login(String username, String password, Long roleId);
    
    List<String> getAuthCodes(String username);
    
    List<String> getAuthCodesByUserId(Long userId);
    
    String refreshToken(String refreshToken);
    
    void logout(String refreshToken);

    boolean switchCurrentRole(Long userId, Long roleId);

    boolean switchCurrentTenant(Long userId, Long tenantId);

    List<TenantInfoVO> getUserTenants(Long userId);

    LoginResponseVO register(String username, String password, String email);

    LoginResponseVO codeLogin(String phone, String code, String captchaKey);

    boolean forgetPassword(String email, String newPassword, String code, String captchaKey);

}
