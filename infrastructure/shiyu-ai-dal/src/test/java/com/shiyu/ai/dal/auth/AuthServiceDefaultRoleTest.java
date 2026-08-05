package com.shiyu.ai.dal.auth;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.auth.port.repository.AuthRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.port.repository.TenantRoleRepository;
import com.shiyu.ai.auth.port.repository.UserRepository;
import com.shiyu.ai.auth.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.auth.service.CaptchaService;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.auth.service.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceDefaultRoleTest {

    @Test
    void assignsExplicitUserRoleInsteadOfCopyingBootstrapAdministratorRole() {
        AuthRepository authRepository = mock(AuthRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserScopeRoleRepository userScopeRoleRepository = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoleRepository = mock(TenantRoleRepository.class);
        TenantRepository tenantRepository = mock(TenantRepository.class);
        MenuService menuService = mock(MenuService.class);
        CaptchaService captchaService = mock(CaptchaService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthServiceImpl service = new AuthServiceImpl(authRepository, userRepository,
                userScopeRoleRepository, tenantRoleRepository, tenantRepository,
                menuService, captchaService, request);

        UserBO user = new UserBO();
        user.setId(42L);
        RoleBO userRole = new RoleBO();
        userRole.setId(3L);
        userRole.setCode("user");
        userRole.setName("User");
        when(userRepository.selectById(42L)).thenReturn(user);
        when(tenantRoleRepository.selectEnabledRoleByCode(1L, "user")).thenReturn(userRole);

        ReflectionTestUtils.invokeMethod(service, "assignDefaultTenantWorkspaceRole", 42L);

        verify(userRepository, never()).selectRolesByUserId(1L);
        verify(tenantRoleRepository).selectEnabledRoleByCode(1L, "user");
        ArgumentCaptor<UserScopeRoleBO> assignment = ArgumentCaptor.forClass(UserScopeRoleBO.class);
        verify(userScopeRoleRepository).insert(assignment.capture());
        assertEquals(42L, assignment.getValue().getUserId());
        assertEquals(1L, assignment.getValue().getTenantId());
        assertEquals(3L, assignment.getValue().getRoleId());
    }
}
