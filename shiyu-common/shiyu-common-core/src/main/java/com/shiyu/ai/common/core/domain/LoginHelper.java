package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.DeviceType;
import com.shiyu.ai.common.core.enums.UserType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * 登录鉴权助手
 * <p>
 * user_type 为 用户类型 同一个用户表 可以有多种用户类型 例如 pc,app
 * deivce 为 设备类型 同一个用户类型 可以有 多种设备类型 例如 web,ios
 * 可以组成 用户类型与设备类型多对多的 权限灵活控制
 * <p>
 * 多用户体系 针对 多种用户类型 但权限控制不一致
 * 可以组成 多用户类型表与多设备类型 分别控制权限
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String TENANT_KEY = "tenantId";
    public static final String USER_KEY = "userId";

    /**
     * 登录系统
     *
     * @param loginUser 登录用户信息
     */
    public static void login(LoginUser loginUser) {
        loginByDevice(loginUser, null);
    }

    /**
     * 登录系统 基于 设备类型
     * 针对相同用户体系不同设备
     *
     * @param loginUser 登录用户信息
     */
    public static void loginByDevice(LoginUser loginUser, DeviceType deviceType) {
    }

    /**
     * 获取用户(多级缓存)
     */
    public static LoginUser getLoginUser() {
        return null;
    }


    public static <T extends LoginUser> T getLoginUser(String token) {
        return null;
    }

    /**
     * 获取用户id
     */
    public static Long getUserId() {
        return null;
    }

    /**
     * 获取租户ID
     */
    public static String getTenantId() {
        return null;
    }

    /**
     * 获取部门ID
     */
    public static Long getDeptId() {
        return getLoginUser().getDeptId();
    }

    /**
     * 获取用户账户
     */
    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    /**
     * 获取用户类型
     */
    public static UserType getUserType() {
        return null;
    }

    /**
     * 是否为超级管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isSuperAdmin(Long userId) {
        return false;
    }

    public static boolean isSuperAdmin() {
        return isSuperAdmin(getUserId());
    }

    /**
     * 是否为超级管理员
     *
     * @param rolePermission 角色权限标识组
     * @return 结果
     */
    public static boolean isTenantAdmin(Set<String> rolePermission) {
        return false;
    }

    public static boolean isTenantAdmin() {
        return isTenantAdmin(getLoginUser().getRolePermission());
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 结果
     */
    public static boolean isLogin() {
        try {
            return getLoginUser() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
