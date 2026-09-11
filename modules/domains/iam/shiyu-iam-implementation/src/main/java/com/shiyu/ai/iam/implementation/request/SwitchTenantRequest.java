package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 切换租户请求 */
@Data
@Schema(description = "切换租户请求")
public class SwitchTenantRequest implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotNull(message = "租户ID不能为空")
    @Schema(description = "目标租户ID")
    private Long tenantId;
}
