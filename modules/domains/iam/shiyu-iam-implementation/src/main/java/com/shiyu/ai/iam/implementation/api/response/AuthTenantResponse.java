package com.shiyu.ai.iam.implementation.api.response;

import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = TenantBO.class)
public class AuthTenantResponse {
    private Long id;
    private String name;
    private Integer status;
    private Integer delFlag;
}

