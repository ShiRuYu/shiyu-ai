package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeEvaluationCaseBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeReviewRecordBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceMemberBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.time.LocalDateTime;
import java.util.List;

public interface KnowledgeEnterpriseRepository {
    KnowledgeSpaceBO findSpace(TenantId tenantId, Long id);
    KnowledgeSpaceBO findSpaceByTenant(TenantId tenantId, Long id);
    List<KnowledgeSpaceBO> findActiveSpacesByTenant(TenantId tenantId);
    List<KnowledgeSpaceBO> findAllActiveSpaces();
    KnowledgeSpaceBO findSpaceByTenantAndCode(TenantId tenantId, String code);
    PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword);
    PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword, String domainCode);
    PageData<KnowledgeSpaceBO> pageSpacesByTenant(TenantId tenantId, int pageNum, int pageSize,
                                                   String keyword, String domainCode);
    KnowledgeSpaceBO insertSpace(TenantId tenantId, KnowledgeSpaceBO space);
    void updateSpace(TenantId tenantId, KnowledgeSpaceBO space);
    void deleteSpace(TenantId tenantId, Long id);
    List<KnowledgeSpaceMemberBO> findMembers(TenantId tenantId, Long spaceId);
    void replaceMembers(TenantId tenantId, Long spaceId, List<KnowledgeSpaceMemberBO> members);
    boolean hasMember(TenantId tenantId, Long spaceId, String principalType, Long principalId, List<String> acceptedRoles);
    KnowledgeDocumentVersionBO findVersion(TenantId tenantId, Long id);
    List<KnowledgeDocumentVersionBO> findVersions(TenantId tenantId, Long documentId);
    int nextVersionNo(TenantId tenantId, Long documentId);
    KnowledgeDocumentVersionBO insertVersion(TenantId tenantId, KnowledgeDocumentVersionBO version);
    void updateVersion(TenantId tenantId, KnowledgeDocumentVersionBO version);
    void insertReview(TenantId tenantId, KnowledgeReviewRecordBO review);
    KnowledgeIngestionJobBO findJob(TenantId tenantId, Long id);
    KnowledgeIngestionJobBO findJobByKey(TenantId tenantId, String jobKey);
    KnowledgeIngestionJobBO insertJob(TenantId tenantId, KnowledgeIngestionJobBO job);
    void updateJob(TenantId tenantId, KnowledgeIngestionJobBO job);
    PageData<KnowledgeIngestionJobBO> pageJobsByTenant(TenantId tenantId, int pageNum, int pageSize,
                                                        Long spaceId, String status);
    List<KnowledgeIngestionJobBO> pollPendingJobs(int limit);
    List<KnowledgeIngestionJobBO> findStaleJobs(LocalDateTime heartbeatBefore);
    void insertAudit(TenantId tenantId, KnowledgeAuditLogBO audit);
    PageData<KnowledgeAuditLogBO> pageAudit(TenantId tenantId, int pageNum, int pageSize, Long spaceId);
    KnowledgeEvaluationCaseBO insertEvaluation(TenantId tenantId, KnowledgeEvaluationCaseBO evaluation);
    PageData<KnowledgeEvaluationCaseBO> pageEvaluations(TenantId tenantId, int pageNum, int pageSize, Long spaceId);
    KnowledgeEvaluationCaseBO findEvaluation(TenantId tenantId, Long id);
    void deleteEvaluation(TenantId tenantId, Long id);
}
