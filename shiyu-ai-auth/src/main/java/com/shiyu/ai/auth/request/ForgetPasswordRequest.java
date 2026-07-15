package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 忘记密码请求
 */
@Data
public class ForgetPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
