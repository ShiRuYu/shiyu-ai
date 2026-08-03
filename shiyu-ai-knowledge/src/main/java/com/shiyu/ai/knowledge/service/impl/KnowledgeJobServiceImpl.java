package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.service.KnowledgeJobService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgeJobServiceImpl implements KnowledgeJobService {

    private final KnowledgeEnterpriseRepository repository;
    private final KnowledgeSpaceService spaceService;

    @Override
    public PageData<JobView> page(int pageNum, int pageSize, Long spaceId, String status) {
        if (spaceId != null) {
            spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER);
        }
        PageData<KnowledgeIngestionJobBO> page = repository.pageJobs(pageNum, pageSize, spaceId, status);
        return new PageData<>(page.getItems().stream()
                .filter(job -> canView(job.getSpaceId()))
                .map(this::toView).toList(), page.getTotal());
    }

    @Override
    public JobView get(Long id) {
        KnowledgeIngestionJobBO job = requireJob(id);
        spaceService.requireAccess(job.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
        return toView(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        KnowledgeIngestionJobBO job = requireJob(id);
        spaceService.requireAccess(job.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        if ("SUCCEEDED".equals(job.getJobStatus()) || "CANCELLED".equals(job.getJobStatus())) {
            throw new ServiceException("当前任务不能取消");
        }
        job.setJobStatus("CANCELLED");
        job.setStage("CANCELLED");
        job.setFinishedTime(java.time.LocalDateTime.now());
        repository.updateJob(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retry(Long id) {
        KnowledgeIngestionJobBO job = requireJob(id);
        spaceService.requireAccess(job.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        if (!"FAILED".equals(job.getJobStatus()) && !"CANCELLED".equals(job.getJobStatus())) {
            throw new ServiceException("只有失败或已取消的任务可以重试");
        }
        job.setJobStatus("PENDING");
        job.setStage("QUEUED");
        job.setProgress(0);
        job.setErrorMessage(null);
        job.setFinishedTime(null);
        repository.updateJob(job);
    }

    private KnowledgeIngestionJobBO requireJob(Long id) {
        KnowledgeIngestionJobBO job = repository.findJob(id);
        if (job == null) throw new ServiceException("任务不存在: " + id);
        return job;
    }

    private boolean canView(Long spaceId) {
        try {
            spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER);
            return true;
        } catch (ServiceException ignored) {
            return false;
        }
    }

    private JobView toView(KnowledgeIngestionJobBO job) {
        return new JobView(job.getId(), job.getJobKey(), job.getJobType(), job.getSpaceId(),
                job.getDocumentId(), job.getVersionId(), job.getJobStatus(), job.getStage(),
                job.getProgress(), job.getAttempts(), job.getMaxAttempts(), job.getErrorMessage(),
                job.getHeartbeatTime(), job.getStartedTime(), job.getFinishedTime(),
                job.getCreateTime());
    }
}
