package com.shiyu.ai.auth.api.response;

import com.shiyu.ai.auth.domain.model.RoleBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = RoleBO.class)
public class AuthRoleResponse {
    private Long id;
    private Long tenantId;
    private String code;
    private Integer status;
    private Integer delFlag;
}
