package com.shiyu.ai.agent.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.shiyu.ai.common.core.domain.LoginHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token 认证助手
 * LoginHelper 的 Sa-Token 实现类
 * 
 * 注意：此类只在 shiyu-agent 模块中使用，用于封装 Sa-Token 的具体实现
 */
@Slf4j
public class SaTokenHelper extends LoginHelper {

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
     * 
     * @return 用户 ID
     */
    public static Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前 Token 值
     * 
     * @return Token 值
     */
    public static String getCurrentToken() {
        return StpUtil.getTokenValue();
    }
}
