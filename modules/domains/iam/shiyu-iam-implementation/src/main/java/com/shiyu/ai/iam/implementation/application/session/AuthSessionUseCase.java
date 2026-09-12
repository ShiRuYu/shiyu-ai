package com.shiyu.ai.iam.implementation.application.session;

import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * 编排 AuthSession 应用用例。
 */
@Slf4j
public final class AuthSessionUseCase {
    /**
     * {@code refreshToken} 执行当前类型定义的业务操作。
     *
     * @param oldToken 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public String refreshToken(String oldToken) {
        log.info("刷新Token");
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(oldToken);
            if (userId == null) {
                log.warn("无效的access token");
                return null;
            }
            String newAccessToken = helper.refreshToken(userId);
            log.info("刷新Token成功, userIdPresent={}", userId > 0);
            return newAccessToken;
        } catch (Exception e) {
            log.error(
                    "刷新Token异常: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            return null;
        }
    }

    /**
     * {@code logout} 执行当前类型定义的业务操作。
     *
     * @param token 参数值，用于执行当前操作。
     */
    public void logout(String token) {
        log.info("注销退出");
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(token);
            if (userId != null) {
                helper.logout(userId);
                log.info("退出登录成功, userIdPresent={}", userId > 0);
            } else {
                log.warn("无效的token, 注销失败");
            }
        } catch (Exception e) {
            log.error(
                    "注销异常: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
        }
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
