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

    /** 当前作用域租户 ID（用户切换到的租户，即角色分配的目标） */
    private Long scopeTenantId;

    /** 可见租户 ID 列表（scopeTenantId 自身 + 所有后代）
     *  用于 ContextTenantFactory 控制数据可见范围 */
    private List<Long> visibleTenantIds;

    /** 子租户筛选器（可选，在可见范围内进一步限定只看某个租户的数据） */
    private Long scopedTenantId;

    /** 当前角色编码 */
    private String currentRoleCode;

    public boolean isSuperAdmin() {
        return "super".equals(currentRoleCode);
    }
}
