package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 刷新令牌请求
 */
@Data
public class RefreshTokenRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "访问令牌不能为空")
    private String accessToken;
}
