package com.shiyu.ai.kernel.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

        String result =
                TenantScope.withTenant(
                        new TenantId(9L),
                        () -> {
                            assertEquals(9L, TenantScope.require().value());
                            return "done";
                        });

        assertEquals("done", result);
        assertEquals(7L, TenantScope.require().value());
    }

    @Test
    void restoresThePreviousScopeWhenTheCommandFails() {
        TenantScope.set(new TenantId(7L));

        assertThrows(
                IllegalStateException.class,
                () ->
                        TenantScope.withTenant(
                                new TenantId(9L),
                                () -> {
                                    assertEquals(9L, TenantScope.require().value());
                                    throw new IllegalStateException("boom");
                                }));

        assertEquals(7L, TenantScope.require().value());
    }

    @Test
    void clearsTheScopeAfterAnAnonymousRequest() {
        TenantScope.set(new TenantId(7L));
        TenantScope.clear();

        assertTrue(TenantScope.current().isEmpty());
    }

    @Test
    void rejectsACommandTenantThatDiffersFromTheCurrentScope() {
        TenantScope.set(new TenantId(7L));

        assertThrows(
                IllegalArgumentException.class,
                () -> TenantScope.requireMatches(new TenantId(9L)));
        TenantScope.requireMatches(new TenantId(7L));
    }

    @Test
    void validatesAnExplicitTenantWhenARequestScopeIsBound() {
        TenantScope.requireMatchesIfBound(new TenantId(7L));
        TenantScope.set(new TenantId(7L));
        TenantScope.requireMatchesIfBound(new TenantId(7L));
        assertThrows(
                IllegalArgumentException.class,
                () -> TenantScope.requireMatchesIfBound(new TenantId(8L)));
    }
}
