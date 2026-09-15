package com.shiyu.ai.web.module;

import com.shiyu.ai.common.core.module.BusinessModuleDescriptor;
import com.shiyu.ai.iam.contract.module.TenantModuleAccessPort;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.web.interceptor.BusinessModuleAccessInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("dev")
class ModuleAccessInterceptorTest {

    private final TenantModuleAccessPort access = mock(TenantModuleAccessPort.class);
    private final BusinessModuleAccessInterceptor interceptor =
            new BusinessModuleAccessInterceptor(
                    List.of(BusinessModuleDescriptor.of(
                            "education", "教育", "/api/education", "education:")),
                    access);

    @AfterEach
    void clearScope() {
        TenantScope.clear();
    }

    @Test
    void allowsAnEnabledModuleForTheCurrentTenant() throws Exception {
        TenantId tenantId = new TenantId(7L);
        TenantScope.set(tenantId);
        when(access.isEnabled(tenantId, "education")).thenReturn(true);

        assertTrue(interceptor.preHandle(request("/api/education/course/list"),
                new MockHttpServletResponse(), new Object()));
    }

    @Test
    void deniesAClosedModuleWithForbiddenResponse() throws Exception {
        TenantId tenantId = new TenantId(7L);
        TenantScope.set(tenantId);
        when(access.isEnabled(tenantId, "education")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request("/api/education/course/list"), response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void ignoresRoutesOwnedByAnotherModule() throws Exception {
        assertTrue(interceptor.preHandle(request("/api/knowledge/search"),
                new MockHttpServletResponse(), new Object()));
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setDispatcherType(jakarta.servlet.DispatcherType.REQUEST);
        return request;
    }
}
