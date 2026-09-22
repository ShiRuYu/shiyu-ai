package com.shiyu.ai.common.foundation.context;

import com.shiyu.ai.common.foundation.context.model.UserContext;

import com.shiyu.ai.common.foundation.enums.UserTypeEnum;

/**
 * 管理 用户 Context 相关的运行时状态、注册信息或临时数据。
 */
public final class UserContextHolder {

    private UserContextHolder() {}

    /**
     * 更新或设置 用户 Context 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userContext 用于完成本次业务处理的 userContext 参数。
     */
    public static void setContext(UserContext userContext) {
        UserGlobalContext.set(userContext);
    }

    /**
     * 查询 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Context 相关操作生成的结果数据。
     */
    public static UserContext getContext() {
        return UserGlobalContext.get();
    }

    /**
     * 删除或移除 用户 Context 相关业务操作，并维护必要的状态和协作关系。
     */
    public static void clearContext() {
        UserGlobalContext.clear();
    }

    /**
     * 查询 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Context 相关操作生成的结果数据。
     */
    public static UserContext getUserContext() {
        return getContext();
    }

    /**
     * 查询 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Context 相关操作生成的结果数据。
     */
    public static Long getUserId() {
        UserContext user = getUserContext();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 查询 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Context 相关操作生成的结果数据。
     */
    public static String getUsername() {
        UserContext user = getUserContext();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 查询 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Context 相关操作生成的结果数据。
     */
    public static UserTypeEnum getUserType() {
        UserContext user = getUserContext();
        return user != null ? user.getUserType() : null;
    }

    /**
     * 校验或判断 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
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
     * 查询 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Context 相关操作生成的结果数据。
     */
    public static Long getCurrentRoleId() {
        UserContext user = getUserContext();
        return user != null ? user.getCurrentRoleId() : null;
    }

    /**
     * 查询 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回 用户 Context 相关操作生成的结果数据。
     */
    public static String getSwitchMode() {
        UserContext user = getUserContext();
        return user != null ? user.getSwitchMode() : null;
    }

    /**
     * 校验或判断 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public static boolean isParentSuperAdminSwitch() {
        UserContext user = getUserContext();
        return user != null && user.isParentSuperAdminSwitch();
    }

    /**
     * 校验或判断 用户 Context 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public static boolean isSuperAdmin() {
        UserContext user = getUserContext();
        return user != null && user.isSuperAdmin();
    }
}
