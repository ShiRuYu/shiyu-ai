package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.record.domain.model.MediaBO;
import com.shiyu.ai.record.port.repository.MediaRepository;
import com.shiyu.ai.record.request.MediaRequest;
import com.shiyu.ai.record.vo.MediaVO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class MediaServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(3), new UserId(8), false);
    private final MediaRepository repository = mock(MediaRepository.class);
    private final MediaServiceImpl service = new MediaServiceImpl(repository);

    @Test
    void supportsTenantScopedCrudAndDefaults() {
        MediaBO media = new MediaBO();
        MediaRequest request = new MediaRequest(); request.setRecordId(11L); request.setUrl("https://media"); request.setMediaType("image");
        MediaVO view = mock(MediaVO.class);
        when(repository.selectPage(ACTOR.tenantId(), 1, 10, 11L)).thenReturn(Pair.of(1L, List.of(media)));
        when(repository.selectById(ACTOR.tenantId(), 2L)).thenReturn(media).thenReturn(media).thenReturn(null);
        when(repository.insert(eq(ACTOR.tenantId()), any(MediaBO.class))).thenReturn(media);
        when(repository.update(ACTOR.tenantId(), media)).thenReturn(true);
        when(repository.deleteById(ACTOR.tenantId(), 2L)).thenReturn(true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(MediaVO.class))).thenReturn(List.of(view));
            mapper.when(() -> MapstructUtils.convert(any(MediaBO.class), eq(MediaVO.class))).thenReturn(view);
            assertEquals(1, service.pageView(ACTOR, null, null, 11L).getRight().size());
            assertSame(view, service.detailView(ACTOR, 2L));
            assertSame(view, service.create(ACTOR, request));
        }
        assertTrue(service.update(ACTOR, 2L, request));
        assertFalse(service.update(ACTOR, 2L, request));
        assertTrue(service.delete(ACTOR, 2L));
        assertThrows(NullPointerException.class, () -> service.delete(null, 2L));
        verify(repository, atLeastOnce()).selectPage(ACTOR.tenantId(), 1, 10, 11L);
    }

    @Test
    void coversExplicitPaginationAndNoRecordFilter() {
        when(repository.selectPage(ACTOR.tenantId(), 2, 3, null)).thenReturn(Pair.of(0L, List.of()));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(MediaVO.class))).thenReturn(List.of());
            assertEquals(0L, service.pageView(ACTOR, 2, 3, null).getLeft());
        }
    }
}
