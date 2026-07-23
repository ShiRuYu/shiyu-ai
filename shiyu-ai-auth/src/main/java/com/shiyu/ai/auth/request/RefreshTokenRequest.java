package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 刷新令牌请求
 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "访问令牌不能为空")
    @Schema(description = "当前有效的访问令牌")
    private String accessToken;
}
