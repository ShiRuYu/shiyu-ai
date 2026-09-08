package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.service.KnowledgeJobService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeJobServiceImpl implements KnowledgeJobService {

    private final KnowledgeEnterpriseRepository repository;
    private final KnowledgeSpaceService spaceService;

    @Override
    public PageData<JobView> page(ActorContext actor, int pageNum, int pageSize, Long spaceId, String status) {
        requireActor(actor);
        if (spaceId != null) {
            spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        }
        PageData<KnowledgeIngestionJobBO> page = repository.pageJobsByTenant(
                actor.tenantId(), pageNum, pageSize, spaceId, status);
        return new PageData<>(page.getItems().stream()
                .filter(job -> canView(actor, job.getSpaceId()))
                .map(this::toView).toList(), page.getTotal());
    }

    @Override
    public JobView get(ActorContext actor, Long id) {
        KnowledgeIngestionJobBO job = requireJob(actor, id);
        spaceService.requireAccess(job.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return toView(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(ActorContext actor, Long id) {
        KnowledgeIngestionJobBO job = requireJob(actor, id);
        spaceService.requireAccess(job.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        if ("SUCCEEDED".equals(job.getJobStatus()) || "CANCELLED".equals(job.getJobStatus())) {
            throw new ServiceException("当前任务不能取消");
        }
        job.setJobStatus("CANCELLED");
        job.setStage("CANCELLED");
        job.setFinishedTime(java.time.LocalDateTime.now());
        repository.updateJob(actor.tenantId(), job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(ActorContext actor, Long id) {
        KnowledgeIngestionJobBO job = requireJob(actor, id);
        spaceService.requireAccess(job.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        if (!"FAILED".equals(job.getJobStatus()) && !"CANCELLED".equals(job.getJobStatus())) {
            throw new ServiceException("只有失败或已取消的任务可以重试");
        }
        job.setJobStatus("PENDING");
        job.setStage("QUEUED");
        job.setProgress(0);
        job.setErrorMessage(null);
        job.setFinishedTime(null);
        repository.updateJob(actor.tenantId(), job);
    }

    private KnowledgeIngestionJobBO requireJob(ActorContext actor, Long id) {
        requireActor(actor);
        KnowledgeIngestionJobBO job = repository.findJob(actor.tenantId(), id);
        if (job == null) throw new ServiceException("任务不存在: " + id);
        return job;
    }

    private boolean canView(ActorContext actor, Long spaceId) {
        try {
            spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER, actor);
            return true;
        } catch (ServiceException ignored) {
            return false;
        }
    }

    private void requireActor(ActorContext actor) {
        if (actor == null) throw new ServiceException("actor context is required");
    }

    private JobView toView(KnowledgeIngestionJobBO job) {
        return new JobView(job.getId(), job.getJobKey(), job.getJobType(), job.getSpaceId(),
                job.getDocumentId(), job.getVersionId(), job.getJobStatus(), job.getStage(),
                job.getProgress(), job.getAttempts(), job.getMaxAttempts(), job.getErrorMessage(),
                job.getHeartbeatTime(), job.getStartedTime(), job.getFinishedTime(),
                job.getCreateTime());
    }
}
