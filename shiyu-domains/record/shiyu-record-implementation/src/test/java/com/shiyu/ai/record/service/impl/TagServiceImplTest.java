package com.shiyu.ai.record.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.record.domain.model.TagBO;
import com.shiyu.ai.record.port.repository.TagRepository;
import com.shiyu.ai.record.request.TagRequest;
import com.shiyu.ai.record.vo.TagVO;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class TagServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(5), new UserId(10), false);
    private final TagRepository repository = mock(TagRepository.class); private final TagServiceImpl service = new TagServiceImpl(repository);

    @Test
    void supportsTagViewsAndCrudWithActorTenant() {
        TagBO tag = new TagBO(); TagRequest request = new TagRequest(); request.setName("math"); TagVO view = mock(TagVO.class);
        when(repository.selectPage(ACTOR.tenantId(), 1, 10, "m")).thenReturn(Pair.of(1L, List.of(tag))); when(repository.selectAll(ACTOR.tenantId())).thenReturn(List.of(tag));
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(tag).thenReturn(tag).thenReturn(null); when(repository.insert(eq(ACTOR.tenantId()), any(TagBO.class))).thenReturn(tag);
        when(repository.update(ACTOR.tenantId(), tag)).thenReturn(true); when(repository.deleteById(ACTOR.tenantId(), 1L)).thenReturn(true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TagVO.class))).thenReturn(List.of(view)); mapper.when(() -> MapstructUtils.convert(any(TagBO.class), eq(TagVO.class))).thenReturn(view);
            assertEquals(1, service.pageView(ACTOR, null, null, "m").getRight().size()); assertEquals(1, service.allView(ACTOR).size()); assertSame(view, service.detailView(ACTOR, 1L)); assertSame(view, service.create(ACTOR, request));
        }
        assertTrue(service.update(ACTOR, 1L, request)); assertFalse(service.update(ACTOR, 1L, request)); assertTrue(service.delete(ACTOR, 1L)); assertThrows(NullPointerException.class, () -> service.allView(null));
    }

    @Test
    void coversExplicitPaginationAndBlankFilter() {
        when(repository.selectPage(ACTOR.tenantId(), 2, 3, null)).thenReturn(Pair.of(0L, List.of()));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TagVO.class))).thenReturn(List.of());
            assertEquals(0L, service.pageView(ACTOR, 2, 3, null).getLeft());
        }
    }
}
