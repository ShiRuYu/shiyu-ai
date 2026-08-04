package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeEvaluationCaseBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeReviewRecordBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceMemberBO;
import java.time.LocalDateTime;
import java.util.List;

public interface KnowledgeEnterpriseRepository {
    KnowledgeSpaceBO findSpace(Long id);
    KnowledgeSpaceBO findSpaceByTenant(Long tenantId, Long id);
    List<KnowledgeSpaceBO> findActiveSpacesByTenant(Long tenantId);
    List<KnowledgeSpaceBO> findAllActiveSpaces();
    KnowledgeSpaceBO findSpaceByCode(String code);
    KnowledgeSpaceBO findSpaceByTenantAndCode(Long tenantId, String code);
    PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword);
    PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword, String domainCode);
    KnowledgeSpaceBO insertSpace(KnowledgeSpaceBO space);
    void updateSpace(KnowledgeSpaceBO space);
    void deleteSpace(Long id);
    List<KnowledgeSpaceMemberBO> findMembers(Long spaceId);
    void replaceMembers(Long spaceId, List<KnowledgeSpaceMemberBO> members);
    boolean hasMember(Long spaceId, String principalType, Long principalId, List<String> acceptedRoles);
    boolean hasMember(Long tenantId, Long spaceId, String principalType, Long principalId, List<String> acceptedRoles);
    KnowledgeDocumentVersionBO findVersion(Long id);
    List<KnowledgeDocumentVersionBO> findVersions(Long documentId);
    int nextVersionNo(Long documentId);
    KnowledgeDocumentVersionBO insertVersion(KnowledgeDocumentVersionBO version);
    void updateVersion(KnowledgeDocumentVersionBO version);
    void insertReview(KnowledgeReviewRecordBO review);
    KnowledgeIngestionJobBO findJob(Long id);
    KnowledgeIngestionJobBO findJobByKey(String jobKey);
    KnowledgeIngestionJobBO insertJob(KnowledgeIngestionJobBO job);
    void updateJob(KnowledgeIngestionJobBO job);
    PageData<KnowledgeIngestionJobBO> pageJobs(int pageNum, int pageSize, Long spaceId, String status);
    List<KnowledgeIngestionJobBO> pollPendingJobs(int limit);
    List<KnowledgeIngestionJobBO> findStaleJobs(LocalDateTime heartbeatBefore);
    void insertAudit(KnowledgeAuditLogBO audit);
    PageData<KnowledgeAuditLogBO> pageAudit(int pageNum, int pageSize, Long spaceId);
    KnowledgeEvaluationCaseBO insertEvaluation(KnowledgeEvaluationCaseBO evaluation);
    PageData<KnowledgeEvaluationCaseBO> pageEvaluations(int pageNum, int pageSize, Long spaceId);
    KnowledgeEvaluationCaseBO findEvaluation(Long id);
    void deleteEvaluation(Long id);
}
