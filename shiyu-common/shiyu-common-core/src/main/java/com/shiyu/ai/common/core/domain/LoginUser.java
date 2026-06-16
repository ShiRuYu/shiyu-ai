package com.shiyu.ai.common.core.domain;

import com.google.common.collect.Maps;
import com.shiyu.ai.common.core.enums.DeviceTypeEnum;
import com.shiyu.ai.common.core.enums.UserTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 登录用户
 */

@Data
@NoArgsConstructor
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户类型
     */
    private UserTypeEnum userType;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private DeviceTypeEnum os;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户名
     */
    private String nickName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 扩展信息
     */
    private Map<String, Object> extInfo = Maps.newHashMap();

    /**
     * 当前租户ID
     */
    private Long tenantId;

    /**
     * 当前空间ID（null表示全部空间）
     */
    private Long currentWorkspaceId;

    /**
     * 用户所属的所有空间ID列表
     */
    private List<Long> workspaceIds;

    /**
     * 空间-角色映射：{workspaceId -> roleCode}
     */
    private Map<Long, String> workspaceRoleMap;

    public boolean isSuperAdmin() {
        return workspaceRoleMap != null
            && workspaceRoleMap.values().stream().anyMatch("super"::equals);
    }

}
