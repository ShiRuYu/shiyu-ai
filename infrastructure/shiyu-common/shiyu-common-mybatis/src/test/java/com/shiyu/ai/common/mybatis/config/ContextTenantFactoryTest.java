package com.shiyu.ai.common.mybatis.config;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextTenantFactoryTest {

    private final ContextTenantFactory factory = new ContextTenantFactory();

    @AfterEach
    void clearContext() {
        TenantScope.clear();
    }

    @Test
    void rejectsTenantOwnedQueriesWithoutTenantContext() {
        assertThrows(IllegalStateException.class, factory::getTenantIds);

        assertThrows(IllegalStateException.class, factory::getTenantIds);
    }

    @Test
    void suppliesExactlyTheCurrentTenant() {
        TenantScope.set(new TenantId(9L));

        assertArrayEquals(new Object[]{9L}, factory.getTenantIds());
    }

    @Test
    void authInfrastructureTablesStillRequireContext() {
        assertThrows(IllegalStateException.class, () -> factory.getTenantIds("auth_user"));

        TenantScope.set(new TenantId(9L));

        assertArrayEquals(new Object[]{9L}, factory.getTenantIds("auth_user"));
    }
}
