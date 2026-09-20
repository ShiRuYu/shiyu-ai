package com.shiyu.ai.iam.implementation.utils;

import cn.dev33.satoken.stp.StpUtil;

import com.shiyu.ai.common.core.auth.LoginHelper;
import com.shiyu.ai.common.core.context.model.UserContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 提供 Sa Token 相关的通用辅助操作，供业务和基础设施复用。
 */
@Slf4j
public class SaTokenHelper extends LoginHelper {

    /** session 中 UserContext 的键名 */
    private static final String SESSION_KEY_LOGIN_USER = "userContext";

    /** 单例实例（用于调用实例方法） */
    private static final SaTokenHelper INSTANCE = new SaTokenHelper();

    /** 获取实例 */
    public static SaTokenHelper getInstance() {
        return INSTANCE;
    }

    // ==================== 实现抽象方法 ====================

    /**
     * 执行 Sa Token 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 Sa Token 相关操作生成的结果数据。
     */
    @Override
    public String login(Long userId) {
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    /**
     * 执行 Sa Token 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 Sa Token 相关操作生成的结果数据。
     */
    @Override
    public String loginWithKickout(Long userId) {
        StpUtil.kickout(userId);
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    /**
     * 执行 Sa Token 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userId 当前操作涉及的用户标识。
     */
    @Override
    public void logout(Long userId) {
        StpUtil.logout(userId);
    }

    /**
     * 查询 Sa Token 相关业务数据，并返回处理结果。
     *
     * @param token 用于完成本次业务处理的 token 参数。
     * @return 返回 Sa Token 相关操作生成的结果数据。
     */
    @Override
    public Long getUserIdByToken(String token) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null) {
                return Long.parseLong(loginId.toString());
            }
        } catch (Exception e) {
            log.warn(
                    "从 Token 中获取用户 ID 失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
        return null;
    }

    /**
     * 执行 Sa Token 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @return 返回 Sa Token 相关操作生成的结果数据。
     */
    @Override
    public String refreshToken(Long userId) {
        StpUtil.logout(userId);
        StpUtil.login(userId);
        return StpUtil.getTokenValue();
    }

    /**
     * 查询 Sa Token 相关业务数据，并返回处理结果。
     *
     * @return 返回 Sa Token 相关操作生成的结果数据。
     */
    @Override
    public long getTokenTimeout() {
        return StpUtil.getTokenTimeout();
    }

    /**
     * 校验或判断 Sa Token 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean isFrameworkLogin() {
        return StpUtil.isLogin();
    }

    // ==================== 额外工具方法 ====================

    /** 获取当前登录用户 ID（基于 Sa-Token） */
    public static Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /** 获取当前 Token 值 */
    public static String getCurrentToken() {
        return StpUtil.getTokenValue();
    }

    /** 缓存当前 UserContext 到 session */
    public static void saveUserContextToSession(UserContext userContext) {
        StpUtil.getSession().set(SESSION_KEY_LOGIN_USER, userContext);
    }

    /** 从 session 中读取缓存的 UserContext */
    public static UserContext getUserContextFromSession() {
        try {
            return (UserContext) StpUtil.getSession().get(SESSION_KEY_LOGIN_USER);
        } catch (Exception e) {
            return null;
        }
    }

    /** 清除 session 中的 UserContext 缓存 在切换租户作用域或角色后调用，确保下次请求从数据库重新加载 */
    public static void clearUserContextSession() {
        try {
            StpUtil.getSession().delete(SESSION_KEY_LOGIN_USER);
        } catch (Exception e) {
            // session 可能已过期，忽略
        }
    }
}
