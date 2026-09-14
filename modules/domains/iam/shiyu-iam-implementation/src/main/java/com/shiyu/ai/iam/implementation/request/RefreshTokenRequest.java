package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 刷新令牌请求 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * accessToken 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "访问令牌不能为空")
    @Schema(description = "当前有效的访问令牌")
    private String accessToken;
}
