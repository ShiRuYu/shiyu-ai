package com.shiyu.ai.model.api.response;

import com.shiyu.ai.model.domain.model.AiPlatformBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = AiPlatformBO.class)
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
