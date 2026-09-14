package com.shiyu.ai.common.mybatis.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantConsistencyListenerTest {

    private final TenantConsistencyListener listener = new TenantConsistencyListener();

    @AfterEach
    void clearScope() {
        TenantScope.clear();
    }

    @Test
    void rejectsTenantOwnedWritesWithoutScope() {
        TenantEntity entity = new TenantEntity();
        entity.setTenantId(7L);

        assertThrows(IllegalStateException.class, () -> listener.onInsert(entity));
    }

    @Test
    void rejectsAnEntityFromAnotherTenant() {
        TenantScope.set(new TenantId(7L));
        TenantEntity entity = new TenantEntity();
        entity.setTenantId(9L);

        assertThrows(IllegalArgumentException.class, () -> listener.onInsert(entity));
    }

    @Test
    void acceptsAnEntityInTheCurrentTenant() {
        TenantScope.set(new TenantId(7L));
        TenantEntity entity = new TenantEntity();
        entity.setTenantId(7L);

        assertDoesNotThrow(() -> listener.onInsert(entity));
        assertDoesNotThrow(() -> listener.onUpdate(entity));
    }

    @Test
    void acceptsFlexFilledInsertsAndPartialUpdatesWithinTheCurrentScope() {
        TenantScope.set(new TenantId(7L));
        TenantEntity entity = new TenantEntity();

        assertDoesNotThrow(() -> listener.onInsert(entity));
        assertDoesNotThrow(() -> listener.onUpdate(entity));
    }
}
