package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeEvaluationCaseBO;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.service.KnowledgeEvaluationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeEvaluationDeepCoverageTest {
    @Test
    void pagesCreatesAndRunsEvaluationCasesWithNormalizedTopK() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeIndexService index = mock(KnowledgeIndexService.class);
        ActorContext actor = actor();
        KnowledgeEvaluationCaseBO first = evaluation(1L, 8L, "42, invalid, 42");
        KnowledgeEvaluationCaseBO second = evaluation(2L, 8L, null);
        when(repository.pageEvaluations(new TenantId(7L), 1, 100, 8L))
                .thenReturn(new PageData<>(List.of(first, second), 2));
        when(repository.pageEvaluations(new TenantId(7L), 1, 1000, 8L))
                .thenReturn(new PageData<>(List.of(first, second), 2));
        when(index.hybridSearch(eq(actor), eq(8L), any(String.class), eq("HYBRID"), eq(5), eq(0D), eq(true)))
                .thenReturn(List.of(
                        new KnowledgeIndexService.HybridHit(10L, 42L, "answer", null, 1D, 2D, 3D, 4D),
                        new KnowledgeIndexService.HybridHit(11L, 99L, "other", null, 1D, 1D, 1D, 1D),
                        new KnowledgeIndexService.HybridHit(12L, null, "unknown", null, 1D, 1D, 1D, 1D)));
        KnowledgeEvaluationServiceImpl service = new KnowledgeEvaluationServiceImpl(repository, spaces, index);

        assertEquals(2, service.page(actor, 1, 200, 8L).getItems().size());
        KnowledgeEvaluationService.CaseView created = service.create(actor,
                new KnowledgeEvaluationService.CreateCaseRequest(8L, " question ", "42", "expected"));
        assertEquals("question", created.question());
        verify(repository).insertEvaluation(eq(new TenantId(7L)), any(KnowledgeEvaluationCaseBO.class));

        KnowledgeEvaluationService.RunResult result = service.run(actor,
                new KnowledgeEvaluationService.RunRequest(8L, null));
        assertEquals(2, result.caseCount());
        assertEquals(5, result.topK());
        assertEquals(0.5D, result.recallAtK());
        assertEquals(0.5D, result.mrr());
        assertEquals(0.25D, result.citationAccuracy());
    }

    @Test
    void rejectsMissingSpacesCasesAndMissingEvaluationRows() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeEvaluationServiceImpl service = new KnowledgeEvaluationServiceImpl(repository, spaces, mock(KnowledgeIndexService.class));
        ActorContext actor = actor();
        assertThrows(ServiceException.class, () -> service.page(null, 1, 10, 8L));
        assertThrows(ServiceException.class, () -> service.page(actor, 1, 10, null));
        assertThrows(ServiceException.class, () -> service.create(actor,
                new KnowledgeEvaluationService.CreateCaseRequest(null, "q", null, null)));
        assertThrows(ServiceException.class, () -> service.run(actor, null));
        assertThrows(ServiceException.class, () -> service.delete(actor, 9L));
        when(repository.findEvaluation(new TenantId(7L), 9L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.delete(actor, 9L));
    }

    private KnowledgeEvaluationCaseBO evaluation(Long id, Long spaceId, String expected) {
        KnowledgeEvaluationCaseBO value = new KnowledgeEvaluationCaseBO();
        value.setId(id); value.setTenantId(7L); value.setSpaceId(spaceId); value.setQuestion("q" + id);
        value.setExpectedDocIds(expected); value.setExpectedAnswer("a");
        return value;
    }

    private ActorContext actor() {
        return new ActorContext(new TenantId(7L), new UserId(9L), new RoleId(3L), false);
    }
}
