package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重置密码请求
 */
@Data
public class ResetPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "密码不能为空")
    private String password;
}
