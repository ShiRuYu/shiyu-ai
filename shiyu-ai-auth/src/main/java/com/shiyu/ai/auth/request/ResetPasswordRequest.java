package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重置密码请求
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "新密码（为空则自动生成随机密码）")
    private String password;
}
