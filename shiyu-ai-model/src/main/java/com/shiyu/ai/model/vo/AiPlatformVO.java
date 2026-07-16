package com.shiyu.ai.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.shiyu.ai.dal.bo.model.AiPlatformBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@AutoMapper(target = AiPlatformBO.class)
public class AiPlatformVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
