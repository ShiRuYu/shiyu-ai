package com.shiyu.ai.auth.api.response;

import com.shiyu.ai.auth.domain.model.TenantBO;
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
