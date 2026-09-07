package com.shiyu.ai.model.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.shiyu.ai.model.api.response.AiPlatformResponse;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;

@Data
@AutoMappers({
        @AutoMapper(target = AiPlatformBO.class),
        @AutoMapper(target = AiPlatformResponse.class)
})
public class AiPlatformVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String code;
    @Schema(description = "平台接口适配器协议", allowableValues = {"OPENAI_COMPATIBLE", "OLLAMA"})
    private String adapterType;
    private String baseUrl;
    private Double temperature;
    private Integer maxTokens;
    private Integer maxRetries;
    private String availableModels;
    private String extraConfig;
    private String isDefault;
    private String status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
