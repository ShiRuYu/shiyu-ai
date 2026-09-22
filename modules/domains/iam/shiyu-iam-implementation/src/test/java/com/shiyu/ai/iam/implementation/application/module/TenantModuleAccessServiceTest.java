package com.shiyu.ai.iam.implementation.application.module;

import com.shiyu.ai.common.foundation.module.BusinessModuleDescriptor;
import com.shiyu.ai.iam.implementation.port.repository.TenantModuleAccessRepository;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证 租户 Module Access Service 相关功能、边界条件、异常路径和协作行为。
 */
class TenantModuleAccessServiceTest {

    private final TenantModuleAccessRepository repository = mock(TenantModuleAccessRepository.class);
    private final TenantModuleAccessService service = new TenantModuleAccessService(repository);

    @Test
    void delegatesTenantScopedModuleStateToTheIamRepository() {
        TenantId tenantId = new TenantId(7L);
        when(repository.isEnabled(tenantId, "education")).thenReturn(true);

        assertTrue(service.isEnabled(tenantId, "education"));
        verify(repository).isEnabled(tenantId, "education");
    }

    @Test
    void rejectsMissingTenantOrModuleIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> service.isEnabled(null, "education"));
        assertThrows(IllegalArgumentException.class, () -> service.isEnabled(new TenantId(7L), " "));
    }

    @Test
    void initializesOnlyTheModulesPublishedByTheComposition() {
        TenantId tenantId = new TenantId(7L);
        TenantModuleAccessService compositionAwareService = new TenantModuleAccessService(
                repository,
                java.util.List.of(BusinessModuleDescriptor.of(
                        "education", "教育", "/api/education", "education:")));

        compositionAwareService.initializeTenantDefaults(tenantId);

        verify(repository).initializeTenantDefaults(tenantId, java.util.List.of("education"));
    }
}
