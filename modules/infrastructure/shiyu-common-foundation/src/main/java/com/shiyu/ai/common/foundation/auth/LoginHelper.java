package com.shiyu.ai.common.foundation.auth;

import com.shiyu.ai.common.foundation.context.UserContextHolder;

/**
 * 提供 Login 相关的通用辅助操作，供业务和基础设施复用。
 */
public abstract class LoginHelper {

    /** 执行登录（生成 Token） */
    public abstract String login(Long userId);

    /** 执行登录并踢掉旧会话（单设备登录） */
    public abstract String loginWithKickout(Long userId);

    /** 登出（使 Token 失效） */
    public abstract void logout(Long userId);

    /** 从 Token 中获取用户 ID */
    public abstract Long getUserIdByToken(String token);

    /** 登出单个 Token，不影响同一用户的其他会话。 */
    public abstract void logoutToken(String token);

    /** 刷新单个 Token，不影响同一用户的其他会话。 */
    public abstract String refreshToken(String token);

    /** 旧式用户级刷新接口，保留给兼容实现。 */
    public abstract String refreshToken(Long userId);

    /** 获取 Token 过期时间（秒） */
    public abstract long getTokenTimeout();

    /** 检查是否已登录（基于认证框架） */
    public abstract boolean isFrameworkLogin();
}
