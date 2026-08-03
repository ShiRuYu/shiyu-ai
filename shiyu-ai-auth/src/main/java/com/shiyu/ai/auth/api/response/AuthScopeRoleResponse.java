package com.shiyu.ai.auth.api.response;
import lombok.Data;
@Data public class AuthScopeRoleResponse { private Long userId; private Long tenantId; private Long roleId; private Integer status; private Integer delFlag; }
