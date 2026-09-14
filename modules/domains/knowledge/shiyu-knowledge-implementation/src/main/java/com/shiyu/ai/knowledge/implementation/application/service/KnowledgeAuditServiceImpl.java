package com.shiyu.ai.knowledge.implementation.application.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeAuditService;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeAuditResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * {@code KnowledgeAuditServiceImpl} 实现知识模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeAuditServiceImpl implements KnowledgeAuditService {

    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final KnowledgeEnterpriseRepository repository;

    /**
     * {@code record} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param resourceType 参数值，用于执行当前操作。
     * @param resourceId 参数值，用于执行当前操作。
     * @param action 参数值，用于执行当前操作。
     * @param detail 参数值，用于执行当前操作。
     */
    @Override
    public void record(
            ActorContext actor,
            Long spaceId,
            String resourceType,
            Long resourceId,
            String action,
            Object detail) {
        requireActor(actor);
        KnowledgeAuditLogBO audit = new KnowledgeAuditLogBO();
        audit.setTenantId(actor.tenantId().value());
        audit.setSpaceId(spaceId);
        audit.setResourceType(resourceType);
        audit.setResourceId(resourceId);
        audit.setAction(action);
        audit.setDetailJson(detail == null ? null : JSONUtils.toJsonString(detail));
        audit.setStatus(1);
        audit.setDelFlag(0);
        repository.insertAudit(actor.tenantId(), audit);
    }

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PageData<KnowledgeAuditResponse> page(
            ActorContext actor, int pageNum, int pageSize, Long spaceId) {
        requireActor(actor);
        PageData<KnowledgeAuditLogBO> data =
                repository.pageAudit(actor.tenantId(), pageNum, pageSize, spaceId);
        return new PageData<>(
                MapstructUtils.convert(data.getItems(), KnowledgeAuditResponse.class),
                data.getTotal());
    }

    private void requireActor(ActorContext actor) {
        if (actor == null || actor.tenantId() == null || actor.userId() == null) {
            throw new ServiceException("当前租户或用户上下文不存在");
        }
    }
}
