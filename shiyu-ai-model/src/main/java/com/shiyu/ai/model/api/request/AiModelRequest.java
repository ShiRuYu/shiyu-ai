package com.shiyu.ai.model.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiModelRequest {
    private Long platformId;
    @NotBlank private String modelName;
    private String displayName;
    private String description;
    private String modelConfig;
    private String isDefault;
    private Integer sort;
    private String status;
}
