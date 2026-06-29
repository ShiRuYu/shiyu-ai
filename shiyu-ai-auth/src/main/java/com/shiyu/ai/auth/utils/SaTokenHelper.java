package com.shiyu.ai.auth.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.shiyu.ai.common.core.domain.LoginHelper;
import com.shiyu.ai.common.core.domain.LoginUser;
import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token 璁よ瘉鍔╂墜
 * LoginHelper 鐨?Sa-Token 瀹炵幇绫?
 * 
 * 娉ㄦ剰锛氭绫诲彧鍦?shiyu-agent 妯″潡涓娇鐢紝鐢ㄤ簬灏佽 Sa-Token 鐨勫叿浣撳疄鐜?
 */
@Slf4j
public class SaTokenHelper extends LoginHelper {

    /** session 涓?LoginUser 鐨勯敭鍚?*/
    private static final String SESSION_KEY_LOGIN_USER = "loginUser";

    /**
     * 鍗曚緥瀹炰緥锛堢敤浜庤皟鐢ㄥ疄渚嬫柟娉曪級
     */
    private static final SaTokenHelper INSTANCE = new SaTokenHelper();

    /**
     * 鑾峰彇瀹炰緥
     */
    public static SaTokenHelper getInstance() {
        return INSTANCE;
    }

    // ==================== 瀹炵幇鎶借薄鏂规硶 ====================

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
            log.warn("浠?Token 涓幏鍙栫敤鎴?ID 澶辫触: {}", e.getMessage());
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

    // ==================== 棰濆宸ュ叿鏂规硶 ====================

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛 ID锛堝熀浜?Sa-Token锛?
     */
    public static Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 鑾峰彇褰撳墠 Token 鍊?
     */
    public static String getCurrentToken() {
        return StpUtil.getTokenValue();
    }

    /**
     * 缂撳瓨褰撳墠 LoginUser 鍒?session
     */
    public static void saveLoginUserToSession(LoginUser loginUser) {
        try {
            StpUtil.getSession().set(SESSION_KEY_LOGIN_USER, loginUser);
        } catch (Exception e) {
            log.warn("写入 session 失败（可能已过期）: {}", e.getMessage());
            return;
        }
        // 必须持久化 session 修改，否则 extInfo 中存的 SaSession 不包含 loginUser
        // 服务重启后 Caffeine 缓存丢失，getLoginUserFromSession() 会返回 null
        try {
            StpUtil.getStpLogic().getSaTokenDao().updateSession(StpUtil.getSession());
        } catch (Exception e) {
            log.error("持久化 session 失败", e);
        }
    }

    /**
     * 浠?session 涓鍙栫紦瀛樼殑 LoginUser
     */
    public static LoginUser getLoginUserFromSession() {
        try {
            return (LoginUser) StpUtil.getSession().get(SESSION_KEY_LOGIN_USER);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 娓呴櫎 session 涓殑 LoginUser 缂撳瓨
     * 鍦ㄥ垏鎹㈢鎴?绌洪棿/瑙掕壊鍚庤皟鐢紝纭繚涓嬫璇锋眰浠庢暟鎹簱閲嶆柊鍔犺浇
     */
    public static void clearLoginUserSession() {
        try {
            StpUtil.getSession().delete(SESSION_KEY_LOGIN_USER);
            try {
                StpUtil.getStpLogic().getSaTokenDao().updateSession(StpUtil.getSession());
            } catch (Exception ignored) { }
        } catch (Exception e) {
            // session 鍙兘宸茶繃鏈燂紝蹇界暐
        }
    }
}
