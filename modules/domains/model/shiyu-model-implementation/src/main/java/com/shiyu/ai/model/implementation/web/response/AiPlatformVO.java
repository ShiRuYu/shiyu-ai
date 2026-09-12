package com.shiyu.ai.model.implementation.web.response;

import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {@code AiPlatformVO} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@AutoMappers({
    @AutoMapper(target = AiPlatformBO.class),
    @AutoMapper(target = AiPlatformResponse.class)
})
public class AiPlatformVO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    @Schema(
            description = "平台接口适配器协议",
            allowableValues = {"OPENAI_COMPATIBLE", "OLLAMA"})
    private String adapterType;

    /**
     * baseUrl 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String baseUrl;
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
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;
}
