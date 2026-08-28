package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.DictBO;
import com.shiyu.ai.auth.port.repository.DictRepository;
import com.shiyu.ai.auth.request.DictRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DictServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(3), new UserId(8), false);
    private final DictRepository repository = mock(DictRepository.class);
    private final DictServiceImpl service = new DictServiceImpl(repository);

    @Test
    void pagesCachesByTypeAndUsesTenantForCrud() {
        DictBO dict = new DictBO(); dict.setId(4L); dict.setTenantId(3L); dict.setDictType("level"); dict.setDictLabel("High"); dict.setDictValue("3");
        when(repository.selectPage(ACTOR.tenantId(), 1, 10)).thenReturn(Pair.of(1L, List.of(dict)));
        when(repository.selectByDictType(ACTOR.tenantId(), "level")).thenReturn(List.of(dict));
        when(repository.selectById(ACTOR.tenantId(), 4L)).thenReturn(dict);
        when(repository.update(any(DictBO.class))).thenReturn(dict);
        DictRequest request = new DictRequest(); request.setDictType("level"); request.setDictLabel("Medium"); request.setDictValue("2");
        assertEquals(1, service.pageView(ACTOR, 1, 10).getRight().size());
        assertEquals(1, service.byTypeView(ACTOR, "level").size());
        assertEquals(1, service.byTypeView(ACTOR, "level").size());
        verify(repository, times(1)).selectByDictType(ACTOR.tenantId(), "level");
        assertNotNull(service.update(ACTOR, 4L, request));
        service.deleteById(ACTOR, 4L); service.deleteByIds(ACTOR, List.of(4L));
        verify(repository, atLeastOnce()).deleteById(ACTOR.tenantId(), 4L);
    }

    @Test
    void rejectsMissingActorAndMissingDictionary() {
        assertThrows(IllegalArgumentException.class, () -> service.pageView(null, 1, 10));
        when(repository.selectById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertNull(service.update(ACTOR, 99L, new DictRequest()));
        service.deleteById(ACTOR, 99L);
        verify(repository, never()).deleteById(ACTOR.tenantId(), 99L);
    }

    @Test
    void coversNullResultsAndTenantMismatchBranches() {
        when(repository.selectByDictType(ACTOR.tenantId(), "missing")).thenReturn(null);
        assertNull(service.byTypeView(ACTOR, "missing"));
        DictRequest request = new DictRequest();
        request.setDictType("level");
        DictBO created = new DictBO(); created.setId(8L); created.setTenantId(3L); created.setDictType("level");
        when(repository.create(any(DictBO.class))).thenReturn(created);
        assertNotNull(service.create(ACTOR, request));
        assertNull(service.update(ACTOR, null, request));
        DictBO foreign = new DictBO(); foreign.setId(10L); foreign.setTenantId(99L); foreign.setDictType("x");
        when(repository.selectById(ACTOR.tenantId(), 10L)).thenReturn(foreign);
        assertThrows(IllegalArgumentException.class, () -> service.update(ACTOR, 10L, request));
        when(repository.selectById(ACTOR.tenantId(), 11L)).thenReturn(null);
        service.deleteByIds(ACTOR, List.of(11L));
    }
}
