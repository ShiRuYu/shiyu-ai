package com.shiyu.ai.auth.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改密码请求
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码（需与当前密码匹配）")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码")
    private String newPassword;
}
