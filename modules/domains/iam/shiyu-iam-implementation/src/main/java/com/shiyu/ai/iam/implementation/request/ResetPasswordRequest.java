package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 重置密码请求 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * password 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "新密码（为空则自动生成随机密码）")
    private String password;
}
