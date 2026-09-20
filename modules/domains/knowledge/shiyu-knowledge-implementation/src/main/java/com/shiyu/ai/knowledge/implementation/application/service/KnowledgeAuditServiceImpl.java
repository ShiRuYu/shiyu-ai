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
 * 提供 知识 Audit 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
@RequiredArgsConstructor
public class KnowledgeAuditServiceImpl implements KnowledgeAuditService {

    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final KnowledgeEnterpriseRepository repository;

    /**
     * 执行 知识 Audit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param resourceType 用于完成本次业务处理的 resourceType 参数。
     * @param resourceId 用于定位resource的标识。
     * @param action 用于完成本次业务处理的 action 参数。
     * @param detail 用于完成本次业务处理的 detail 参数。
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
     * 查询 知识 Audit 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 Audit 相关操作生成的结果数据。
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
