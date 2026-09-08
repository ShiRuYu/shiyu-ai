package com.shiyu.ai.model.api.request;

import com.shiyu.ai.model.domain.model.AiModelBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@AutoMapper(target = AiModelBO.class, reverseConvertGenerate = false)
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
