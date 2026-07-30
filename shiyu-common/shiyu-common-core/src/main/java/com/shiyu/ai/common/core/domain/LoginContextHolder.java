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

    /** @deprecated 改用 getCurrentTenantId() */
    @Deprecated
    public static Long getTenantId() {
        return getCurrentTenantId();
    }

    /** 用户默认/登录租户。 */
    public static Long getHomeTenantId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getHomeTenantId() : null;
    }

    /** 当前操作租户 ID。 */
    public static Long getCurrentTenantId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getCurrentTenantId() : null;
    }

    /** 当前角色编码 */
    public static String getCurrentRoleCode() {
        LoginUser user = getLoginUser();
        return user != null ? user.getCurrentRoleCode() : null;
    }

    public static Long getCurrentRoleId() {
        LoginUser user = getLoginUser();
        return user != null ? user.getCurrentRoleId() : null;
    }

    public static String getSwitchMode() {
        LoginUser user = getLoginUser();
        return user != null ? user.getSwitchMode() : null;
    }

    public static boolean isParentSuperAdminSwitch() {
        LoginUser user = getLoginUser();
        return user != null && user.isParentSuperAdminSwitch();
    }

    public static boolean isSuperAdmin() {
        LoginUser user = getLoginUser();
        return user != null && user.isSuperAdmin();
    }
}
