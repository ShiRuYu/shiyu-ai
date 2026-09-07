package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocRelationBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentRelationBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeDocumentRelationServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(10), new UserId(20), false);

    @Test
    void listsAndReplacesPointDocumentRelationsWithTenantAndSpaceChecks() {
        KnowledgeRepository points = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocRelationRepository relations = mock(KnowledgeDocRelationRepository.class);
        KnowledgeDocumentRelationRepository documentRelations = mock(KnowledgeDocumentRelationRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeBO point = point(7L, 3L);
        KnowledgeDocumentBO doc = document(11L, 3L, "doc");
        when(points.findById(ACTOR.tenantId(), 7L)).thenReturn(point);
        when(documents.selectById(ACTOR.tenantId(), 11L)).thenReturn(doc);
        KnowledgeDocRelationBO relation = pointRelation(11L, 7L, "SUPPORTS");
        when(relations.selectByKnowledgeId(ACTOR.tenantId(), 3L, 7L)).thenReturn(List.of(relation));

        KnowledgeDocumentRelationServiceImpl service = new KnowledgeDocumentRelationServiceImpl(
                points, documents, relations, documentRelations, spaces);
        assertEquals("doc", service.listDocuments(ACTOR, 7L).getFirst().title());
        service.replaceDocuments(ACTOR, 7L, Arrays.asList(11L, 11L, null), "supports");
        verify(relations).deleteByKnowledgeId(ACTOR.tenantId(), 3L, 7L);
        verify(relations).insertBatch(eq(ACTOR.tenantId()), argThat(items ->
                items.size() == 1 && "SUPPORTS".equals(items.getFirst().getRelationType())));

        when(relations.selectByDocId(ACTOR.tenantId(), 3L, 11L)).thenReturn(List.of(relation));
        assertEquals(List.of(7L), service.listPointIds(ACTOR, 11L));
        service.replacePoints(ACTOR, 11L, List.of(7L, 7L), null);
        verify(relations).deleteByDocId(ACTOR.tenantId(), 3L, 11L);
        verify(spaces, atLeastOnce()).requireAccess(eq(3L), any(), eq(ACTOR));
    }

    @Test
    void rejectsMissingActorCrossSpaceAndInvalidRelationTypes() {
        KnowledgeRepository points = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocRelationRepository relations = mock(KnowledgeDocRelationRepository.class);
        KnowledgeDocumentRelationRepository documentRelations = mock(KnowledgeDocumentRelationRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeBO point = point(7L, 3L);
        KnowledgeDocumentBO source = document(11L, 3L, "source");
        KnowledgeDocumentBO otherSpace = document(12L, 4L, "other");
        when(points.findById(ACTOR.tenantId(), 7L)).thenReturn(point);
        when(documents.selectById(ACTOR.tenantId(), 11L)).thenReturn(source);
        when(documents.selectById(ACTOR.tenantId(), 12L)).thenReturn(otherSpace);
        KnowledgeDocumentRelationServiceImpl service = new KnowledgeDocumentRelationServiceImpl(
                points, documents, relations, documentRelations, spaces);

        assertThrows(ServiceException.class, () -> service.listDocuments(null, 7L));
        assertThrows(ServiceException.class, () -> service.replaceDocuments(ACTOR, 7L, List.of(11L), "invalid"));
        assertThrows(ServiceException.class, () -> service.replaceDocumentRelations(ACTOR, 11L,
                List.of(new KnowledgeDocumentRelationService.DocumentRelationRequest(11L, "RELATED_TO"))));
        assertThrows(ServiceException.class, () -> service.replaceDocumentRelations(ACTOR, 11L,
                List.of(new KnowledgeDocumentRelationService.DocumentRelationRequest(12L, "RELATED_TO"))));
        assertThrows(ServiceException.class, () -> service.replaceDocumentRelations(ACTOR, 11L,
                List.of(new KnowledgeDocumentRelationService.DocumentRelationRequest(12L, "INVALID"))));

        when(documents.selectById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.listPointIds(ACTOR, 99L));
        verifyNoInteractions(documentRelations);
    }

    @Test
    void listsAndReplacesDocumentRelationsAndRemovesBothRelationKinds() {
        KnowledgeRepository points = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocRelationRepository relations = mock(KnowledgeDocRelationRepository.class);
        KnowledgeDocumentRelationRepository documentRelations = mock(KnowledgeDocumentRelationRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeDocumentBO source = document(11L, 3L, "source");
        KnowledgeDocumentBO target = document(12L, 3L, "target");
        when(documents.selectById(ACTOR.tenantId(), 11L)).thenReturn(source);
        when(documents.selectById(ACTOR.tenantId(), 12L)).thenReturn(target);
        KnowledgeDocumentRelationBO record = new KnowledgeDocumentRelationBO();
        record.setId(9L); record.setTargetDocumentId(12L); record.setSourceDocumentId(11L); record.setRelationType("REFERENCES");
        when(documentRelations.selectBySource(ACTOR.tenantId(), 3L, 11L)).thenReturn(List.of(record));
        KnowledgeDocumentRelationServiceImpl service = new KnowledgeDocumentRelationServiceImpl(
                points, documents, relations, documentRelations, spaces);

        assertEquals("target", service.listDocumentRelations(ACTOR, 11L).getFirst().targetTitle());
        service.replaceDocumentRelations(ACTOR, 11L, List.of(
                new KnowledgeDocumentRelationService.DocumentRelationRequest(12L, null)));
        verify(documentRelations).replace(eq(ACTOR.tenantId()), eq(3L), eq(11L), argThat(items ->
                items.size() == 1 && "RELATED_TO".equals(items.getFirst().getRelationType())));
        service.removeDocumentRelations(ACTOR, 11L);
        verify(relations).deleteByDocId(ACTOR.tenantId(), 3L, 11L);
        verify(documentRelations).deleteByDocument(ACTOR.tenantId(), 11L);
    }

    @Test
    void handlesEmptyListsNullSpacesAndFiltersForeignRelationRows() {
        KnowledgeRepository points = mock(KnowledgeRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeDocRelationRepository relations = mock(KnowledgeDocRelationRepository.class);
        KnowledgeDocumentRelationRepository documentRelations = mock(KnowledgeDocumentRelationRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeBO point = point(7L, 3L);
        KnowledgeDocumentBO matching = document(11L, 3L, "matching");
        KnowledgeDocumentBO foreign = document(12L, 4L, "foreign");
        when(points.findById(ACTOR.tenantId(), 7L)).thenReturn(point);
        when(documents.selectById(ACTOR.tenantId(), 11L)).thenReturn(matching);
        when(documents.selectById(ACTOR.tenantId(), 12L)).thenReturn(foreign);
        KnowledgeDocRelationBO nullRelation = pointRelation(null, 7L, "RELATED");
        KnowledgeDocRelationBO foreignRelation = pointRelation(12L, 7L, "RELATED");
        KnowledgeDocRelationBO matchingRelation = pointRelation(11L, 7L, "RELATED");
        when(relations.selectByKnowledgeId(ACTOR.tenantId(), 3L, 7L))
                .thenReturn(List.of(nullRelation, foreignRelation, matchingRelation));
        KnowledgeDocumentRelationServiceImpl service = new KnowledgeDocumentRelationServiceImpl(
                points, documents, relations, documentRelations, spaces);
        assertEquals(List.of("matching"), service.listDocuments(ACTOR, 7L).stream()
                .map(KnowledgeDocumentRelationService.DocumentSummary::title).toList());

        service.replaceDocuments(ACTOR, 7L, null, null);
        verify(relations).deleteByKnowledgeId(ACTOR.tenantId(), 3L, 7L);
        service.replacePoints(ACTOR, 11L, null, null);
        verify(relations).deleteByDocId(ACTOR.tenantId(), 3L, 11L);

        KnowledgeDocumentBO unscoped = document(20L, null, "unscoped");
        unscoped.setTenantId(null);
        when(documents.selectById(ACTOR.tenantId(), 20L)).thenReturn(unscoped);
        when(relations.selectByDocId(ACTOR.tenantId(), null, 20L)).thenReturn(List.of());
        assertTrue(service.listPointIds(ACTOR, 20L).isEmpty());
        service.removeDocumentRelations(ACTOR, 20L);
        verify(documentRelations, never()).deleteByDocument(any(), eq(20L));
        assertTrue(service.listDocumentRelations(ACTOR, 20L).isEmpty());
        assertThrows(ServiceException.class, () -> service.replaceDocumentRelations(ACTOR, 20L, null));

        when(documents.selectById(ACTOR.tenantId(), 12L)).thenReturn(foreign);
        assertThrows(ServiceException.class, () -> service.replaceDocuments(ACTOR, 7L, List.of(12L), "RELATED"));
        KnowledgeBO foreignPoint = point(8L, 4L);
        when(points.findById(ACTOR.tenantId(), 8L)).thenReturn(foreignPoint);
        assertThrows(ServiceException.class, () -> service.replacePoints(ACTOR, 11L, List.of(8L), "RELATED"));
        when(points.findById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.listDocuments(ACTOR, 99L));
        KnowledgeBO unboundPoint = point(100L, null);
        when(points.findById(ACTOR.tenantId(), 100L)).thenReturn(unboundPoint);
        assertThrows(ServiceException.class, () -> service.listDocuments(ACTOR, 100L));

        KnowledgeDocumentRelationBO missingTarget = new KnowledgeDocumentRelationBO();
        missingTarget.setId(10L);
        missingTarget.setSourceDocumentId(11L);
        missingTarget.setTargetDocumentId(12L);
        missingTarget.setRelationType("REFERENCES");
        when(documentRelations.selectBySource(ACTOR.tenantId(), 3L, 11L)).thenReturn(List.of(missingTarget));
        when(documents.selectById(ACTOR.tenantId(), 12L)).thenReturn(null);
        assertNull(service.listDocumentRelations(ACTOR, 11L).getFirst().targetTitle());
        when(documents.selectById(ACTOR.tenantId(), 12L)).thenReturn(document(12L, 3L, "target"));
        service.replaceDocumentRelations(ACTOR, 11L, List.of(
                new KnowledgeDocumentRelationService.DocumentRelationRequest(12L, "REFERENCES")));
        verify(documentRelations, atLeastOnce()).replace(eq(ACTOR.tenantId()), eq(3L), eq(11L), anyList());
        assertThrows(ServiceException.class, () -> service.replaceDocumentRelations(ACTOR, 11L, List.of(
                new KnowledgeDocumentRelationService.DocumentRelationRequest(12L, "INVALID"))));
    }

    private static KnowledgeBO point(Long id, Long space) {
        KnowledgeBO value = new KnowledgeBO(); value.setId(id); value.setSpaceId(space); return value;
    }

    private static KnowledgeDocumentBO document(Long id, Long space, String title) {
        KnowledgeDocumentBO value = new KnowledgeDocumentBO(); value.setId(id); value.setSpaceId(space); value.setTenantId(10L); value.setTitle(title); value.setDocType("TXT"); return value;
    }

    private static KnowledgeDocRelationBO pointRelation(Long docId, Long pointId, String type) {
        KnowledgeDocRelationBO value = new KnowledgeDocRelationBO(); value.setDocId(docId); value.setKnowledgeId(pointId); value.setRelationType(type); return value;
    }
}
