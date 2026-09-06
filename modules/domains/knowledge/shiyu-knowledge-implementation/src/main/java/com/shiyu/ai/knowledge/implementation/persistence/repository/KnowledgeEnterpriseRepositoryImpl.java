package com.shiyu.ai.knowledge.implementation.persistence.repository;

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
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeAuditLogDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentVersionDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeEvaluationCaseDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeIngestionJobDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeReviewRecordDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeSpaceDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeSpaceMemberDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeAuditLogMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocumentVersionMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeEvaluationCaseMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeIngestionJobMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeReviewRecordMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeSpaceMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeSpaceMemberMapper;
import com.shiyu.ai.kernel.context.TenantId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class KnowledgeEnterpriseRepositoryImpl implements com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository {

    private final KnowledgeSpaceMapper spaceMapper;
    private final KnowledgeSpaceMemberMapper memberMapper;
    private final KnowledgeDocumentVersionMapper versionMapper;
    private final KnowledgeReviewRecordMapper reviewMapper;
    private final KnowledgeIngestionJobMapper jobMapper;
    private final KnowledgeAuditLogMapper auditMapper;
    private final KnowledgeEvaluationCaseMapper evaluationMapper;

    public KnowledgeSpaceBO findSpace(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                .eq(KnowledgeSpaceDO::getId, id)
                .eq(KnowledgeSpaceDO::getDelFlag, 0)), KnowledgeSpaceBO.class);
    }

    public KnowledgeSpaceBO findSpaceByTenant(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        if (id == null) {
            return null;
        }
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                .eq(KnowledgeSpaceDO::getId, id)
                .eq(KnowledgeSpaceDO::getDelFlag, 0))), KnowledgeSpaceBO.class);
    }

    public List<KnowledgeSpaceBO> findActiveSpacesByTenant(TenantId tenantId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> spaceMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
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

    /** Find a space by tenant while provisioning outside the current tenant context. */
    public KnowledgeSpaceBO findSpaceByTenantAndCode(TenantId tenantId, String code) {
        requireTenant(tenantId);
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> spaceMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
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

    public PageData<KnowledgeSpaceBO> pageSpacesByTenant(TenantId tenantId, int pageNum, int pageSize,
                                                          String keyword, String domainCode) {
        requireTenant(tenantId);
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                .eq(KnowledgeSpaceDO::getDelFlag, 0);
        if (keyword != null && !keyword.isBlank()) {
            query.like(KnowledgeSpaceDO::getName, keyword);
        }
        if (domainCode != null && !domainCode.isBlank()) {
            query.eq(KnowledgeSpaceDO::getDomainCode, domainCode);
        }
        Page<KnowledgeSpaceDO> page = spaceMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeSpaceDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeSpaceBO.class),
                page.getTotalRow());
    }

    public KnowledgeSpaceBO insertSpace(TenantId tenantId, KnowledgeSpaceBO space) {
        requireTenant(tenantId);
        space.setTenantId(tenantId.value());
        KnowledgeSpaceDO data = MapstructUtils.convert(space, KnowledgeSpaceDO.class);
        requireAffected(spaceMapper.insert(data), "insert knowledge space");
        space.setId(data.getId());
        return space;
    }

    public void updateSpace(TenantId tenantId, KnowledgeSpaceBO space) {
        requireTenant(tenantId);
        space.setTenantId(tenantId.value());
        requireAffected(spaceMapper.updateByQuery(MapstructUtils.convert(space, KnowledgeSpaceDO.class),
                QueryWrapper.create().eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                        .eq(KnowledgeSpaceDO::getId, space.getId())), "update knowledge space");
    }

    public void deleteSpace(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        requireAffected(spaceMapper.deleteByQuery(QueryWrapper.create().eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                .eq(KnowledgeSpaceDO::getId, id)), "delete knowledge space");
    }

    public List<KnowledgeSpaceMemberBO> findMembers(TenantId tenantId, Long spaceId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(memberMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getTenantId, tenantId.value())
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0)
                .orderBy(KnowledgeSpaceMemberDO::getId, true)), KnowledgeSpaceMemberBO.class);
    }

    public void replaceMembers(TenantId tenantId, Long spaceId, List<KnowledgeSpaceMemberBO> members) {
        requireTenant(tenantId);
        memberMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getTenantId, tenantId.value())
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId));
        if (!members.isEmpty()) {
            members.forEach(member -> member.setTenantId(tenantId.value()));
            requireAffected(memberMapper.insertBatch(MapstructUtils.convert(members, KnowledgeSpaceMemberDO.class)),
                    "replace knowledge space members");
        }
    }

    public boolean hasMember(TenantId tenantId, Long spaceId, String principalType,
                             Long principalId, List<String> acceptedRoles) {
        requireTenant(tenantId);
        if (principalId == null) {
            return false;
        }
        return memberMapper.selectCountByQuery(QueryWrapper.create()
                .eq(KnowledgeSpaceMemberDO::getTenantId, tenantId.value())
                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                .eq(KnowledgeSpaceMemberDO::getPrincipalType, principalType)
                .eq(KnowledgeSpaceMemberDO::getPrincipalId, principalId)
                .in(KnowledgeSpaceMemberDO::getSpaceRole, acceptedRoles)
                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0)) > 0;
    }

    public KnowledgeDocumentVersionBO findVersion(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(versionMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocumentVersionDO::getId, id)
                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0)), KnowledgeDocumentVersionBO.class);
    }

    public List<KnowledgeDocumentVersionBO> findVersions(TenantId tenantId, Long documentId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(versionMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0)
                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false)), KnowledgeDocumentVersionBO.class);
    }

    public int nextVersionNo(TenantId tenantId, Long documentId) {
        requireTenant(tenantId);
        KnowledgeDocumentVersionDO latest = versionMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false)
                .limit(1));
        return latest == null ? 1 : latest.getVersionNo() + 1;
    }

    public KnowledgeDocumentVersionBO insertVersion(TenantId tenantId, KnowledgeDocumentVersionBO version) {
        requireTenant(tenantId);
        if (version == null) throw new IllegalArgumentException("version must not be null");
        version.setTenantId(tenantId.value());
        KnowledgeDocumentVersionDO data = MapstructUtils.convert(version, KnowledgeDocumentVersionDO.class);
        requireAffected(versionMapper.insert(data), "insert knowledge document version");
        version.setId(data.getId());
        return version;
    }

    public void updateVersion(TenantId tenantId, KnowledgeDocumentVersionBO version) {
        requireTenant(tenantId);
        if (version == null || version.getId() == null) {
            throw new IllegalArgumentException("version and id are required");
        }
        version.setTenantId(tenantId.value());
        requireAffected(versionMapper.updateByQuery(MapstructUtils.convert(version, KnowledgeDocumentVersionDO.class),
                QueryWrapper.create().eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                        .eq(KnowledgeDocumentVersionDO::getId, version.getId())), "update knowledge document version");
    }

    public void insertReview(TenantId tenantId, KnowledgeReviewRecordBO review) {
        requireTenant(tenantId);
        if (review == null) throw new IllegalArgumentException("review must not be null");
        review.setTenantId(tenantId.value());
        KnowledgeReviewRecordDO data = MapstructUtils.convert(review, KnowledgeReviewRecordDO.class);
        requireAffected(reviewMapper.insert(data), "insert knowledge review");
        review.setId(data.getId());
    }

    public KnowledgeIngestionJobBO findJob(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(jobMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                .eq(KnowledgeIngestionJobDO::getId, id)
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)), KnowledgeIngestionJobBO.class);
    }

    public KnowledgeIngestionJobBO findJobByKey(TenantId tenantId, String jobKey) {
        requireTenant(tenantId);
        return MapstructUtils.convert(jobMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                .eq(KnowledgeIngestionJobDO::getJobKey, jobKey)
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)), KnowledgeIngestionJobBO.class);
    }

    public KnowledgeIngestionJobBO insertJob(TenantId tenantId, KnowledgeIngestionJobBO job) {
        requireTenant(tenantId);
        if (job == null) throw new IllegalArgumentException("job must not be null");
        job.setTenantId(tenantId.value());
        KnowledgeIngestionJobDO data = MapstructUtils.convert(job, KnowledgeIngestionJobDO.class);
        requireAffected(jobMapper.insert(data), "insert knowledge ingestion job");
        job.setId(data.getId());
        return job;
    }

    public void updateJob(TenantId tenantId, KnowledgeIngestionJobBO job) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null) {
            throw new IllegalArgumentException("job and id are required");
        }
        job.setTenantId(tenantId.value());
        requireAffected(jobMapper.updateByQuery(MapstructUtils.convert(job, KnowledgeIngestionJobDO.class),
                QueryWrapper.create().eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                        .eq(KnowledgeIngestionJobDO::getId, job.getId())), "update knowledge ingestion job");
    }

    public PageData<KnowledgeIngestionJobBO> pageJobsByTenant(TenantId tenantId, int pageNum,
                                                               int pageSize, Long spaceId,
                                                               String status) {
        requireTenant(tenantId);
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                .eq(KnowledgeIngestionJobDO::getDelFlag, 0);
        if (spaceId != null) {
            query.eq(KnowledgeIngestionJobDO::getSpaceId, spaceId);
        }
        if (status != null && !status.isBlank()) {
            query.eq(KnowledgeIngestionJobDO::getJobStatus, status);
        }
        Page<KnowledgeIngestionJobDO> page = jobMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeIngestionJobDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeIngestionJobBO.class),
                page.getTotalRow());
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

    public void insertAudit(TenantId tenantId, KnowledgeAuditLogBO audit) {
        requireTenant(tenantId);
        if (audit == null) throw new IllegalArgumentException("audit must not be null");
        audit.setTenantId(tenantId.value());
        KnowledgeAuditLogDO data = MapstructUtils.convert(audit, KnowledgeAuditLogDO.class);
        requireAffected(auditMapper.insert(data), "insert knowledge audit log");
        audit.setId(data.getId());
    }

    public PageData<KnowledgeAuditLogBO> pageAudit(TenantId tenantId, int pageNum, int pageSize, Long spaceId) {
        requireTenant(tenantId);
        QueryWrapper query = QueryWrapper.create()
                .eq(KnowledgeAuditLogDO::getTenantId, tenantId.value())
                .eq(KnowledgeAuditLogDO::getDelFlag, 0);
        if (spaceId != null) {
            query.eq(KnowledgeAuditLogDO::getSpaceId, spaceId);
        }
        Page<KnowledgeAuditLogDO> page = auditMapper.paginate(pageNum, pageSize,
                query.orderBy(KnowledgeAuditLogDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeAuditLogBO.class), page.getTotalRow());
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
    }

    public KnowledgeEvaluationCaseBO insertEvaluation(TenantId tenantId, KnowledgeEvaluationCaseBO evaluation) {
        requireTenant(tenantId);
        evaluation.setTenantId(tenantId.value());
        KnowledgeEvaluationCaseDO data = MapstructUtils.convert(evaluation, KnowledgeEvaluationCaseDO.class);
        requireAffected(evaluationMapper.insert(data), "insert knowledge evaluation case");
        evaluation.setId(data.getId());
        return evaluation;
    }

    public PageData<KnowledgeEvaluationCaseBO> pageEvaluations(TenantId tenantId, int pageNum, int pageSize,
                                                               Long spaceId) {
        requireTenant(tenantId);
        Page<KnowledgeEvaluationCaseDO> page = evaluationMapper.paginate(pageNum, pageSize,
                QueryWrapper.create()
                        .eq(KnowledgeEvaluationCaseDO::getTenantId, tenantId.value())
                        .eq(KnowledgeEvaluationCaseDO::getSpaceId, spaceId)
                        .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0)
                        .orderBy(KnowledgeEvaluationCaseDO::getId, false));
        return new PageData<>(MapstructUtils.convert(page.getRecords(), KnowledgeEvaluationCaseBO.class), page.getTotalRow());
    }

    public KnowledgeEvaluationCaseBO findEvaluation(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(evaluationMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeEvaluationCaseDO::getTenantId, tenantId.value())
                .eq(KnowledgeEvaluationCaseDO::getId, id)
                .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0)), KnowledgeEvaluationCaseBO.class);
    }

    public void deleteEvaluation(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        requireAffected(evaluationMapper.deleteByQuery(QueryWrapper.create()
                .eq(KnowledgeEvaluationCaseDO::getTenantId, tenantId.value())
                .eq(KnowledgeEvaluationCaseDO::getId, id)), "delete knowledge evaluation case");
    }

    private static void requireAffected(int rows, String operation) {
        if (rows < 1) {
            throw new IllegalStateException(operation + " affected no rows");
        }
    }
}

