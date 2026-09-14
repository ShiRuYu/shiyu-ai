package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 修改密码请求 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * oldPassword 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码（需与当前密码匹配）")
    private String oldPassword;

    /**
     * newPassword 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码")
    private String newPassword;
}
