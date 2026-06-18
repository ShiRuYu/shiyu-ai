package com.shiyu.ai.agent.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.shiyu.ai.common.core.domain.LoginHelper;
import com.shiyu.ai.common.core.domain.LoginUser;
import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token 认证助手
 * LoginHelper 的 Sa-Token 实现类
 * 
 * 注意：此类只在 shiyu-agent 模块中使用，用于封装 Sa-Token 的具体实现
 */
@Slf4j
public class SaTokenHelper extends LoginHelper {

    /** session 中 LoginUser 的键名 */
    private static final String SESSION_KEY_LOGIN_USER = "loginUser";

    /**
     * 单例实例（用于调用实例方法）
     */
    private static final SaTokenHelper INSTANCE = new SaTokenHelper();

    /**
     * 获取实例
     */
    public static SaTokenHelper getInstance() {
        return INSTANCE;
    }

    // ==================== 实现抽象方法 ====================

    @Override
    public String login(Long userId) {
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    @Override
    public String loginWithKickout(Long userId) {
        StpUtil.kickout(userId);
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    @Override
    public void logout(Long userId) {
        StpUtil.logout(userId);
    }

    @Override
    public Long getUserIdByToken(String token) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null) {
                return Long.parseLong(loginId.toString());
            }
        } catch (Exception e) {
            log.warn("从 Token 中获取用户 ID 失败: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String refreshToken(Long userId) {
        StpUtil.logout(userId);
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    @Override
    public long getTokenTimeout() {
        return StpUtil.getTokenTimeout();
    }

    @Override
    public boolean isFrameworkLogin() {
        return StpUtil.isLogin();
    }

    // ==================== 额外工具方法 ====================

    /**
     * 获取当前登录用户 ID（基于 Sa-Token）
     */
    public static Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前 Token 值
     */
    public static String getCurrentToken() {
        return StpUtil.getTokenValue();
    }

    /**
     * 缓存当前 LoginUser 到 session
     */
    public static void saveLoginUserToSession(LoginUser loginUser) {
        StpUtil.getSession().set(SESSION_KEY_LOGIN_USER, loginUser);
    }

    /**
     * 从 session 中读取缓存的 LoginUser
     */
    public static LoginUser getLoginUserFromSession() {
        try {
            return (LoginUser) StpUtil.getSession().get(SESSION_KEY_LOGIN_USER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 清除 session 中的 LoginUser 缓存
     * 在切换租户/空间/角色后调用，确保下次请求从数据库重新加载
     */
    public static void clearLoginUserSession() {
        try {
            StpUtil.getSession().delete(SESSION_KEY_LOGIN_USER);
        } catch (Exception e) {
            // session 可能已过期，忽略
        }
    }
}
