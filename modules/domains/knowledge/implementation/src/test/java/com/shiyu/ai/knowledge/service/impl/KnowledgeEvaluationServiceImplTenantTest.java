package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeEvaluationCaseBO;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KnowledgeEvaluationServiceImplTenantTest {

    @Test
    void scopesEvaluationDeletionToActorTenant() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        KnowledgeEvaluationCaseBO evaluation = new KnowledgeEvaluationCaseBO();
        evaluation.setId(8L);
        evaluation.setTenantId(3L);
        evaluation.setSpaceId(11L);
        when(repository.findEvaluation(new TenantId(3L), 8L)).thenReturn(evaluation);
        KnowledgeEvaluationServiceImpl service = new KnowledgeEvaluationServiceImpl(
                repository, spaces, mock(KnowledgeIndexService.class));
        ActorContext actor = new ActorContext(
                new TenantId(3L), new UserId(4L), new RoleId(5L), false);

        service.delete(actor, 8L);

        verify(spaces).requireAccess(11L, KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        verify(repository).deleteEvaluation(new TenantId(3L), 8L);
    }

    @Test
    void rejectsMissingActorBeforeRepositoryAccess() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeEvaluationServiceImpl service = new KnowledgeEvaluationServiceImpl(
                repository, mock(KnowledgeSpaceService.class), mock(KnowledgeIndexService.class));

        assertThrows(ServiceException.class, () -> service.delete(null, 8L));
        verifyNoInteractions(repository);
    }
}
