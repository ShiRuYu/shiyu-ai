package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.enums.TimezoneEnum;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.port.repository.UserRepository;
import com.shiyu.ai.auth.request.SetTimezoneRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimezoneServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(2), new UserId(7), false);
    private final UserRepository repository = mock(UserRepository.class);
    private final TimezoneServiceImpl service = new TimezoneServiceImpl(repository);

    @Test
    void timezoneLookupRejectsNullAndUnknownValues() {
        assertNull(TimezoneEnum.getByValue(null));
        assertNull(TimezoneEnum.getByValue("Not/A_Timezone"));
        assertEquals(TimezoneEnum.ASIA_SHANGHAI, TimezoneEnum.getByValue("Asia/Shanghai"));
    }

    @Test
    void returnsDefaultAndConfiguredTimezone() {
        when(repository.selectById(7L)).thenReturn(null);
        assertEquals("Asia/Shanghai", service.getTimezone(ACTOR));
        UserBO user = new UserBO(); user.setExtInfo("{\"timezone\":\"UTC\"}");
        when(repository.selectById(7L)).thenReturn(user);
        assertEquals("UTC", service.getTimezone(ACTOR));
        assertTrue(service.getTimezoneOptions().stream().anyMatch(item -> "Europe/London".equals(item.getValue())));
    }

    @Test
    void validatesAndPersistsTimezoneOnlyForExistingUser() {
        SetTimezoneRequest invalid = new SetTimezoneRequest(); invalid.setTimezone("not-a-zone");
        assertFalse(service.setTimezone(ACTOR, invalid));
        assertThrows(IllegalArgumentException.class, () -> service.getTimezone(null));
        SetTimezoneRequest request = new SetTimezoneRequest(); request.setTimezone("Europe/London");
        UserBO user = new UserBO(); user.setExtInfo("{\"theme\":\"dark\"}");
        when(repository.selectById(7L)).thenReturn(user); when(repository.update(user)).thenReturn(true);
        assertTrue(service.setTimezone(ACTOR, request));
        assertTrue(user.getExtInfo().contains("\"theme\":\"dark\""));
        assertTrue(user.getExtInfo().contains("\"timezone\":\"Europe/London\""));
        when(repository.selectById(7L)).thenReturn(null);
        assertFalse(service.setTimezone(ACTOR, request));
    }
}
