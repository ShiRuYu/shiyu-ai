package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentRelationBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentRelationDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocumentRelationMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocumentRelationRepositoryImpl implements com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRelationRepository {
    @Resource
    private KnowledgeDocumentRelationMapper mapper;

    public List<KnowledgeDocumentRelationBO> selectBySource(TenantId tenantId, Long spaceId, Long documentId) {
        requireTenant(tenantId);
        return convert(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("space_id", spaceId)
                .eq("source_document_id", documentId).eq("del_flag", 0));
    }

    public List<KnowledgeDocumentRelationBO> selectByTarget(TenantId tenantId, Long spaceId, Long documentId) {
        requireTenant(tenantId);
        return convert(QueryWrapper.create().eq("tenant_id", tenantId.value()).eq("space_id", spaceId)
                .eq("target_document_id", documentId).eq("del_flag", 0));
    }

    public void replace(TenantId tenantId, Long spaceId, Long sourceId,
                        List<KnowledgeDocumentRelationBO> relations) {
        requireTenant(tenantId);
        mapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value())
                .eq("space_id", spaceId).eq("source_document_id", sourceId));
        if (!relations.isEmpty()) {
            relations.forEach(relation -> relation.setTenantId(tenantId.value()));
            mapper.insertBatch(MapstructUtils.convert(relations, KnowledgeDocumentRelationDO.class));
        }
    }

    public void deleteByDocument(TenantId tenantId, Long documentId) {
        requireTenant(tenantId);
        mapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value())
                .eq("source_document_id", documentId));
        mapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value())
                .eq("target_document_id", documentId));
    }

    private List<KnowledgeDocumentRelationBO> convert(QueryWrapper wrapper) {
        return MapstructUtils.convert(mapper.selectListByQuery(wrapper), KnowledgeDocumentRelationBO.class);
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
    }
}

