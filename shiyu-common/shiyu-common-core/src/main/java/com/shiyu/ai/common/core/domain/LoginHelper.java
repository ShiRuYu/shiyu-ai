package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.UserTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录鉴权助手（抽象基类）
 * <p>
 * 提供统一的用户上下文管理和认证操作接口
 * 具体认证框架（如 Sa-Token、Spring Security 等）需要继承此类并实现认证相关方法
 * <p>
 * user_type 为 用户类型 同一个用户表 可以有多种用户类型 例如 pc,app
 * device 为 设备类型 同一个用户类型 可以有 多种设备类型 例如 web,ios
 * 可以组成 用户类型与设备类型多对多的 权限灵活控制
 * <p>
 * 多用户体系 针对 多种用户类型 但权限控制不一致
 * 可以组成 多用户类型表与多设备类型 分别控制权限
 */
@Slf4j
public abstract class LoginHelper {

    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String TENANT_KEY = "tenantId";
    public static final String USER_KEY = "userId";

    // ==================== 用户上下文管理（通用实现）====================

    /**
     * 设置用户上下文
     *
     * @param loginUser 登录用户信息
     */
    public static void setContext(LoginUser loginUser) {
        UserGlobalContext.set(loginUser);
    }

    /**
     * 获取用户上下文
     */
    public static LoginUser getContext() {
        return UserGlobalContext.get();
    }

    /**
     * 清除用户上下文
     */
    public static void clearContext() {
        UserGlobalContext.clear();
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getLoginUser() {
        return getContext();
    }

    /**
     * 获取用户 ID
     */
    public static Long getUserId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取用户类型
     */
    public static UserTypeEnum getUserType() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserType() : null;
    }

    /**
     * 检查当前用户是否已登录（基于上下文）
     *
     * @return 结果
     */
    public static boolean isLogin() {
        try {
            return getLoginUser() != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 认证操作（抽象方法，由子类实现）====================

    /**
     * 执行登录（生成 Token）
     * 
     * @param userId 用户 ID
     * @return Token 值
     */
    public abstract String login(Long userId);

    /**
     * 执行登录并踢掉旧会话（单设备登录）
     * 
     * @param userId 用户 ID
     * @return Token 值
     */
    public abstract String loginWithKickout(Long userId);

    /**
     * 登出（使 Token 失效）
     * 
     * @param userId 用户 ID
     */
    public abstract void logout(Long userId);

    /**
     * 从 Token 中获取用户 ID
     * 
     * @param token Token 值
     * @return 用户 ID，如果 Token 无效返回 null
     */
    public abstract Long getUserIdByToken(String token);

    /**
     * 刷新 Token（先登出再登录）
     * 
     * @param userId 用户 ID
     * @return 新的 Token 值
     */
    public abstract String refreshToken(Long userId);

    /**
     * 获取 Token 过期时间（秒）
     * 
     * @return 过期时间
     */
    public abstract long getTokenTimeout();

    /**
     * 检查是否已登录（基于认证框架）
     * 
     * @return true 已登录，false 未登录
     */
    public abstract boolean isFrameworkLogin();
}
