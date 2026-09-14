package com.shiyu.ai.iam.implementation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * {@code AuthCodeRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
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
