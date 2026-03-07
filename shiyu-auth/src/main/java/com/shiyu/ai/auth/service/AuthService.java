package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysUserBO;
import com.shiyu.ai.auth.domain.vo.LoginResponseVO;
import com.shiyu.ai.auth.domain.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginVO 登录信息
     * @return 登录响应
     */
    LoginResponseVO login(LoginVO loginVO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUserBO getUserByUsername(String username);

    /**
     * 验证 Token 是否有效
     *
     * @param token 访问令牌
     * @return true-有效，false-无效
     */
    boolean validateToken(String token);

    /**
     * 刷新 Token
     *
     * @param oldToken 旧的访问令牌
     * @return 新的访问令牌
     */
    String refreshToken(String oldToken);
}
