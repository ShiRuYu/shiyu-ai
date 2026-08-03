package com.shiyu.ai.model.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiPlatformRequest {
    @NotBlank private String name;
    @NotBlank private String code;
    private String isDefault;
    private String status;
    private Integer sort;
    private String remark;
}
