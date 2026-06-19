package com.shiyu.ai.agent.domain.bo;

import com.shiyu.ai.agent.dal.dataobject.auth.TenantDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = TenantDO.class, reverseConvertGenerate = true)
public class TenantBO implements Serializable {

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

    private Integer delFlag;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
