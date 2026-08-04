package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设置时区请求
 */
@Data
@Schema(description = "设置时区请求")
public class SetTimezoneRequest {
    
    /**
     * 时区标识符，例如：Asia/Shanghai
     */
    @NotBlank(message = "时区不能为空")
    @Schema(description = "时区名称，如 Asia/Shanghai")
    private String timezone;
}
