package com.shiyu.ai.knowledge.retrieval;

import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmbeddedKnowledgeRetrievalServiceTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7L), new UserId(9L), false);

    @Test
    void validatesSecurityContextAndRetrievesDeduplicatedDocumentsAndEntries() {
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService.SpaceView space = new KnowledgeSpaceService.SpaceView(10L, "s", "d", "Space", "", "PUBLIC", "", "", null, "", "", "", 200, 20, null, 1, null, null);
        when(spaces.accessibleSpaces(ACTOR)).thenReturn(List.of(space));
        KnowledgeBO entry = new KnowledgeBO(); entry.setId(42L); entry.setSpaceId(10L); entry.setName("Math"); entry.setDescription("math entry");
        when(knowledge.findBySpace(ACTOR.tenantId(), 10L)).thenReturn(List.of(entry));
        KnowledgeChunkBO chunk = new KnowledgeChunkBO(); chunk.setId(11L); chunk.setVersionId(12L); chunk.setMetadata("{\"knowledgeId\":42}"); chunk.setPageNumber(3); chunk.setSectionPath("§1");
        when(chunks.getById(ACTOR.tenantId(), 11L)).thenReturn(chunk);
        KnowledgeDocumentBO document = new KnowledgeDocumentBO(); document.setId(13L); document.setTitle("Math document");
        when(documents.selectById(ACTOR.tenantId(), 13L)).thenReturn(document);
        String longContent = "x".repeat(301);
        when(index.hybridSearch(eq(ACTOR), eq(10L), eq("math"), anyString(), anyInt(), anyDouble(), anyBoolean()))
                .thenReturn(List.of(new KnowledgeIndexService.HybridHit(11L, 13L, longContent, "math", 0.3, 0.4, 0.8, 0.0),
                        new KnowledgeIndexService.HybridHit(11L, 13L, "duplicate", "", 0.9, 0.1, 0.2, 0.0)));
        EmbeddedKnowledgeRetrievalService service = new EmbeddedKnowledgeRetrievalService(spaces, index, knowledge, chunks, documents);

        KnowledgeRetrievalResult missingContext = service.retrieve(null);
        assertFalse(missingContext.success());
        assertFalse(service.retrieve(new KnowledgeRetrievalRequest(null, List.of(), Set.of(), null, "math", null, null, null, null)).success());
        assertFalse(service.retrieve(new KnowledgeRetrievalRequest(ACTOR, null, Set.of(), null, null, null, null, null, null)).success());
        assertFalse(service.retrieve(new KnowledgeRetrievalRequest(ACTOR, List.of(), Set.of(), null, "  ", null, null, null, null)).success());

        KnowledgeRetrievalResult result = service.retrieve(new KnowledgeRetrievalRequest(ACTOR, List.of(), Set.of(), RetrievalMode.HYBRID, "math", 10, 5, 0D, false));
        assertTrue(result.success());
        assertEquals(2, result.hits().size());
        assertEquals(2, result.citations().size());
        assertEquals(300, result.citations().get(1).excerpt().length());
        assertTrue(result.context().contains("[c1]"));
        verify(index).hybridSearch(eq(ACTOR), eq(10L), eq("math"), eq("HYBRID"), eq(10), eq(0D), eq(false));
    }

    @Test
    void handlesSelectedSpacesEmptySourcesMissingChunkAndInvalidMetadata() {
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService.SpaceView space = new KnowledgeSpaceService.SpaceView(10L, "s", "d", "Space", "", "PUBLIC", "", "", null, "", "", "", 200, 20, null, 1, null, null);
        KnowledgeSpaceService.SpaceView otherSpace = new KnowledgeSpaceService.SpaceView(99L, "s99", "d", "Other", "", "PUBLIC", "", "", null, "", "", "", 200, 20, null, 1, null, null);
        when(spaces.accessibleSpaces(ACTOR)).thenReturn(List.of(space));
        doNothing().when(spaces).requireAccess(anyLong(), any(KnowledgeSpaceService.SpaceRole.class), eq(ACTOR));
        when(index.hybridSearch(any(ActorContext.class), anyLong(), anyString(), anyString(), anyInt(), anyDouble(), anyBoolean()))
                .thenReturn(List.of(new KnowledgeIndexService.HybridHit(99L, 13L, null, null, 0D, 0D, 0D, 0.4D)));
        when(chunks.getById(ACTOR.tenantId(), 99L)).thenReturn(null);
        KnowledgeBO entry = new KnowledgeBO(); entry.setId(2L); entry.setSpaceId(10L); entry.setName("other"); entry.setDescription(null);
        when(knowledge.findBySpace(ACTOR.tenantId(), 10L)).thenReturn(List.of(entry));
        EmbeddedKnowledgeRetrievalService service = new EmbeddedKnowledgeRetrievalService(spaces, index, knowledge, chunks, documents);

        assertThrows(ServiceException.class, () -> service.retrieve(new KnowledgeRetrievalRequest(ACTOR, List.of(99L), Set.of(KnowledgeSourceType.DOCUMENT), RetrievalMode.VECTOR, "q", 1, 1, 0D, true)));
        verify(spaces).requireAccess(99L, KnowledgeSpaceService.SpaceRole.VIEWER, ACTOR);
        when(spaces.accessibleSpaces(ACTOR)).thenReturn(List.of(otherSpace));
        KnowledgeRetrievalResult missingChunk = service.retrieve(new KnowledgeRetrievalRequest(ACTOR, List.of(99L), Set.of(KnowledgeSourceType.DOCUMENT), RetrievalMode.VECTOR, "q", 1, 1, 0D, true));
        assertTrue(missingChunk.success());
        assertNull(missingChunk.hits().get(0).knowledgeId());
        when(spaces.accessibleSpaces(ACTOR)).thenReturn(List.of());
        assertTrue(service.retrieve(new KnowledgeRetrievalRequest(ACTOR, List.of(), Set.of(), null, "q", 1, 1, null, null)).hits().isEmpty());

        when(spaces.accessibleSpaces(ACTOR)).thenReturn(List.of(space));
        KnowledgeRetrievalResult entries = service.retrieve(new KnowledgeRetrievalRequest(ACTOR, List.of(), Set.of(KnowledgeSourceType.KNOWLEDGE_ENTRY), null, "other", 1, 0, null, null));
        assertTrue(entries.success());
        assertEquals(1, entries.hits().size());
        assertEquals("other", entries.hits().get(0).content());
    }

    @Test
    void mapsDocumentFallbacksAndRanksMixedSourcesForExplicitSpaces() {
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService.SpaceView space = new KnowledgeSpaceService.SpaceView(10L, "s", "d", "Space", "", "PUBLIC", "", "", null, "", "", "", 200, 20, null, 1, null, null);
        when(spaces.accessibleSpaces(ACTOR)).thenReturn(List.of(space));
        doNothing().when(spaces).requireAccess(10L, KnowledgeSpaceService.SpaceRole.VIEWER, ACTOR);
        KnowledgeBO entry = new KnowledgeBO();
        entry.setId(42L); entry.setSpaceId(10L); entry.setName("Math"); entry.setDescription(null);
        when(knowledge.findBySpace(ACTOR.tenantId(), 10L)).thenReturn(List.of(entry));
        when(index.hybridSearch(eq(ACTOR), eq(10L), eq("math"), anyString(), anyInt(), anyDouble(), anyBoolean()))
                .thenReturn(List.of(new KnowledgeIndexService.HybridHit(11L, 13L, null, null,
                        0.8D, 0.1D, 0D, 0.9D)));
        when(chunks.getById(ACTOR.tenantId(), 11L)).thenReturn(null);
        when(documents.selectById(ACTOR.tenantId(), 13L)).thenReturn(null);

        EmbeddedKnowledgeRetrievalService service = new EmbeddedKnowledgeRetrievalService(
                spaces, index, knowledge, chunks, documents);
        KnowledgeRetrievalResult result = service.retrieve(new KnowledgeRetrievalRequest(
                ACTOR, List.of(10L, 10L), Set.of(KnowledgeSourceType.DOCUMENT,
                KnowledgeSourceType.KNOWLEDGE_ENTRY), RetrievalMode.KEYWORD, "math", 0, 1000, null, null));

        assertTrue(result.success());
        assertEquals(2, result.hits().size());
        assertEquals("Math", result.hits().get(0).title());
        assertEquals("Math", result.hits().get(0).content());
        assertEquals("文档 13", result.hits().get(1).title());
        assertTrue(result.context().contains("[c1]"));
        verify(spaces, times(1)).requireAccess(10L, KnowledgeSpaceService.SpaceRole.VIEWER, ACTOR);
    }

    @Test
    void coversRetrievalScoringMetadataAndTextHelperEdges() throws Exception {
        EmbeddedKnowledgeRetrievalService service = new EmbeddedKnowledgeRetrievalService(
                mock(KnowledgeSpaceService.class), mock(KnowledgeIndexService.class),
                mock(KnowledgeRepository.class), mock(KnowledgeChunkRepository.class),
                mock(KnowledgeDocumentRepository.class));
        Method knowledgeId = EmbeddedKnowledgeRetrievalService.class.getDeclaredMethod("knowledgeId", KnowledgeChunkBO.class);
        knowledgeId.setAccessible(true);
        KnowledgeChunkBO withoutMetadata = new KnowledgeChunkBO();
        assertNull(knowledgeId.invoke(service, new Object[]{null}));
        assertNull(knowledgeId.invoke(service, withoutMetadata));
        withoutMetadata.setMetadata("not-json");
        assertNull(knowledgeId.invoke(service, withoutMetadata));
        KnowledgeChunkBO overflowingMetadata = new KnowledgeChunkBO();
        overflowingMetadata.setMetadata("{\"knowledgeId\":\"999999999999999999999999999999\"}");
        assertNull(knowledgeId.invoke(service, overflowingMetadata));

        Method rank = EmbeddedKnowledgeRetrievalService.class.getDeclaredMethod("rankScore", KnowledgeRetrievalHit.class);
        rank.setAccessible(true);
        assertEquals(0.9D, rank.invoke(service, new KnowledgeRetrievalHit(10L, 1L, null, null, null,
                "title", "content", null, null, null, 0.1D, 0.2D, 0.3D, 0.9D)));
        assertEquals(0.3D, rank.invoke(service, new KnowledgeRetrievalHit(10L, 1L, null, null, null,
                "title", "content", null, null, null, 0.1D, 0.2D, 0.3D, 0D)));
        assertEquals(0.2D, rank.invoke(service, new KnowledgeRetrievalHit(10L, 1L, null, null, null,
                "title", "content", null, null, null, 0.1D, 0.2D, 0D, 0D)));

        Method contains = EmbeddedKnowledgeRetrievalService.class.getDeclaredMethod("contains", String.class, String.class);
        contains.setAccessible(true);
        assertFalse((Boolean) contains.invoke(service, new Object[]{null, "q"}));
        assertTrue((Boolean) contains.invoke(service, "Math", "mat"));
        assertFalse((Boolean) contains.invoke(service, "Math", "bio"));
    }

    @Test
    void filtersKnowledgeEntriesByNameOrDescriptionAndUsesFallbackDocumentTitle() {
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeSpaceService.SpaceView space = new KnowledgeSpaceService.SpaceView(
                10L, "s", "d", "Space", "", "PUBLIC", "", "", null, "", "", "", 200, 20, null, 1, null, null);
        when(spaces.accessibleSpaces(ACTOR)).thenReturn(List.of(space));
        KnowledgeBO byDescription = new KnowledgeBO();
        byDescription.setId(1L); byDescription.setSpaceId(10L); byDescription.setName("unrelated");
        byDescription.setDescription("Physics reference");
        KnowledgeBO noMatch = new KnowledgeBO();
        noMatch.setId(2L); noMatch.setSpaceId(10L); noMatch.setName("history"); noMatch.setDescription(null);
        KnowledgeBO nullTitle = new KnowledgeBO();
        nullTitle.setId(3L); nullTitle.setSpaceId(10L); nullTitle.setName(null); nullTitle.setDescription("Physics notes");
        when(knowledge.findBySpace(ACTOR.tenantId(), 10L)).thenReturn(List.of(byDescription, noMatch, nullTitle));
        KnowledgeDocumentBO untitled = new KnowledgeDocumentBO();
        untitled.setId(21L); untitled.setTitle(null);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        when(index.hybridSearch(any(ActorContext.class), eq(10L), eq("physics"), anyString(), anyInt(), anyDouble(), anyBoolean()))
                .thenReturn(List.of(new KnowledgeIndexService.HybridHit(7L, 21L, "chunk", "", 0.5D, 0.1D, 0.2D, 0D)));
        KnowledgeChunkBO chunk = new KnowledgeChunkBO(); chunk.setId(7L); chunk.setDocumentId(21L);
        when(chunks.getById(ACTOR.tenantId(), 7L)).thenReturn(chunk);
        when(documents.selectById(ACTOR.tenantId(), 21L)).thenReturn(untitled);
        EmbeddedKnowledgeRetrievalService service = new EmbeddedKnowledgeRetrievalService(
                spaces, index, knowledge, chunks, documents);

        KnowledgeRetrievalResult entries = service.retrieve(new KnowledgeRetrievalRequest(
                ACTOR, List.of(), Set.of(KnowledgeSourceType.KNOWLEDGE_ENTRY), RetrievalMode.KEYWORD,
                "physics", 10, 10, 0D, false));
        assertEquals(2, entries.hits().size());
        assertEquals("Physics reference", entries.hits().getFirst().content());

        KnowledgeRetrievalResult documentResult = service.retrieve(new KnowledgeRetrievalRequest(
                ACTOR, List.of(), Set.of(KnowledgeSourceType.DOCUMENT), RetrievalMode.KEYWORD,
                "physics", 10, 10, 0D, false));
        assertEquals("文档 21", documentResult.hits().getFirst().title());
    }
}
