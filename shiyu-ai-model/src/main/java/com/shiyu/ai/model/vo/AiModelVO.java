package com.shiyu.ai.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.shiyu.ai.dal.model.bo.AiModelBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@AutoMapper(target = AiModelBO.class)
public class AiModelVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
