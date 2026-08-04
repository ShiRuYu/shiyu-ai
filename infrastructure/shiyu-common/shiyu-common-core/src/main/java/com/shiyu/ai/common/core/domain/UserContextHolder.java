package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.UserTypeEnum;

import java.util.List;

/**
 * 登录上下文持有者
 */
public final class UserContextHolder {

    private UserContextHolder() {}

    public static void setContext(UserContext userContext) {
        UserGlobalContext.set(userContext);
    }

    public static UserContext getContext() {
        return UserGlobalContext.get();
    }

    public static void clearContext() {
        UserGlobalContext.clear();
    }

    public static UserContext getUserContext() {
        return getContext();
    }

    public static Long getUserId() {
        UserContext user = getUserContext();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        UserContext user = getUserContext();
        return user != null ? user.getUsername() : null;
    }

    public static UserTypeEnum getUserType() {
        UserContext user = getUserContext();
        return user != null ? user.getUserType() : null;
    }

    public static boolean isLogin() {
        return getUserContext() != null;
    }

    /** @deprecated 改用 getCurrentTenantId() */
    @Deprecated
    public static Long getTenantId() {
        return getCurrentTenantId();
    }

    /** 用户默认/登录租户。 */
    public static Long getHomeTenantId() {
        UserContext user = getUserContext();
        return user != null ? user.getHomeTenantId() : null;
    }

    /** 当前操作租户 ID。 */
    public static Long getCurrentTenantId() {
        UserContext user = getUserContext();
        return user != null ? user.getCurrentTenantId() : null;
    }

    /** 当前角色编码 */
    public static String getCurrentRoleCode() {
        UserContext user = getUserContext();
        return user != null ? user.getCurrentRoleCode() : null;
    }

    public static Long getCurrentRoleId() {
        UserContext user = getUserContext();
        return user != null ? user.getCurrentRoleId() : null;
    }

    public static String getSwitchMode() {
        UserContext user = getUserContext();
        return user != null ? user.getSwitchMode() : null;
    }

    public static boolean isParentSuperAdminSwitch() {
        UserContext user = getUserContext();
        return user != null && user.isParentSuperAdminSwitch();
    }

    public static boolean isSuperAdmin() {
        UserContext user = getUserContext();
        return user != null && user.isSuperAdmin();
    }
}
