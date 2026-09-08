package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.port.repository.UserRepository;
import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TimezoneServiceImplCoverageTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final TimezoneServiceImpl service = new TimezoneServiceImpl(repository);
    private final ActorContext actor = new ActorContext(new TenantId(3L), new UserId(8L), false);

    @Test
    void readsConfiguredAndFallbackTimezoneValues() {
        assertFalse(service.getTimezoneOptions().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.getTimezone(null));
        when(repository.selectById(8L)).thenReturn(null);
        assertEquals("Asia/Shanghai", service.getTimezone(actor));
        UserBO user = new UserBO(); user.setExtInfo(null);
        when(repository.selectById(8L)).thenReturn(user);
        assertEquals("Asia/Shanghai", service.getTimezone(actor));
        user.setExtInfo("{}");
        assertEquals("Asia/Shanghai", service.getTimezone(actor));
        user.setExtInfo("{\"timezone\":\"Europe/London\"}");
        assertEquals("Europe/London", service.getTimezone(actor));
    }

    @Test
    void validatesAndPersistsTimezoneWhileRetainingExtensionFields() {
        assertFalse(service.setTimezone(actor, null));
        var invalid = new SetTimezoneRequest(); invalid.setTimezone("Mars/Phobos");
        assertFalse(service.setTimezone(actor, invalid));
        assertThrows(IllegalArgumentException.class, () -> service.setTimezone(null, validRequest("Europe/London")));
        when(repository.selectById(8L)).thenReturn(null);
        assertFalse(service.setTimezone(actor, validRequest("UTC")));
        UserBO user = new UserBO(); user.setExtInfo("{\"theme\":\"dark\"}");
        when(repository.selectById(8L)).thenReturn(user);
        when(repository.update(any(UserBO.class))).thenReturn(true);
        assertTrue(service.setTimezone(actor, validRequest("Europe/London")));
        assertTrue(user.getExtInfo().contains("theme"));
        user.setExtInfo("not-json");
        assertThrows(RuntimeException.class, () -> service.setTimezone(actor, validRequest("Asia/Shanghai")));
    }

    private static SetTimezoneRequest validRequest(String timezone) {
        var request = new SetTimezoneRequest(); request.setTimezone(timezone); return request;
    }
}
