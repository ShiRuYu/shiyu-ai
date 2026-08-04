package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentRelationBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentRelationDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocumentRelationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocumentRelationRepositoryImpl implements com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRelationRepository {
    @Resource
    private KnowledgeDocumentRelationMapper mapper;

    public List<KnowledgeDocumentRelationBO> selectBySource(Long spaceId, Long documentId) {
        return convert(QueryWrapper.create().eq("space_id", spaceId)
                .eq("source_document_id", documentId).eq("del_flag", 0));
    }

    public List<KnowledgeDocumentRelationBO> selectByTarget(Long spaceId, Long documentId) {
        return convert(QueryWrapper.create().eq("space_id", spaceId)
                .eq("target_document_id", documentId).eq("del_flag", 0));
    }

    public void replace(Long tenantId, Long spaceId, Long sourceId,
                        List<KnowledgeDocumentRelationBO> relations) {
        mapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId)
                .eq("space_id", spaceId).eq("source_document_id", sourceId));
        if (!relations.isEmpty()) {
            mapper.insertBatch(MapstructUtils.convert(relations, KnowledgeDocumentRelationDO.class));
        }
    }

    public void deleteByDocument(Long tenantId, Long documentId) {
        mapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId)
                .eq("source_document_id", documentId));
        mapper.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId)
                .eq("target_document_id", documentId));
    }

    private List<KnowledgeDocumentRelationBO> convert(QueryWrapper wrapper) {
        return MapstructUtils.convert(mapper.selectListByQuery(wrapper), KnowledgeDocumentRelationBO.class);
    }
}
