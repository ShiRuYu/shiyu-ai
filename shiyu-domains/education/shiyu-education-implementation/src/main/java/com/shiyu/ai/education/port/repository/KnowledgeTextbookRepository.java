package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.KnowledgeTextbookBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface KnowledgeTextbookRepository {
    void insert(TenantId tenantId, KnowledgeTextbookBO kt);
    void deleteById(TenantId tenantId, Long id);
    void deleteByChapterId(TenantId tenantId, Long cid);
    void deleteByKnowledgeIdAndChapterId(TenantId tenantId, Long kid, Long cid);
    List<KnowledgeTextbookBO> selectByChapterId(TenantId tenantId, Long cid);
    List<KnowledgeTextbookBO> selectByKnowledgeId(TenantId tenantId, Long kid);
    List<KnowledgeTextbookBO> selectByTextbookId(TenantId tenantId, Long tid);
}
