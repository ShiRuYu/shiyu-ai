package com.shiyu.ai.iam.implementation.api.response;

import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = UserScopeRoleBO.class)
public class AuthScopeRoleResponse {
    private Long userId;
    private Long tenantId;
    private Long roleId;
    private Integer status;
    private Integer delFlag;
}

