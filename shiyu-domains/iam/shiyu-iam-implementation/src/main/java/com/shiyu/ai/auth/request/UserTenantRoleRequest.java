package com.shiyu.ai.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户租户角色分配项。
 */
@Data
@Schema(description = "用户租户角色分配项")
public class UserTenantRoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tenantId;

    @Schema(description = "角色 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;
}
