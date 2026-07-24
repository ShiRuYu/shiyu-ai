package com.shiyu.ai.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "子租户筛选请求")
public class FilterSubTenantRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "子租户ID不能为空")
    @Schema(description = "目标子租户ID")
    private Long subTenantId;
}
