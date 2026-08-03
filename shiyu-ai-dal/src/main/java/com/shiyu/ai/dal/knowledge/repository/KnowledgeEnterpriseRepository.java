package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeEvaluationCaseBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeReviewRecordBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceMemberBO;
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
public class KnowledgeEnterpriseRepository implements com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository {

    private final KnowledgeSpaceMapper spaceMapper;
    private final KnowledgeSpaceMemberMapper memberMapper;
    private final KnowledgeDocumentVersionMapper versionMapper;
    private final KnowledgeReviewRecordMapper reviewMapper;
    private final KnowledgeIngestionJobMapper jobMapper;
    private final KnowledgeAuditLogMapper auditMapper;
    private final KnowledgeEvaluationCaseMapper evaluationMapper;

    public KnowledgeSpaceBO findSpace(Long id) {
        return MapstructUtils.convert(spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getId, id)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)), KnowledgeSpaceBO.class);
    }

    public KnowledgeSpaceBO findSpaceByTenant(Long tenantId, Long id) {
        if (tenantId == null || id == null) {
            return null;
        }
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId)
                .eq(KnowledgeSpaceDO::getId, id)
                .eq(KnowledgeSpaceDO::getDelFlag, 0))), KnowledgeSpaceBO.class);
    }

    public List<KnowledgeSpaceBO> findActiveSpacesByTenant(Long tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> spaceMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId)
                .eq(KnowledgeSpaceDO::getStatus, 1)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)
                .orderBy(KnowledgeSpaceDO::getId, true))), KnowledgeSpaceBO.class);
    }

    /** Returns active spaces for backup manifests without inheriting the request tenant filter. */
    public List<KnowledgeSpaceBO> findAllActiveSpaces() {
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> spaceMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getStatus, 1)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)
                .orderBy(KnowledgeSpaceDO::getTenantId, true)
                .orderBy(KnowledgeSpaceDO::getId, true))), KnowledgeSpaceBO.class);
    }

    public KnowledgeSpaceBO findSpaceByCode(String code) {
        return MapstructUtils.convert(spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getCode, code)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)), KnowledgeSpaceBO.class);
    }

    /** Find a space by tenant while provisioning outside the current tenant context. */
    public KnowledgeSpaceBO findSpaceByTenantAndCode(Long tenantId, String code) {
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId)
                .eq(KnowledgeSpaceDO::getCode, code)
                .eq(KnowledgeSpaceDO::getDelFlag, 0))), KnowledgeSpaceBO.class);
    }

    public PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword) {
        return pageSpaces(pageNum, pageSize, keyword, null);
    }

    public PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword,
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
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeSpaceBO.class), page.getTotalRow());
    }

    public KnowledgeSpaceBO insertSpace(KnowledgeSpaceBO space) {
        KnowledgeSpaceDO data = MapstructUtils.convert(space, KnowledgeSpaceDO.class);
        spaceMapper.insert(data);
        space.setId(data.getId());
        return space;
    }

    public void updateSpace(KnowledgeSpaceBO space) {
        spaceMapper.update(MapstructUtils.convert(space, KnowledgeSpaceDO.class));
    }

    public void deleteSpace(Long id) {
        spaceMapper.deleteById(id);
    }

    public List<KnowledgeSpaceMemberBO> findMembers(Long spaceId) {
        return MapstructUtils.convert(memberMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0)
                .orderBy(KnowledgeSpaceMemberDO::getId, true)), KnowledgeSpaceMemberBO.class);
    }

    public void replaceMembers(Long spaceId, List<KnowledgeSpaceMemberBO> members) {
        memberMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId));
        if (!members.isEmpty()) {
            memberMapper.insertBatch(MapstructUtils.convert(members, KnowledgeSpaceMemberDO.class));
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

    public KnowledgeDocumentVersionBO findVersion(Long id) {
        return MapstructUtils.convert(versionMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getId, id)
                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0)), KnowledgeDocumentVersionBO.class);
    }

    public List<KnowledgeDocumentVersionBO> findVersions(Long documentId) {
        return MapstructUtils.convert(versionMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0)
                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false)), KnowledgeDocumentVersionBO.class);
    }

    public int nextVersionNo(Long documentId) {
        KnowledgeDocumentVersionDO latest = versionMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false)
                .limit(1));
        return latest == null ? 1 : latest.getVersionNo() + 1;
    }

    public KnowledgeDocumentVersionBO insertVersion(KnowledgeDocumentVersionBO version) {
        KnowledgeDocumentVersionDO data = MapstructUtils.convert(version, KnowledgeDocumentVersionDO.class);
        versionMapper.insert(data);
        version.setId(data.getId());
        return version;
    }

    public void updateVersion(KnowledgeDocumentVersionBO version) {
        versionMapper.update(MapstructUtils.convert(version, KnowledgeDocumentVersionDO.class));
    }

    public void insertReview(KnowledgeReviewRecordBO review) {
        KnowledgeReviewRecordDO data = MapstructUtils.convert(review, KnowledgeReviewRecordDO.class);
        reviewMapper.insert(data);
        review.setId(data.getId());
    }

    public KnowledgeIngestionJobBO findJob(Long id) {
        return MapstructUtils.convert(jobMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getId, id)
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)), KnowledgeIngestionJobBO.class);
    }

    public KnowledgeIngestionJobBO findJobByKey(String jobKey) {
        return MapstructUtils.convert(jobMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getJobKey, jobKey)
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)), KnowledgeIngestionJobBO.class);
    }

    public KnowledgeIngestionJobBO insertJob(KnowledgeIngestionJobBO job) {
        KnowledgeIngestionJobDO data = MapstructUtils.convert(job, KnowledgeIngestionJobDO.class);
        jobMapper.insert(data);
        job.setId(data.getId());
        return job;
    }

    public void updateJob(KnowledgeIngestionJobBO job) {
        jobMapper.update(MapstructUtils.convert(job, KnowledgeIngestionJobDO.class));
    }

    public PageData<KnowledgeIngestionJobBO> pageJobs(int pageNum, int pageSize,
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
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeIngestionJobBO.class), page.getTotalRow());
    }

    public List<KnowledgeIngestionJobBO> pollPendingJobs(int limit) {
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() ->
                jobMapper.selectListByQuery(QueryWrapper.create()
                        .eq(KnowledgeIngestionJobDO::getJobStatus, "PENDING")
                        .eq(KnowledgeIngestionJobDO::getDelFlag, 0)
                        .orderBy(KnowledgeIngestionJobDO::getCreateTime, true)
                        .limit(0, limit))), KnowledgeIngestionJobBO.class);
    }

    public List<KnowledgeIngestionJobBO> findStaleJobs(LocalDateTime heartbeatBefore) {
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() ->
                jobMapper.selectListByQuery(QueryWrapper.create()
                        .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                        .lt(KnowledgeIngestionJobDO::getHeartbeatTime, heartbeatBefore)
                        .eq(KnowledgeIngestionJobDO::getDelFlag, 0))), KnowledgeIngestionJobBO.class);
    }

    public void insertAudit(KnowledgeAuditLogBO audit) {
        KnowledgeAuditLogDO data = MapstructUtils.convert(audit, KnowledgeAuditLogDO.class);
        auditMapper.insert(data);
        audit.setId(data.getId());
    }

    public PageData<KnowledgeAuditLogBO> pageAudit(int pageNum, int pageSize, Long spaceId) {
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeAuditLogDO::getDelFlag, 0);
        if (spaceId != null) {
            query.eq(KnowledgeAuditLogDO::getSpaceId, spaceId);
        }
        Page<KnowledgeAuditLogDO> page = auditMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeAuditLogDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeAuditLogBO.class), page.getTotalRow());
    }

    public KnowledgeEvaluationCaseBO insertEvaluation(KnowledgeEvaluationCaseBO evaluation) {
        KnowledgeEvaluationCaseDO data = MapstructUtils.convert(evaluation, KnowledgeEvaluationCaseDO.class);
        evaluationMapper.insert(data);
        evaluation.setId(data.getId());
        return evaluation;
    }

    public PageData<KnowledgeEvaluationCaseBO> pageEvaluations(int pageNum, int pageSize,
                                                               Long spaceId) {
        Page<KnowledgeEvaluationCaseDO> page = evaluationMapper.paginate(pageNum, pageSize,
                QueryWrapper.create()
                        .eq(KnowledgeEvaluationCaseDO::getSpaceId, spaceId)
                        .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0)
                        .orderBy(KnowledgeEvaluationCaseDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeEvaluationCaseBO.class), page.getTotalRow());
    }

    public KnowledgeEvaluationCaseBO findEvaluation(Long id) {
        return MapstructUtils.convert(evaluationMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeEvaluationCaseDO::getId, id)
                .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0)), KnowledgeEvaluationCaseBO.class);
    }

    public void deleteEvaluation(Long id) {
        evaluationMapper.deleteById(id);
    }
}
