package com.shiyu.ai.model.implementation.web.response;

import com.shiyu.ai.model.implementation.domain.model.AiModelBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMappers({@AutoMapper(target = AiModelBO.class), @AutoMapper(target = AiModelResponse.class)})
public class AiModelVO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

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
