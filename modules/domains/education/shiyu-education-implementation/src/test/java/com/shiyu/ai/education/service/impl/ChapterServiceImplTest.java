package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.ChapterBO;
import com.shiyu.ai.education.domain.model.KnowledgeTextbookBO;
import com.shiyu.ai.education.dto.ChapterResponse;
import com.shiyu.ai.education.port.repository.ChapterRepository;
import com.shiyu.ai.education.port.repository.KnowledgeTextbookRepository;
import com.shiyu.ai.education.request.ChapterRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class ChapterServiceImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final ChapterRepository chapters = mock(ChapterRepository.class);
    private final KnowledgeTextbookRepository links = mock(KnowledgeTextbookRepository.class);
    private final ChapterServiceImpl service = new ChapterServiceImpl(chapters, links);

    @Test
    void readsAndCreatesChaptersWithActorTenant() {
        ChapterBO chapter = new ChapterBO();
        chapter.setId(1L);
        when(chapters.selectById(ACTOR.tenantId(), 1L)).thenReturn(chapter);
        when(chapters.selectByTextbookId(ACTOR.tenantId(), 2L)).thenReturn(List.of(chapter));
        when(chapters.selectRootChapters(ACTOR.tenantId(), 2L)).thenReturn(List.of(chapter));
        when(chapters.selectByParentId(ACTOR.tenantId(), 3L)).thenReturn(List.of(chapter));
        ChapterRequest request = request();

        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            ChapterResponse response = mock(ChapterResponse.class);
            mapper.when(() -> MapstructUtils.convert(any(ChapterBO.class), eq(ChapterResponse.class)))
                    .thenReturn(response);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(ChapterResponse.class)))
                    .thenReturn(List.of(response));
            assertEquals(response, service.getById(ACTOR, 1L));
            assertEquals(1, service.listByTextbookId(ACTOR, 2L).size());
            assertEquals(1, service.listRootChapters(ACTOR, 2L).size());
            assertEquals(1, service.listByParentId(ACTOR, 3L).size());
            assertEquals(response, service.create(ACTOR, request));
        }
        verify(chapters).insert(eq(ACTOR.tenantId()), any(ChapterBO.class));
    }

    @Test
    void updatesAndDeletesOnlyLeafChapters() {
        ChapterBO chapter = new ChapterBO();
        chapter.setId(1L);
        when(chapters.selectById(ACTOR.tenantId(), 1L)).thenReturn(chapter).thenReturn(null);
        when(chapters.selectByParentId(ACTOR.tenantId(), 1L)).thenReturn(List.of()).thenReturn(List.of(new ChapterBO()));
        service.update(ACTOR, 1L, request());
        verify(chapters).update(eq(ACTOR.tenantId()), eq(chapter));
        service.delete(ACTOR, 1L);
        verify(links).deleteByChapterId(ACTOR.tenantId(), 1L);
        assertThrows(ServiceException.class, () -> service.delete(ACTOR, 1L));
        assertThrows(ServiceException.class, () -> service.update(ACTOR, 1L, request()));
    }

    @Test
    void replacesKnowledgeLinksWithDistinctNonNullIds() {
        ChapterBO chapter = new ChapterBO();
        chapter.setTextbookId(2L);
        when(chapters.selectById(ACTOR.tenantId(), 1L)).thenReturn(chapter);
        KnowledgeTextbookBO first = new KnowledgeTextbookBO();
        first.setKnowledgeId(4L);
        KnowledgeTextbookBO duplicate = new KnowledgeTextbookBO();
        duplicate.setKnowledgeId(4L);
        KnowledgeTextbookBO missing = new KnowledgeTextbookBO();
        when(links.selectByChapterId(ACTOR.tenantId(), 1L)).thenReturn(List.of(first, duplicate, missing));
        assertEquals(List.of(4L), service.listKnowledgeIds(ACTOR, 1L));

        service.replaceKnowledgeIds(ACTOR, 1L, java.util.Arrays.asList(4L, null, 4L, 5L));
        verify(links).deleteByChapterId(ACTOR.tenantId(), 1L);
        verify(links, org.mockito.Mockito.times(2)).insert(eq(ACTOR.tenantId()), any(KnowledgeTextbookBO.class));
        service.replaceKnowledgeIds(ACTOR, 1L, null);
    }

    private static ChapterRequest request() {
        ChapterRequest request = new ChapterRequest();
        request.setTextbookId(2L);
        request.setName("chapter");
        return request;
    }
}
