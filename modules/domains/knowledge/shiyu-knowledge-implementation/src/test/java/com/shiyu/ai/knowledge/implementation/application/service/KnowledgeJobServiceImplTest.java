package com.shiyu.ai.knowledge.implementation.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 验证 知识 Job Service Impl 相关功能、边界条件、异常路径和协作行为。
 */
class KnowledgeJobServiceImplTest {
    @Test
    void pagesFiltersUnauthorizedJobsAndSupportsCancelRetryTransitions() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        ActorContext actor = new ActorContext(new TenantId(3), new UserId(4), false);
        KnowledgeIngestionJobBO visible = job(1L, 7L, "RUNNING");
        visible.setJobType("INGEST");
        visible.setLockVersion(4L);
        KnowledgeIngestionJobBO hidden = job(2L, 8L, "RUNNING");
        KnowledgeIngestionJobBO cancelled = job(1L, 7L, "CANCELLED");
        cancelled.setJobType("INGEST");
        cancelled.setLockVersion(5L);
        cancelled.setProgress(35);
        cancelled.setErrorMessage("previous attempt failed");
        cancelled.setFinishedTime(java.time.LocalDateTime.now());
        KnowledgeIngestionJobBO pending = job(1L, 7L, "PENDING");
        pending.setJobType("INGEST");
        pending.setStage("QUEUED");
        pending.setProgress(0);
        pending.setLockVersion(6L);
        KnowledgeIngestionJobBO succeeded = job(1L, 7L, "SUCCEEDED");
        KnowledgeIngestionJobBO running = job(1L, 7L, "RUNNING");
        when(repository.pageJobsByTenant(actor.tenantId(), 1, 10, null, null))
                .thenReturn(new PageData<>(List.of(visible, hidden), 2));
        when(repository.findJob(actor.tenantId(), 1L))
                .thenReturn(visible, visible, cancelled, pending, succeeded, running);
        when(repository.cancelIngestionJob(actor.tenantId(), visible)).thenReturn(true);
        when(repository.cancelIngestionJob(actor.tenantId(), pending)).thenReturn(true);
        doNothing().when(spaces).requireAccess(eq(7L), any(), eq(actor));
        doThrow(new ServiceException("denied"))
                .when(spaces)
                .requireAccess(eq(8L), any(), eq(actor));
        KnowledgeJobServiceImpl service = new KnowledgeJobServiceImpl(repository, spaces);
        assertEquals(1, service.page(actor, 1, 10, null, null).getItems().size());
        assertNotNull(service.get(actor, 1L));
        service.cancel(actor, 1L);
        verify(repository).cancelIngestionJob(actor.tenantId(), visible);
        service.retry(actor, 1L);
        verify(repository)
                .updateJob(
                        eq(actor.tenantId()),
                        argThat(
                                retried ->
                                        "PENDING".equals(retried.getJobStatus())
                                                && "QUEUED".equals(retried.getStage())
                                                && Integer.valueOf(0).equals(retried.getProgress())
                                                && retried.getErrorMessage() == null
                                                && retried.getFinishedTime() == null
                                                && retried.getLockVersion() == 6L));
        service.cancel(actor, 1L);
        verify(repository).cancelIngestionJob(actor.tenantId(), pending);
        assertThrows(ServiceException.class, () -> service.cancel(actor, 1L));
        assertThrows(ServiceException.class, () -> service.retry(actor, 1L));
        assertThrows(ServiceException.class, () -> service.page(null, 1, 10, null, null));
    }

    @Test
    void rejectsCancelWhenPersistedJobStateHasChanged() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        ActorContext actor = new ActorContext(new TenantId(3), new UserId(4), false);
        KnowledgeIngestionJobBO stale = job(1L, 7L, "RUNNING");
        stale.setJobType("INGEST");
        stale.setLockVersion(4L);
        when(repository.findJob(actor.tenantId(), 1L)).thenReturn(stale);
        when(repository.cancelIngestionJob(actor.tenantId(), stale)).thenReturn(false);
        KnowledgeJobServiceImpl service = new KnowledgeJobServiceImpl(repository, spaces);

        ServiceException failure =
                assertThrows(ServiceException.class, () -> service.cancel(actor, 1L));

        assertEquals("任务状态已变化，请刷新后重试", failure.getMessage());
        verify(repository).cancelIngestionJob(actor.tenantId(), stale);
        verify(repository, never()).updateJob(any(), any());
    }

    @Test
    void missingJobsAndSpaceAccessFailuresAreRejected() {
        KnowledgeEnterpriseRepository repository = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeSpaceService spaces = mock(KnowledgeSpaceService.class);
        ActorContext actor = new ActorContext(new TenantId(3), new UserId(4), false);
        when(repository.findJob(actor.tenantId(), 9L)).thenReturn(null);
        KnowledgeJobServiceImpl service = new KnowledgeJobServiceImpl(repository, spaces);
        assertThrows(ServiceException.class, () -> service.get(actor, 9L));
        assertThrows(ServiceException.class, () -> service.cancel(actor, 9L));
        assertThrows(ServiceException.class, () -> service.retry(actor, 9L));
    }

    private static KnowledgeIngestionJobBO job(Long id, Long space, String status) {
        KnowledgeIngestionJobBO job = new KnowledgeIngestionJobBO();
        job.setId(id);
        job.setSpaceId(space);
        job.setJobStatus(status);
        job.setStage(status);
        job.setProgress(1);
        job.setAttempts(1);
        job.setMaxAttempts(3);
        return job;
    }
}
