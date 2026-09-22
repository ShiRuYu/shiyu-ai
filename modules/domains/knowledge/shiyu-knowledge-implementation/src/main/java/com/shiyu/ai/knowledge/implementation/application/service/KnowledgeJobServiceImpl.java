package com.shiyu.ai.knowledge.implementation.application.service;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeJobService.JobView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeJobService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提供 知识 Job 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeJobServiceImpl implements KnowledgeJobService {

    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final KnowledgeEnterpriseRepository repository;
    /**
     * spaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceService spaceService;

    /**
     * 查询 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 知识 Job 相关操作生成的结果数据。
     */
    @Override
    public PageData<JobView> page(
            ActorContext actor, int pageNum, int pageSize, Long spaceId, String status) {
        requireActor(actor);
        if (spaceId != null) {
            spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        }
        PageData<KnowledgeIngestionJobBO> page =
                repository.pageJobsByTenant(actor.tenantId(), pageNum, pageSize, spaceId, status);
        return new PageData<>(
                page.getItems().stream()
                        .filter(job -> canView(actor, job.getSpaceId()))
                        .map(this::toView)
                        .toList(),
                page.getTotal());
    }

    /**
     * 查询 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Job 相关操作生成的结果数据。
     */
    @Override
    public JobView get(ActorContext actor, Long id) {
        KnowledgeIngestionJobBO job = requireJob(actor, id);
        spaceService.requireAccess(job.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return toView(job);
    }

    /**
     * 执行 知识 Job 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
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

    /**
     * 执行 知识 Job 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
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
        return new JobView(
                job.getId(),
                job.getJobKey(),
                job.getJobType(),
                job.getSpaceId(),
                job.getDocumentId(),
                job.getVersionId(),
                job.getJobStatus(),
                job.getStage(),
                job.getProgress(),
                job.getAttempts(),
                job.getMaxAttempts(),
                job.getErrorMessage(),
                job.getHeartbeatTime(),
                job.getStartedTime(),
                job.getFinishedTime(),
                job.getCreateTime());
    }
}
