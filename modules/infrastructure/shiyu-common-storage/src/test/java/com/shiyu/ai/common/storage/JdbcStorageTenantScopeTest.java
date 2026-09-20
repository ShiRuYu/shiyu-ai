package com.shiyu.ai.common.storage;

import com.shiyu.ai.common.storage.metadata.JdbcStorageMetadataStore;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * 验证 Jdbc Storage 租户 Scope 相关功能、边界条件、异常路径和协作行为。
 */
class JdbcStorageTenantScopeTest {
    @Test
    void rejectsMissingAndMismatchedScopesBeforeAccessingObjects() {
        var jdbc = mock(JdbcTemplate.class);
        var store = new JdbcStorageMetadataStore(jdbc);
        clearInvocations(jdbc);
        TenantScope.clear();
        try {
            assertThrows(IllegalStateException.class, () -> store.markObjectDeleted(1L, "key"));
            TenantScope.set(new TenantId(2));
            assertThrows(IllegalArgumentException.class, () -> store.markObjectDeleted(1L, "key"));
            verifyNoInteractions(jdbc);
        } finally {
            TenantScope.clear();
        }
    }
}
