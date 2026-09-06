package com.shiyu.ai.kernel.context;

import com.shiyu.ai.kernel.error.DomainAccessDeniedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActorContextTest {

    @Test
    void tenantAndUserIdsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new TenantId(0));
        assertThrows(IllegalArgumentException.class, () -> new TenantId(-1));
        assertThrows(IllegalArgumentException.class, () -> new UserId(0));
        assertThrows(IllegalArgumentException.class, () -> new UserId(-1));
        assertThrows(IllegalArgumentException.class, () -> new RoleId(0));
        assertThrows(IllegalArgumentException.class, () -> new RoleId(-1));
    }

    @Test
    void actorCanCarryTheSelectedRoleAcrossAsyncBoundaries() {
        ActorContext actor = new ActorContext(
                new TenantId(3), new UserId(7), new RoleId(11), false);

        assertEquals(11L, actor.activeRoleId().value());
        assertDoesNotThrow(() -> serialize(actor));
    }

    private byte[] serialize(ActorContext actor) {
        try {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            try (java.io.ObjectOutputStream output = new java.io.ObjectOutputStream(bytes)) {
                output.writeObject(actor);
            }
            return bytes.toByteArray();
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void actorContextRequiresTenantAndUser() {
        UserId userId = new UserId(7);
        TenantId tenantId = new TenantId(3);

        assertThrows(NullPointerException.class, () -> new ActorContext(null, userId, false));
        assertThrows(NullPointerException.class, () -> new ActorContext(tenantId, null, false));
    }

    @Test
    void actorCanOnlyAccessItsOwnTenant() {
        ActorContext actor = new ActorContext(new TenantId(3), new UserId(7), false);

        assertDoesNotThrow(() -> actor.requireTenant(new TenantId(3)));
        DomainAccessDeniedException error = assertThrows(
                DomainAccessDeniedException.class,
                () -> actor.requireTenant(new TenantId(4))
        );

        assertEquals("TENANT_MISMATCH", error.code());
    }
}
