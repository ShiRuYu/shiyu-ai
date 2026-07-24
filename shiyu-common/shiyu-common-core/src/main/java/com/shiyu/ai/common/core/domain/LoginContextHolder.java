package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.UserTypeEnum;

import java.util.List;

/**
 * 登录上下文持有者
 */
public final class LoginContextHolder {

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

    /** @deprecated 改用 getScopeTenantId() */
    @Deprecated
    public static Long getTenantId() {
        return getScopeTenantId();
    }

    /** 当前作用域租户 ID */
    public static Long getScopeTenantId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getScopeTenantId() : null;
    }

    /** 可见租户 ID 列表（scope 自身 + 所有后代） */
    public static List<Long> getVisibleTenantIds() {
        LoginUser user = getLoginUser();
        return user != null ? user.getVisibleTenantIds() : null;
    }

    /** 子租户筛选器 */
    public static Long getScopedTenantId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getScopedTenantId() : null;
    }

    /** @deprecated 改用 getScopedTenantId() */
    @Deprecated
    public static Long getCurrentWorkspaceId() {
        return getScopedTenantId();
    }

    /** @deprecated 无需使用 */
    @Deprecated
    public static List<Long> getWorkspaceIds() {
        return null;
    }

    /** @deprecated 改用 currentRoleCode == 'super' */
    @Deprecated
    public static boolean isRootTenant() {
        return false;
    }

    public static boolean isSuperAdmin() {
        LoginUser user = getLoginUser();
        return user != null && user.isSuperAdmin();
    }
}
