package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeAuditLogDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentVersionDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeEvaluationCaseDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeIngestionJobDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeReviewRecordDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeSpaceDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeSpaceMemberDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeAuditLogMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDocumentVersionMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeEvaluationCaseMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeIngestionJobMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeReviewRecordMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeSpaceMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeSpaceMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class KnowledgeEnterpriseRepository {

    private final KnowledgeSpaceMapper spaceMapper;
    private final KnowledgeSpaceMemberMapper memberMapper;
    private final KnowledgeDocumentVersionMapper versionMapper;
    private final KnowledgeReviewRecordMapper reviewMapper;
    private final KnowledgeIngestionJobMapper jobMapper;
    private final KnowledgeAuditLogMapper auditMapper;
    private final KnowledgeEvaluationCaseMapper evaluationMapper;

    public KnowledgeSpaceDO findSpace(Long id) {
        return spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getId, id)
                .eq(KnowledgeSpaceDO::getDelFlag, 0));
    }

    public KnowledgeSpaceDO findSpaceByTenant(Long tenantId, Long id) {
        if (tenantId == null || id == null) {
            return null;
        }
        return TenantManager.withoutTenantCondition(() -> spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId)
                .eq(KnowledgeSpaceDO::getId, id)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)));
    }

    public List<KnowledgeSpaceDO> findActiveSpacesByTenant(Long tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        return TenantManager.withoutTenantCondition(() -> spaceMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId)
                .eq(KnowledgeSpaceDO::getStatus, 1)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)
                .orderBy(KnowledgeSpaceDO::getId, true)));
    }

    public KnowledgeSpaceDO findSpaceByCode(String code) {
        return spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getCode, code)
                .eq(KnowledgeSpaceDO::getDelFlag, 0));
    }

    /** Find a space by tenant while provisioning outside the current tenant context. */
    public KnowledgeSpaceDO findSpaceByTenantAndCode(Long tenantId, String code) {
        return TenantManager.withoutTenantCondition(() -> spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId)
                .eq(KnowledgeSpaceDO::getCode, code)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)));
    }

    public PageData<KnowledgeSpaceDO> pageSpaces(int pageNum, int pageSize, String keyword) {
        return pageSpaces(pageNum, pageSize, keyword, null);
    }

    public PageData<KnowledgeSpaceDO> pageSpaces(int pageNum, int pageSize, String keyword,
                                                 String domainCode) {
        QueryWrapper query = QueryWrapper.create().eq(KnowledgeSpaceDO::getDelFlag, 0);
        if (keyword != null && !keyword.isBlank()) {
            query.like(KnowledgeSpaceDO::getName, keyword);
        }
        if (domainCode != null && !domainCode.isBlank()) {
            query.eq(KnowledgeSpaceDO::getDomainCode, domainCode);
        }
        Page<KnowledgeSpaceDO> page = spaceMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeSpaceDO::getId, false));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public KnowledgeSpaceDO insertSpace(KnowledgeSpaceDO space) {
        spaceMapper.insert(space);
        return space;
    }

    public void updateSpace(KnowledgeSpaceDO space) {
        spaceMapper.update(space);
    }

    public void deleteSpace(Long id) {
        spaceMapper.deleteById(id);
    }

    public List<KnowledgeSpaceMemberDO> findMembers(Long spaceId) {
        return memberMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0)
                .orderBy(KnowledgeSpaceMemberDO::getId, true));
    }

    public void replaceMembers(Long spaceId, List<KnowledgeSpaceMemberDO> members) {
        memberMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId));
        if (!members.isEmpty()) {
            memberMapper.insertBatch(members);
        }
    }

    public boolean hasMember(Long spaceId, String principalType, Long principalId,
                             List<String> acceptedRoles) {
        if (principalId == null) {
            return false;
        }
        return memberMapper.selectCountByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                .eq(KnowledgeSpaceMemberDO::getPrincipalType, principalType)
                .eq(KnowledgeSpaceMemberDO::getPrincipalId, principalId)
                .in(KnowledgeSpaceMemberDO::getSpaceRole, acceptedRoles)
                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0)) > 0;
    }

    public boolean hasMember(Long tenantId, Long spaceId, String principalType,
                             Long principalId, List<String> acceptedRoles) {
        if (tenantId == null || principalId == null) {
            return false;
        }
        return memberMapper.selectCountByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getTenantId, tenantId)
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                .eq(KnowledgeSpaceMemberDO::getPrincipalType, principalType)
                .eq(KnowledgeSpaceMemberDO::getPrincipalId, principalId)
                .in(KnowledgeSpaceMemberDO::getSpaceRole, acceptedRoles)
                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0)) > 0;
    }

    public KnowledgeDocumentVersionDO findVersion(Long id) {
        return versionMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getId, id)
                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0));
    }

    public List<KnowledgeDocumentVersionDO> findVersions(Long documentId) {
        return versionMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0)
                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false));
    }

    public int nextVersionNo(Long documentId) {
        KnowledgeDocumentVersionDO latest = versionMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false)
                .limit(1));
        return latest == null ? 1 : latest.getVersionNo() + 1;
    }

    public KnowledgeDocumentVersionDO insertVersion(KnowledgeDocumentVersionDO version) {
        versionMapper.insert(version);
        return version;
    }

    public void updateVersion(KnowledgeDocumentVersionDO version) {
        versionMapper.update(version);
    }

    public void insertReview(KnowledgeReviewRecordDO review) {
        reviewMapper.insert(review);
    }

    public KnowledgeIngestionJobDO findJob(Long id) {
        return jobMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getId, id)
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
    }

    public KnowledgeIngestionJobDO findJobByKey(String jobKey) {
        return jobMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getJobKey, jobKey)
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
    }

    public KnowledgeIngestionJobDO insertJob(KnowledgeIngestionJobDO job) {
        jobMapper.insert(job);
        return job;
    }

    public void updateJob(KnowledgeIngestionJobDO job) {
        jobMapper.update(job);
    }

    public PageData<KnowledgeIngestionJobDO> pageJobs(int pageNum, int pageSize,
                                                      Long spaceId, String status) {
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0);
        if (spaceId != null) {
            query.eq(KnowledgeIngestionJobDO::getSpaceId, spaceId);
        }
        if (status != null && !status.isBlank()) {
            query.eq(KnowledgeIngestionJobDO::getJobStatus, status);
        }
        Page<KnowledgeIngestionJobDO> page = jobMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeIngestionJobDO::getId, false));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public List<KnowledgeIngestionJobDO> pollPendingJobs(int limit) {
        return TenantManager.withoutTenantCondition(() ->
                jobMapper.selectListByQuery(QueryWrapper.create()
                        .eq(KnowledgeIngestionJobDO::getJobStatus, "PENDING")
                        .eq(KnowledgeIngestionJobDO::getDelFlag, 0)
                        .orderBy(KnowledgeIngestionJobDO::getCreateTime, true)
                        .limit(0, limit)));
    }

    public List<KnowledgeIngestionJobDO> findStaleJobs(LocalDateTime heartbeatBefore) {
        return TenantManager.withoutTenantCondition(() ->
                jobMapper.selectListByQuery(QueryWrapper.create()
                        .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                        .lt(KnowledgeIngestionJobDO::getHeartbeatTime, heartbeatBefore)
                        .eq(KnowledgeIngestionJobDO::getDelFlag, 0)));
    }

    public void insertAudit(KnowledgeAuditLogDO audit) {
        auditMapper.insert(audit);
    }

    public PageData<KnowledgeAuditLogDO> pageAudit(int pageNum, int pageSize, Long spaceId) {
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeAuditLogDO::getDelFlag, 0);
        if (spaceId != null) {
            query.eq(KnowledgeAuditLogDO::getSpaceId, spaceId);
        }
        Page<KnowledgeAuditLogDO> page = auditMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeAuditLogDO::getId, false));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public KnowledgeEvaluationCaseDO insertEvaluation(KnowledgeEvaluationCaseDO evaluation) {
        evaluationMapper.insert(evaluation);
        return evaluation;
    }

    public PageData<KnowledgeEvaluationCaseDO> pageEvaluations(int pageNum, int pageSize,
                                                               Long spaceId) {
        Page<KnowledgeEvaluationCaseDO> page = evaluationMapper.paginate(pageNum, pageSize,
                QueryWrapper.create()
                        .eq(KnowledgeEvaluationCaseDO::getSpaceId, spaceId)
                        .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0)
                        .orderBy(KnowledgeEvaluationCaseDO::getId, false));
        return new PageData<>(page.getRecords(), page.getTotalRow());
    }

    public KnowledgeEvaluationCaseDO findEvaluation(Long id) {
        return evaluationMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeEvaluationCaseDO::getId, id)
                .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0));
    }

    public void deleteEvaluation(Long id) {
        evaluationMapper.deleteById(id);
    }
}
