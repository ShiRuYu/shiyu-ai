package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.record.domain.model.TimelineEventBO;
import com.shiyu.ai.record.port.repository.TimelineEventRepository;
import com.shiyu.ai.record.request.TimelineEventRequest;
import com.shiyu.ai.record.vo.TimelineEventVO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class TimelineEventServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(6), new UserId(11), false);
    private final TimelineEventRepository repository = mock(TimelineEventRepository.class); private final TimelineEventServiceImpl service = new TimelineEventServiceImpl(repository);

    @Test
    void supportsTimelineConversionAndTenantScopedCrud() {
        TimelineEventBO event = new TimelineEventBO(); TimelineEventRequest request = new TimelineEventRequest(); request.setProfileId(7L); request.setTitle("exam"); request.setEventType("study"); request.setEventDate(new Date()); TimelineEventVO view = mock(TimelineEventVO.class);
        when(repository.selectPage(ACTOR.tenantId(), 1, 20, 7L)).thenReturn(Pair.of(1L, List.of(event))); when(repository.selectByIdWithDetails(ACTOR.tenantId(), 1L)).thenReturn(event).thenReturn(event).thenReturn(null); when(repository.selectByProfileId(ACTOR.tenantId(), 7L)).thenReturn(List.of(event));
        when(repository.insert(eq(ACTOR.tenantId()), any(TimelineEventBO.class))).thenReturn(event); when(repository.update(ACTOR.tenantId(), event)).thenReturn(true); when(repository.deleteById(ACTOR.tenantId(), 1L)).thenReturn(true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TimelineEventVO.class))).thenReturn(List.of(view)); mapper.when(() -> MapstructUtils.convert(any(TimelineEventBO.class), eq(TimelineEventVO.class))).thenReturn(view);
            assertEquals(1, service.pageView(ACTOR, null, null, 7L).getRight().size()); assertSame(view, service.detailView(ACTOR, 1L)); assertSame(view, service.create(ACTOR, request)); assertEquals(1, service.timelineView(ACTOR, 7L).size());
        }
        assertTrue(service.update(ACTOR, 1L, request)); assertFalse(service.update(ACTOR, 1L, request)); assertTrue(service.delete(ACTOR, 1L));
    }

    @Test
    void coversExplicitPaginationAndEventTimePrecedence() {
        TimelineEventBO event = new TimelineEventBO();
        TimelineEventRequest request = new TimelineEventRequest();
        request.setProfileId(7L); request.setTitle("exam"); request.setEventType("study"); request.setEventTime(new Date());
        when(repository.selectPage(ACTOR.tenantId(), 2, 3, null)).thenReturn(Pair.of(0L, List.of()));
        when(repository.selectByIdWithDetails(ACTOR.tenantId(), 2L)).thenReturn(event);
        when(repository.update(ACTOR.tenantId(), event)).thenReturn(true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TimelineEventVO.class))).thenReturn(List.of());
            assertEquals(0L, service.pageView(ACTOR, 2, 3, null).getLeft());
        }
        assertTrue(service.update(ACTOR, 2L, request));
    }
}
