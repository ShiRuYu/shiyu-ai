package com.shiyu.ai.auth.api.response;
import lombok.Data;
@Data public class AuthRoleResponse { private Long id; private Long tenantId; private String code; private Integer status; private Integer delFlag; }
