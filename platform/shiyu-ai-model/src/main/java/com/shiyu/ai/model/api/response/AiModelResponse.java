package com.shiyu.ai.model.api.response;

import com.shiyu.ai.model.domain.model.AiModelBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiModelBO.class)
public class AiModelResponse {
    private Long id;
    private Long platformId;
    private String modelName;
    private String displayName;
    private String description;
    private String modelConfig;
    private String platformName;
    private String isDefault;
    private Integer sort;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
