package com.shiyu.ai.kernel.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantScopeTest {

    @AfterEach
    void clearScope() {
        TenantScope.clear();
    }

    @Test
    void requiresAnExplicitPositiveTenant() {
        assertThrows(IllegalStateException.class, TenantScope::require);
        assertThrows(IllegalArgumentException.class, () -> TenantScope.set(null));
        assertThrows(IllegalArgumentException.class, () -> new TenantId(0L));
    }

    @Test
    void restoresThePreviousScopeAfterACommand() {
        TenantScope.set(new TenantId(7L));

        String result = TenantScope.withTenant(new TenantId(9L), () -> {
            assertEquals(9L, TenantScope.require().value());
            return "done";
        });

        assertEquals("done", result);
        assertEquals(7L, TenantScope.require().value());
    }

    @Test
    void clearsTheScopeAfterAnAnonymousRequest() {
        TenantScope.set(new TenantId(7L));
        TenantScope.clear();

        assertTrue(TenantScope.current().isEmpty());
    }
}
