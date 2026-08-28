package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface KnowledgeRepository {
    KnowledgeBO findById(TenantId tenantId, Long id);
    KnowledgeBO findByCode(TenantId tenantId, String code);
    List<KnowledgeBO> findAll(TenantId tenantId);
    List<KnowledgeBO> searchByName(TenantId tenantId, String keyword, int topK);
    List<KnowledgeBO> page(TenantId tenantId, int offset, int limit);
    List<KnowledgeBO> page(TenantId tenantId, int offset, int limit, String category, String keyword);
    long count(TenantId tenantId);
    long count(TenantId tenantId, String category, String keyword);
    int insert(TenantId tenantId, KnowledgeBO bo);
    int update(TenantId tenantId, KnowledgeBO bo);
    int deleteById(TenantId tenantId, Long id);
    boolean existsByCode(TenantId tenantId, String code);
    boolean existsBySpaceAndCode(TenantId tenantId, Long spaceId, String code);
    List<KnowledgeBO> findBySpace(TenantId tenantId, Long spaceId);
    PageData<KnowledgeBO> pageBySpace(TenantId tenantId, Long spaceId, int pageNum, int pageSize,
                                       String keyword, String category);
    int deleteByIdAndSpace(TenantId tenantId, Long id, Long spaceId);
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
