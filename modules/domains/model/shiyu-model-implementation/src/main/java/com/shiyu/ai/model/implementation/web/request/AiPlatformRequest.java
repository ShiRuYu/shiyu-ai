package com.shiyu.ai.model.implementation.web.request;

import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * {@code AiPlatformRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@AutoMapper(target = AiPlatformBO.class, reverseConvertGenerate = false)
public class AiPlatformRequest {
    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank private String name;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank private String code;

    @Schema(
            description = "平台接口适配器协议",
            allowableValues = {"OPENAI_COMPATIBLE", "OLLAMA"})
    private String adapterType;

    /**
     * baseUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String baseUrl;
    /**
     * apiKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String apiKey;
    /**
     * temperature 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double temperature;
    /**
     * maxTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer maxTokens;
    /**
     * maxRetries 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer maxRetries;
    /**
     * availableModels 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String availableModels;
    /**
     * extraConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String extraConfig;
    /**
     * isDefault 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String isDefault;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private String status;
    /**
     * sort 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer sort;
    /**
     * remark 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String remark;
}
