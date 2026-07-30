package com.shiyu.ai.common.core.domain;

import com.google.common.collect.Maps;
import com.shiyu.ai.common.core.enums.DeviceTypeEnum;
import com.shiyu.ai.common.core.enums.UserTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录用户
 */
@Data
@NoArgsConstructor
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String token;
    private UserTypeEnum userType;
    private Long loginTime;
    private Long expireTime;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private DeviceTypeEnum os;
    private String username;
    private String nickName;
    private String avatar;
    private java.util.Map<String, Object> extInfo = Maps.newHashMap();

    /** 用户默认/登录租户，代表用户身份归属。 */
    private Long homeTenantId;

    /** 当前操作租户，业务数据严格按此租户过滤。 */
    private Long currentTenantId;

    /** 当前租户下生效的角色。 */
    private Long currentRoleId;
    private String currentRoleCode;

    public boolean isSuperAdmin() {
        return "tenant_super".equals(currentRoleCode)
                || "super".equals(currentRoleCode);
    }

    /** NORMAL 或 PARENT_SUPER_ADMIN。 */
    private String switchMode;

    /** 父租户超级管理员切换前的租户。 */
    private Long switchFromTenantId;

    /** @deprecated 严格单租户模式不再使用。 */
    @Deprecated
    private Long rootTenantId;

    public boolean isParentSuperAdminSwitch() {
        return "PARENT_SUPER_ADMIN".equals(switchMode);
    }
}
