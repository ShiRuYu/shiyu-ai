package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.UserTypeEnum;

/**
 * 登录上下文持有者
 * 提供统一的用户上下文存取操作（静态工具类）
 * 继承自原 LoginHelper 的上下文管理部分
 */
public final class LoginContextHolder {

    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String TENANT_KEY = "tenantId";
    public static final String USER_KEY = "userId";

    private LoginContextHolder() {}

    public static void setContext(LoginUser loginUser) {
        UserGlobalContext.set(loginUser);
    }

    public static LoginUser getContext() {
        return UserGlobalContext.get();
    }

    public static void clearContext() {
        UserGlobalContext.clear();
    }

    public static LoginUser getLoginUser() {
        return getContext();
    }

    public static Long getUserId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUsername() : null;
    }

    public static UserTypeEnum getUserType() {
        LoginUser user = getLoginUser();
        return user != null ? user.getUserType() : null;
    }

    public static boolean isLogin() {
        return getLoginUser() != null;
    }
}