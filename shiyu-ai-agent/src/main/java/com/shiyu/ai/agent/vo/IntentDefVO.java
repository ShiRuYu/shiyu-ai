package com.shiyu.ai.agent.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class IntentDefVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agentId;
    private String name;
    private String code;
    private String category;
    private String description;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
