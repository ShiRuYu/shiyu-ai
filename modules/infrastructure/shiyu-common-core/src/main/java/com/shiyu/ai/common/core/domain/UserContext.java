package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.DeviceTypeEnum;
import com.shiyu.ai.common.core.enums.UserTypeEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;

/** 登录用户 */
@Data
@NoArgsConstructor
@SuppressWarnings("serial")
public class UserContext implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;
    /**
     * 令牌，表示当前对象中的对应属性。
     */
    private String token;
    /**
     * 用户类型，表示当前对象中的对应属性。
     */
    private UserTypeEnum userType;
    /**
     * loginTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long loginTime;
    /**
     * expireTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long expireTime;
    /**
     * ipaddr 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String ipaddr;
    /**
     * loginLocation 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String loginLocation;
    /**
     * browser 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String browser;
    /**
     * os 属性，保存当前对象中的业务数据或协作依赖。
     */
    private DeviceTypeEnum os;
    /**
     * username 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String username;
    /**
     * nickName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nickName;
    /**
     * avatar 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String avatar;
    private java.util.Map<String, Object> extInfo = new HashMap<>();

    /** 用户默认/登录租户，代表用户身份归属。 */
    private Long homeTenantId;

    /** 当前操作租户，业务数据严格按此租户过滤。 */
    private Long currentTenantId;

    /** 当前租户下生效的角色。 */
    private Long currentRoleId;

    /**
     * currentRoleCode 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String currentRoleCode;

    /**
     * {@code isSuperAdmin} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isSuperAdmin() {
        return "tenant_super".equals(currentRoleCode) || "super".equals(currentRoleCode);
    }

    /** NORMAL 或 PARENT_SUPER_ADMIN。 */
    private String switchMode;

    /** 父租户超级管理员切换前的租户。 */
    private Long switchFromTenantId;

    /**
     * {@code isParentSuperAdminSwitch} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isParentSuperAdminSwitch() {
        return "PARENT_SUPER_ADMIN".equals(switchMode);
    }
}
