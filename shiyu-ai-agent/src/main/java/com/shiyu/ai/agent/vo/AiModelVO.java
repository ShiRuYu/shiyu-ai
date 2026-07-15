package com.shiyu.ai.agent.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AiModelVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long platformId;
    private String name;
    private String code;
    private String type;
    private Boolean isDefault;
    private String status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
