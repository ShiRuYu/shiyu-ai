package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.UserTypeEnum;

/** 登录上下文持有者 */
public final class UserContextHolder {

    private UserContextHolder() {}

    /**
     * {@code setContext} 写入或更新当前模块中的业务数据。
     *
     * @param userContext 参数值，用于执行当前操作。
     */
    public static void setContext(UserContext userContext) {
        UserGlobalContext.set(userContext);
    }

    /**
     * {@code getContext} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static UserContext getContext() {
        return UserGlobalContext.get();
    }

    /**
     * {@code clearContext} 执行当前类型定义的业务操作。
     */
    public static void clearContext() {
        UserGlobalContext.clear();
    }

    /**
     * {@code getUserContext} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static UserContext getUserContext() {
        return getContext();
    }

    /**
     * {@code getUserId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Long getUserId() {
        UserContext user = getUserContext();
        return user != null ? user.getUserId() : null;
    }

    /**
     * {@code getUsername} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String getUsername() {
        UserContext user = getUserContext();
        return user != null ? user.getUsername() : null;
    }

    /**
     * {@code getUserType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static UserTypeEnum getUserType() {
        UserContext user = getUserContext();
        return user != null ? user.getUserType() : null;
    }

    /**
     * {@code isLogin} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public static boolean isLogin() {
        return getUserContext() != null;
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

    /**
     * {@code getCurrentRoleId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Long getCurrentRoleId() {
        UserContext user = getUserContext();
        return user != null ? user.getCurrentRoleId() : null;
    }

    /**
     * {@code getSwitchMode} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String getSwitchMode() {
        UserContext user = getUserContext();
        return user != null ? user.getSwitchMode() : null;
    }

    /**
     * {@code isParentSuperAdminSwitch} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public static boolean isParentSuperAdminSwitch() {
        UserContext user = getUserContext();
        return user != null && user.isParentSuperAdminSwitch();
    }

    /**
     * {@code isSuperAdmin} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public static boolean isSuperAdmin() {
        UserContext user = getUserContext();
        return user != null && user.isSuperAdmin();
    }
}
