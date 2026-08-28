package com.shiyu.ai.web.auth;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class ActorContextHttpAdapterTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.clearContext();
    }

    @Test
    void rejectsMissingAuthenticationContext() {
        assertThrows(ServiceException.class, ActorContextHttpAdapter::currentActor);
    }

    @Test
    void optionalTelemetryAdapterDoesNotTurnAnonymousRequestsIntoFailures() {
        assertNull(ActorContextHttpAdapter.currentActorOrNull());
    }

    @Test
    void copiesTenantUserAndRoleIntoStronglyTypedContext() {
        UserContext source = new UserContext();
        source.setCurrentTenantId(11L);
        source.setUserId(12L);
        source.setCurrentRoleId(13L);
        source.setCurrentRoleCode("member");
        source.setHomeTenantId(10L);
        source.setSwitchMode("PARENT_SUPER_ADMIN");
        UserContextHolder.setContext(source);

        var actor = ActorContextHttpAdapter.currentActor();

        assertEquals(11L, actor.tenantId().value());
        assertEquals(12L, actor.userId().value());
        assertEquals(13L, actor.activeRoleId().value());
        assertEquals("member", actor.activeRoleCode());
        assertEquals(10L, actor.homeTenantId().value());
        assertEquals("PARENT_SUPER_ADMIN", actor.switchMode());
        assertTrue(actor.parentSuperAdminSwitch());
        assertFalse(actor.platformAdmin());
    }
}
