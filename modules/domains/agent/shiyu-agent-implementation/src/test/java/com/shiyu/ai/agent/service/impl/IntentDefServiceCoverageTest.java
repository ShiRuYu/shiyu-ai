package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.agent.port.repository.IntentDefRepository;
import com.shiyu.ai.agent.request.IntentDefRequest;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class IntentDefServiceCoverageTest {
    @Test
    void mapsCrudViewsAndRefreshesFactoryAfterMutations() {
        IntentDefRepository repository = mock(IntentDefRepository.class);
        IntentDefServiceImpl service = new IntentDefServiceImpl();
        ReflectionTestUtils.setField(service, "intentDefRepository", repository);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        IntentDefBO bo = new IntentDefBO(); bo.setId(1L); bo.setCode("WEATHER"); bo.setAgentId("agent-1");
        IntentDefVO vo = mock(IntentDefVO.class);
        IntentDefRequest request = new IntentDefRequest();
        when(repository.selectPage(actor.tenantId(), 1, 20, "agent-1", "name", "code", "TASK"))
                .thenReturn(Pair.of(1L, List.of(bo)));
        when(repository.selectById(actor.tenantId(), 1L)).thenReturn(bo);
        when(repository.selectByAgentId(actor.tenantId(), "default")).thenReturn(List.of(bo));
        when(repository.selectAllOptions(actor.tenantId())).thenReturn(List.of());
        when(repository.create(actor.tenantId(), bo)).thenReturn(bo);
        when(repository.update(actor.tenantId(), bo)).thenReturn(bo);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(IntentDefVO.class))).thenReturn(List.of(vo));
            mapper.when(() -> MapstructUtils.convert(bo, IntentDefVO.class)).thenReturn(vo);
            mapper.when(() -> MapstructUtils.convert(request, IntentDefBO.class)).thenReturn(bo);
            mapper.when(() -> MapstructUtils.convert(bo, IntentDefBO.class)).thenReturn(bo);
            service.pageView(actor, 1, 20, "agent-1", "name", "code", "TASK");
            service.detailView(actor, 1L);
            service.create(actor, request);
            service.update(actor, 1L, request);
        }
        service.listAllOptions(actor);
        service.deleteById(actor, 1L);
        when(repository.selectById(actor.tenantId(), 2L)).thenReturn(null);
        service.deleteById(actor, 2L);
        service.deleteByIds(actor, List.of(1L, 2L));
        verify(repository, times(2)).deleteById(actor.tenantId(), 1L);
    }

    @Test
    void rejectsMissingActorForEveryPublicOperation() {
        IntentDefServiceImpl service = new IntentDefServiceImpl();
        ReflectionTestUtils.setField(service, "intentDefRepository", mock(IntentDefRepository.class));
        IntentDefRequest request = new IntentDefRequest();
        assertThrows(RuntimeException.class, () -> service.pageView(null, 1, 10, null, null, null, null));
        assertThrows(RuntimeException.class, () -> service.detailView(null, 1L));
        assertThrows(RuntimeException.class, () -> service.create(null, request));
        assertThrows(RuntimeException.class, () -> service.update(null, 1L, request));
        assertThrows(RuntimeException.class, () -> service.deleteById(null, 1L));
        assertThrows(RuntimeException.class, () -> service.deleteByIds(null, List.of(1L)));
        assertThrows(RuntimeException.class, () -> service.listAllOptions(null));
    }
}
