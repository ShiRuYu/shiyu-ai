package com.shiyu.ai.iam.implementation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * 封装 认证 Code 操作所需的请求条件和输入数据。
 */
@Data
public class AuthCodeRequest {
    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank
    @Size(max = 64)
    private String code;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
}
