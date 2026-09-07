package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeJobServiceImplTest {
    @Test
    void pagesFiltersUnauthorizedJobsAndSupportsCancelRetryTransitions() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class); KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class); ActorContext actor = new ActorContext(new TenantId(3), new UserId(4), false);
        KnowledgeIngestionJobBO visible = job(1L, 7L, "RUNNING"); KnowledgeIngestionJobBO hidden = job(2L, 8L, "RUNNING");
        when(repository.pageJobsByTenant(actor.tenantId(), 1, 10, null, null)).thenReturn(new PageData<>(List.of(visible, hidden), 2)); when(repository.findJob(actor.tenantId(), 1L)).thenReturn(visible);
        doNothing().when(spaces).requireAccess(eq(7L), any(), eq(actor)); doThrow(new ServiceException("denied")).when(spaces).requireAccess(eq(8L), any(), eq(actor));
        KnowledgeJobServiceImpl service = new KnowledgeJobServiceImpl(repository, spaces); assertEquals(1, service.page(actor, 1, 10, null, null).getItems().size()); assertNotNull(service.get(actor, 1L));
        service.cancel(actor, 1L); verify(repository, atLeastOnce()).updateJob(eq(actor.tenantId()), argThat(job -> "CANCELLED".equals(job.getJobStatus())));
        visible.setJobStatus("FAILED"); service.retry(actor, 1L); verify(repository, atLeastOnce()).updateJob(eq(actor.tenantId()), argThat(job -> "PENDING".equals(job.getJobStatus())));
        visible.setJobStatus("SUCCEEDED"); assertThrows(ServiceException.class, () -> service.cancel(actor, 1L));
        visible.setJobStatus("RUNNING"); assertThrows(ServiceException.class, () -> service.retry(actor, 1L));
        assertThrows(ServiceException.class, () -> service.page(null, 1, 10, null, null));
    }

    @Test
    void missingJobsAndSpaceAccessFailuresAreRejected() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class); KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class); ActorContext actor = new ActorContext(new TenantId(3), new UserId(4), false);
        when(repository.findJob(actor.tenantId(), 9L)).thenReturn(null); KnowledgeJobServiceImpl service = new KnowledgeJobServiceImpl(repository, spaces);
        assertThrows(ServiceException.class, () -> service.get(actor, 9L)); assertThrows(ServiceException.class, () -> service.cancel(actor, 9L)); assertThrows(ServiceException.class, () -> service.retry(actor, 9L));
    }

    private static KnowledgeIngestionJobBO job(Long id, Long space, String status) { KnowledgeIngestionJobBO job = new KnowledgeIngestionJobBO(); job.setId(id); job.setSpaceId(space); job.setJobStatus(status); job.setStage(status); job.setProgress(1); job.setAttempts(1); job.setMaxAttempts(3); return job; }
}
