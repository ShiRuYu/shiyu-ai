package com.shiyu.ai.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设置时区请求
 */
@Data
public class SetTimezoneRequest {
    
    /**
     * 时区标识符，例如：Asia/Shanghai
     */
    @NotBlank(message = "时区不能为空")
    private String timezone;
}
