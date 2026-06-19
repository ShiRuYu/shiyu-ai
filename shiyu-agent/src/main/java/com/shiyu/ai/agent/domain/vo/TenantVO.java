package com.shiyu.ai.agent.domain.vo;

import com.shiyu.ai.agent.domain.bo.TenantBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = TenantBO.class)
public class TenantVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String name;

    private String contactName;

    private String contactPhone;

    private String address;

    private String domain;

    private String intro;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
