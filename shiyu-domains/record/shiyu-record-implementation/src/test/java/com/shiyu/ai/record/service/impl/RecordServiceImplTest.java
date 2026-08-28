package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.record.domain.model.RecordBO;
import com.shiyu.ai.record.port.repository.RecordRepository;
import com.shiyu.ai.record.request.RecordRequest;
import com.shiyu.ai.record.vo.RecordVO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class RecordServiceImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final RecordRepository repository = mock(RecordRepository.class);
    private final RecordServiceImpl service = new RecordServiceImpl(repository);

    @Test
    void pagesAndCreatesRecordsUsingTheActorTenant() {
        RecordBO record = new RecordBO();
        record.setId(1L);
        when(repository.selectPage(ACTOR.tenantId(), 1, 10, 2L)).thenReturn(Pair.of(1L, List.of(record)));
        when(repository.insert(eq(ACTOR.tenantId()), any(RecordBO.class))).thenReturn(record);
        RecordRequest request = request();
        RecordVO view = mock(RecordVO.class);

        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(RecordVO.class)))
                    .thenReturn(List.of(view));
            mapper.when(() -> MapstructUtils.convert(any(RecordBO.class), eq(RecordVO.class)))
                    .thenReturn(view);
            assertEquals(1, service.pageView(ACTOR, null, null, 2L).getRight().size());
            assertEquals(view, service.create(ACTOR, request));
        }
        verify(repository).selectPage(ACTOR.tenantId(), 1, 10, 2L);
        verify(repository).insert(eq(ACTOR.tenantId()), any(RecordBO.class));
    }

    @Test
    void updatesExistingRecordsAndReturnsFalseForMissingRecords() {
        RecordBO record = new RecordBO();
        record.setId(1L);
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(record).thenReturn(null);
        when(repository.update(ACTOR.tenantId(), record)).thenReturn(true);
        assertTrue(service.update(ACTOR, 1L, request()));
        assertEquals(100L, record.getEventId());
        assertFalse(service.update(ACTOR, 1L, request()));
        verify(repository).update(ACTOR.tenantId(), record);
    }

    @Test
    void deletesAndRejectsMissingActor() {
        when(repository.deleteById(ACTOR.tenantId(), 1L)).thenReturn(true);
        assertTrue(service.delete(ACTOR, 1L));
        assertThrows(NullPointerException.class, () -> service.delete(null, 1L));
        verify(repository).deleteById(ACTOR.tenantId(), 1L);
    }

    @Test
    void coversExplicitPaginationAndEmptyOptionalFilter() {
        when(repository.selectPage(ACTOR.tenantId(), 2, 3, null)).thenReturn(Pair.of(0L, List.of()));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(RecordVO.class))).thenReturn(List.of());
            assertEquals(0L, service.pageView(ACTOR, 2, 3, null).getLeft());
        }
    }

    private static RecordRequest request() {
        RecordRequest request = new RecordRequest();
        request.setEventId(100L);
        request.setContent("content");
        return request;
    }
}
