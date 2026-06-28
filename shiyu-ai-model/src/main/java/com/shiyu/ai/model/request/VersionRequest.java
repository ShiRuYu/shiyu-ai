package com.shiyu.ai.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VersionRequest {

    @NotBlank(message = "版本号不能为空")
    private String versionNumber;

    private String description;

    private Long copyFromVersionId;
}
