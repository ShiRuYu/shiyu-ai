package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code FilterSubTenantRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@Schema(description = "子租户筛选请求")
public class FilterSubTenantRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * subTenantId 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotNull(message = "子租户ID不能为空")
    @Schema(description = "目标子租户ID")
    private Long subTenantId;
}
