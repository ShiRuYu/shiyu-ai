package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "登录/注册请求")
public class LoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    @Schema(description = "验证码")
    private String captcha;

    @Schema(description = "验证码Key（从 /captcha 接口获取）")
    private String captchaKey;

    @Schema(description = "登录后默认角色ID（可选）")
    private Long roleId;

    @Schema(description = "邮箱（注册时必填）")
    private String email;

    @Schema(description = "手机号")
    private String phone;
}
