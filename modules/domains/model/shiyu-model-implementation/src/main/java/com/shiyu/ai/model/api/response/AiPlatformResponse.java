package com.shiyu.ai.model.api.response;

import com.shiyu.ai.model.domain.model.AiPlatformBO;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiPlatformBO.class)
public class AiPlatformResponse {
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
