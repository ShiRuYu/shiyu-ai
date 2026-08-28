package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface KnowledgeDocumentRepository {
    KnowledgeDocumentBO selectById(TenantId tenantId, Long id);
    List<KnowledgeDocumentBO> selectAll(TenantId tenantId);
    int insert(TenantId tenantId, KnowledgeDocumentBO bo);
    int update(TenantId tenantId, KnowledgeDocumentBO bo);
    int deleteById(TenantId tenantId, Long id);
    List<KnowledgeDocumentBO> searchByKeyword(TenantId tenantId, String keyword, int topK);
    List<KnowledgeDocumentBO> selectByKnowledgeId(TenantId tenantId, Long knowledgeId);
    List<KnowledgeDocumentBO> selectByKnowledgeId(TenantId tenantId, Long spaceId, Long knowledgeId);
    PageData<KnowledgeDocumentBO> pageBySpace(TenantId tenantId, Long spaceId, int pageNum, int pageSize, String keyword, String lifecycleStatus, String parseStatus);
    KnowledgeDocumentBO findBySpaceAndChecksum(TenantId tenantId, Long spaceId, String checksum);
    List<KnowledgeDocumentBO> findBySpace(TenantId tenantId, Long spaceId);
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
