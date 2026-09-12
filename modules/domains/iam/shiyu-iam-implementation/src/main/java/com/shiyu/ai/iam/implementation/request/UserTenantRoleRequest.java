package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 用户租户角色分配项。 */
@Data
@Schema(description = "用户租户角色分配项")
public class UserTenantRoleRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    @Schema(description = "租户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tenantId;

    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    @Schema(description = "角色 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;
}
