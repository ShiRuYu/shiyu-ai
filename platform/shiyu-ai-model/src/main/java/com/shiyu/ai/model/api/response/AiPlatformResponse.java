package com.shiyu.ai.model.api.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiPlatformResponse {
    private Long id;
    private String name;
    private String code;
    private String isDefault;
    private String status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
