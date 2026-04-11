package com.shiyu.ai.agent.biz.auth.service;

import com.shiyu.ai.agent.domain.vo.LoginResponseVO;

import java.util.List;

/**
 * 认证服务
 * 提供用户登录、登出等认证功能
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录响应（包含用户信息和访问令牌）
     */
    LoginResponseVO login(String username, String password);
    
    /**
     * 获取用户权限码（通过用户名）
     * @param username 用户名
     * @return 权限码列表
     */
    List<String> getAuthCodes(String username);
    
    /**
     * 获取用户权限码（通过用户 ID）
     * @param userId 用户 ID
     * @return 权限码列表
     */
    List<String> getAuthCodesByUserId(Long userId);
    
    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);
    
    /**
     * 用户登出
     * @param refreshToken 刷新令牌
     */
    void logout(String refreshToken);
}
