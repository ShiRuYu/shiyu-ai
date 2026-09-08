package com.shiyu.ai.model.api.request;

import com.shiyu.ai.model.domain.model.AiPlatformBO;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@AutoMapper(target = AiPlatformBO.class, reverseConvertGenerate = false)
public class AiPlatformRequest {
    @NotBlank private String name;
    @NotBlank private String code;
    @Schema(description = "平台接口适配器协议", allowableValues = {"OPENAI_COMPATIBLE", "OLLAMA"})
    private String adapterType;
    private String baseUrl;
    private String apiKey;
    private Double temperature;
    private Integer maxTokens;
    private Integer maxRetries;
    private String availableModels;
    private String extraConfig;
    private String isDefault;
    private String status;
    private Integer sort;
    private String remark;
}
