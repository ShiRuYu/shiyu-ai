package com.shiyu.ai.web.auth;

import com.shiyu.ai.auth.api.response.AuthRoleResponse;
import com.shiyu.ai.auth.api.response.AuthScopeRoleResponse;
import com.shiyu.ai.auth.api.response.AuthTenantResponse;
import com.shiyu.ai.auth.api.response.AuthUserResponse;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.RoleBOToAuthRoleResponseMapperImpl;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.domain.model.TenantBOToAuthTenantResponseMapperImpl;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserBOToAuthUserResponseMapperImpl;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBOToAuthScopeRoleResponseMapperImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("dev")
class AuthContextMappingTest {

    @Test
    void generatesEveryMappingRequiredByTheRequestContextInterceptor() {
        UserBO user = new UserBO();
        user.setId(2L);
        user.setUsername("admin");
        user.setStatus(1);
        user.setDelFlag(0);
        AuthUserResponse userResponse =
                new UserBOToAuthUserResponseMapperImpl().convert(user);

        TenantBO tenant = new TenantBO();
        tenant.setId(1L);
        tenant.setName("默认租户");
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        AuthTenantResponse tenantResponse =
                new TenantBOToAuthTenantResponseMapperImpl().convert(tenant);

        RoleBO role = new RoleBO();
        role.setId(1L);
        role.setTenantId(1L);
        role.setCode("super");
        role.setStatus(1);
        role.setDelFlag(0);
        AuthRoleResponse roleResponse =
                new RoleBOToAuthRoleResponseMapperImpl().convert(role);

        UserScopeRoleBO assignment = new UserScopeRoleBO();
        assignment.setUserId(2L);
        assignment.setTenantId(1L);
        assignment.setRoleId(1L);
        assignment.setStatus(1);
        assignment.setDelFlag(0);
        AuthScopeRoleResponse assignmentResponse =
                new UserScopeRoleBOToAuthScopeRoleResponseMapperImpl().convert(assignment);

        assertAll(
                () -> assertEquals("admin", userResponse.getUsername()),
                () -> assertEquals(1, userResponse.getStatus()),
                () -> assertEquals("默认租户", tenantResponse.getName()),
                () -> assertEquals("super", roleResponse.getCode()),
                () -> assertEquals(2L, assignmentResponse.getUserId()),
                () -> assertEquals(1L, assignmentResponse.getTenantId()),
                () -> assertEquals(1L, assignmentResponse.getRoleId()));
    }
}
