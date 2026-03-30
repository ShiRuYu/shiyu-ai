package com.shiyu.ai.agent.auth.service;

import com.shiyu.ai.agent.domain.vo.LoginVO;

/**
 * 认证服务
 * 提供用户登录、登出等认证功能
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param captcha 验证码
     * @param captchaKey 验证码 key
     * @return 登录响应（包含访问令牌）
     */
    LoginVO login(String username, String password, String captcha, String captchaKey);
    
    /**
     * 切换当前角色
     * @param username 用户名
     * @param roleCode 角色编码
     * @return 登录响应（包含新的访问令牌）
     */
    LoginVO switchCurrentRole(String username, String roleCode);
    
    /**
     * 用户登出
     * @param accessToken 访问令牌
     */
    void logout(String accessToken);
}
