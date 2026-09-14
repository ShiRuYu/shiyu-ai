package com.shiyu.ai.model.implementation.web.request;

import com.shiyu.ai.model.implementation.domain.model.AiModelBO;

import io.github.linpeilie.annotations.AutoMapper;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * {@code AiModelRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@AutoMapper(target = AiModelBO.class, reverseConvertGenerate = false)
public class AiModelRequest {
    /**
     * platformId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long platformId;
    /**
     * modelName 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank private String modelName;

    /**
     * displayName 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String displayName;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * modelConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String modelConfig;
    /**
     * isDefault 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String isDefault;
    /**
     * sort 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer sort;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private String status;
}
