package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 验证码登录请求
 */
@Data
@Schema(description = "验证码登录请求")
public class CodeLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码")
    private String code;
}
