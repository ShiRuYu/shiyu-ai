package com.shiyu.ai.auth.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户租户分配信息。
 */
@Data
public class UserTenantAssignmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private String tenantName;
    private Long roleId;
    private String roleName;
    private String roleCode;
}
