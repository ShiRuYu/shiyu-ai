package com.shiyu.ai.common.core.domain;

/**
 * 登录鉴权助手（抽象基类）
 * <p>
 * 提供认证操作接口（登录、登出、Token 管理等）
 * 具体认证框架（如 Sa-Token、Spring Security 等）需要继承此类并实现认证相关方法
 * <p>
 * 上下文存取操作请使用 {@link UserContextHolder}
 */
public abstract class LoginHelper {

    /**
     * 执行登录（生成 Token）
     */
    public abstract String login(Long userId);

    /**
     * 执行登录并踢掉旧会话（单设备登录）
     */
    public abstract String loginWithKickout(Long userId);

    /**
     * 登出（使 Token 失效）
     */
    public abstract void logout(Long userId);

    /**
     * 从 Token 中获取用户 ID
     */
    public abstract Long getUserIdByToken(String token);

    /**
     * 刷新 Token（先登出再登录）
     */
    public abstract String refreshToken(Long userId);

    /**
     * 获取 Token 过期时间（秒）
     */
    public abstract long getTokenTimeout();

    /**
     * 检查是否已登录（基于认证框架）
     */
    public abstract boolean isFrameworkLogin();
}