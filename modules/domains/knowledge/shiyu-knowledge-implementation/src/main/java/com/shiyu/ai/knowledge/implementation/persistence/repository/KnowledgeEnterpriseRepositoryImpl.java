package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.mybatis.tenant.TenantQueryExecutor;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeEvaluationCaseBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeReviewRecordBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceMemberBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeAuditLogDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentVersionDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeEvaluationCaseDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeIngestionJobDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeReviewRecordDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeSpaceDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeSpaceMemberDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeAuditLogMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocumentVersionMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDocumentMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeEvaluationCaseMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeIngestionJobMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeReviewRecordMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeSpaceMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeSpaceMemberMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 负责 知识 Enterprise 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Repository
public class KnowledgeEnterpriseRepositoryImpl
        implements com.shiyu.ai.knowledge.implementation.domain.port.repository
                .KnowledgeEnterpriseRepository {

    /**
     * spaceMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceMapper spaceMapper;
    /**
     * memberMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceMemberMapper memberMapper;
    /**
     * 版本映射器，表示当前对象中的对应属性。
     */
    private final KnowledgeDocumentVersionMapper versionMapper;
    private final KnowledgeDocumentMapper documentMapper;
    /**
     * reviewMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeReviewRecordMapper reviewMapper;
    /**
     * jobMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeIngestionJobMapper jobMapper;
    /**
     * 审计映射器，表示当前对象中的对应属性。
     */
    private final KnowledgeAuditLogMapper auditMapper;
    /**
     * evaluationMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeEvaluationCaseMapper evaluationMapper;

    @Autowired
    public KnowledgeEnterpriseRepositoryImpl(
            KnowledgeSpaceMapper spaceMapper,
            KnowledgeSpaceMemberMapper memberMapper,
            KnowledgeDocumentVersionMapper versionMapper,
            KnowledgeDocumentMapper documentMapper,
            KnowledgeReviewRecordMapper reviewMapper,
            KnowledgeIngestionJobMapper jobMapper,
            KnowledgeAuditLogMapper auditMapper,
            KnowledgeEvaluationCaseMapper evaluationMapper) {
        this.spaceMapper = spaceMapper;
        this.memberMapper = memberMapper;
        this.versionMapper = versionMapper;
        this.documentMapper = documentMapper;
        this.reviewMapper = reviewMapper;
        this.jobMapper = jobMapper;
        this.auditMapper = auditMapper;
        this.evaluationMapper = evaluationMapper;
    }

    /** 保留文档映射器引入前的构造方式，供仓库单元测试夹具使用。 */
    public KnowledgeEnterpriseRepositoryImpl(
            KnowledgeSpaceMapper spaceMapper,
            KnowledgeSpaceMemberMapper memberMapper,
            KnowledgeDocumentVersionMapper versionMapper,
            KnowledgeReviewRecordMapper reviewMapper,
            KnowledgeIngestionJobMapper jobMapper,
            KnowledgeAuditLogMapper auditMapper,
            KnowledgeEvaluationCaseMapper evaluationMapper) {
        this(
                spaceMapper,
                memberMapper,
                versionMapper,
                null,
                reviewMapper,
                jobMapper,
                auditMapper,
                evaluationMapper);
    }

    public KnowledgeSpaceBO findSpace(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                spaceMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                                .eq(KnowledgeSpaceDO::getId, id)
                                .eq(KnowledgeSpaceDO::getDelFlag, 0)),
                KnowledgeSpaceBO.class);
    }

    public KnowledgeSpaceBO findSpaceByTenant(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        if (id == null) {
            return null;
        }
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                spaceMapper.selectOneByQuery(
                                        QueryWrapper.create()
                                                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                                                .eq(KnowledgeSpaceDO::getId, id)
                                                .eq(KnowledgeSpaceDO::getDelFlag, 0))),
                KnowledgeSpaceBO.class);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Long claimIndexRebuildLease(TenantId tenantId, Long spaceId) {
        return claimIndexRebuildLeaseInternal(tenantId, spaceId, null);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Long claimIndexRebuildLease(TenantId tenantId, Long spaceId, Long jobId) {
        if (jobId == null) return null;
        return claimIndexRebuildLeaseInternal(tenantId, spaceId, jobId);
    }

    private Long claimIndexRebuildLeaseInternal(TenantId tenantId, Long spaceId, Long jobId) {
        requireTenant(tenantId);
        if (spaceId == null) return null;
        if (spaceMapper.lockSpaceForIndexRebuild(tenantId.value(), spaceId) == null) return null;
        Long activeVersion = spaceMapper.activeIndexVersion(tenantId.value(), spaceId);
        if (activeVersion == null) return null;
        String leaseKey = "INDEX_REBUILD_LEASE:" + spaceId;
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(5);
        KnowledgeIngestionJobDO lease =
                jobMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getSpaceId, spaceId)
                                .eq(KnowledgeIngestionJobDO::getJobType, "INDEX_REBUILD_LEASE")
                                .eq(KnowledgeIngestionJobDO::getJobKey, leaseKey)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
        LocalDateTime now = LocalDateTime.now();
        if (lease == null) {
            KnowledgeIngestionJobDO created = new KnowledgeIngestionJobDO();
            created.setJobKey(leaseKey);
            created.setJobType("INDEX_REBUILD_LEASE");
            created.setTenantId(tenantId.value());
            created.setSpaceId(spaceId);
            created.setJobStatus("SUCCEEDED");
            created.setStage("IDLE");
            created.setProgress(100);
            created.setAttempts(0);
            created.setMaxAttempts(Integer.MAX_VALUE);
            created.setLockVersion(Math.max(0L, activeVersion));
            created.setDelFlag(0);
            if (jobMapper.insert(created) != 1) return null;
            lease = created;
        }
        lease.setTenantId(tenantId.value());
        if (jobId != null) lease.setCheckpointData(jobId.toString());
        long oldVersion = lease.getLockVersion() == null ? 0 : lease.getLockVersion();
        if ("RUNNING".equals(lease.getJobStatus())
                && lease.getHeartbeatTime() != null
                && !lease.getHeartbeatTime().isBefore(staleBefore)) return null;
        long newVersion = Math.max(oldVersion, activeVersion) + 1;
        int changed =
                jobMapper.claimIndexRebuildLease(
                        tenantId.value(),
                        lease.getId(),
                        leaseKey,
                        jobId == null ? Objects.toString(lease.getCheckpointData(), "") : jobId.toString(),
                        oldVersion,
                        newVersion,
                        now,
                        staleBefore);
        return changed == 1 ? newVersion : null;
    }

    @Override
    public boolean heartbeatIndexRebuildLease(
            TenantId tenantId, Long spaceId, Long fencingVersion, LocalDateTime heartbeatTime) {
        requireTenant(tenantId);
        return jobMapper.heartbeatIndexRebuildLease(
                        tenantId.value(),
                        spaceId,
                        "INDEX_REBUILD_LEASE:" + spaceId,
                        fencingVersion,
                        heartbeatTime)
                == 1;
    }


    @Override
    public boolean activateIndexRebuild(
            TenantId tenantId,
            Long spaceId,
            Long fencingVersion,
            Long expectedActiveVersion,
            Long newActiveVersion) {
        requireTenant(tenantId);
        LocalDateTime now = LocalDateTime.now();
        return spaceMapper.activateIndexRebuild(
                        tenantId.value(),
                        spaceId,
                        "INDEX_REBUILD_LEASE:" + spaceId,
                        fencingVersion,
                        now.minusSeconds(90),
                        expectedActiveVersion,
                        newActiveVersion,
                        null,
                        null)
                == 1;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean activateIndexRebuild(
            TenantId tenantId,
            Long spaceId,
            Long fencingVersion,
            Long expectedActiveVersion,
            Long newActiveVersion,
            Long jobId,
            Long jobLockVersion) {
        requireTenant(tenantId);
        if (jobMapper.lockCurrentIndexRebuildJob(tenantId.value(), jobId, jobLockVersion) == null) {
            return false;
        }
        if (jobMapper.markIndexRebuildActivating(tenantId.value(), jobId, jobLockVersion) != 1) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        int activated;
        try {
            activated =
                    spaceMapper.activateIndexRebuild(
                            tenantId.value(),
                            spaceId,
                            "INDEX_REBUILD_LEASE:" + spaceId,
                            fencingVersion,
                            now.minusSeconds(90),
                            expectedActiveVersion,
                            newActiveVersion,
                            jobId == null ? null : jobId.toString(),
                            jobLockVersion);
        } catch (RuntimeException activationFailure) {
            jobMapper.resetIndexRebuildActivation(
                    tenantId.value(), jobId, jobLockVersion);
            throw activationFailure;
        }
        if (activated != 1) {
            jobMapper.resetIndexRebuildActivation(
                    tenantId.value(), jobId, jobLockVersion);
            throw new IllegalStateException("Index rebuild lease was lost before activation");
        }
        return true;
    }

    @Override
    public boolean releaseIndexRebuildLease(
            TenantId tenantId, Long spaceId, Long fencingVersion, boolean succeeded) {
        requireTenant(tenantId);
        return jobMapper.releaseIndexRebuildLease(
                        tenantId.value(),
                        spaceId,
                        "INDEX_REBUILD_LEASE:" + spaceId,
                        fencingVersion,
                        LocalDateTime.now(),
                        succeeded)
                == 1;
    }

    public List<KnowledgeSpaceBO> findActiveSpacesByTenant(TenantId tenantId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                spaceMapper.selectListByQuery(
                                        QueryWrapper.create()
                                                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                                                .eq(KnowledgeSpaceDO::getStatus, 1)
                                                .eq(KnowledgeSpaceDO::getDelFlag, 0)
                                                .orderBy(KnowledgeSpaceDO::getId, true))),
                KnowledgeSpaceBO.class);
    }

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<KnowledgeSpaceBO> findAllActiveSpaces() {
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                spaceMapper.selectListByQuery(
                                        QueryWrapper.create()
                                                .eq(KnowledgeSpaceDO::getStatus, 1)
                                                .eq(KnowledgeSpaceDO::getDelFlag, 0)
                                                .orderBy(KnowledgeSpaceDO::getTenantId, true)
                                                .orderBy(KnowledgeSpaceDO::getId, true))),
                KnowledgeSpaceBO.class);
    }

    /**
     * 查询知识空间by租户编码。
     *
     * @param tenantId 租户标识。
     * @param code code 参数。
     *
     * @return 处理结果。
     */
    public KnowledgeSpaceBO findSpaceByTenantAndCode(TenantId tenantId, String code) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                spaceMapper.selectOneByQuery(
                                        QueryWrapper.create()
                                                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                                                .eq(KnowledgeSpaceDO::getCode, code)
                                                .eq(KnowledgeSpaceDO::getDelFlag, 0))),
                KnowledgeSpaceBO.class);
    }

    public PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword) {
        return pageSpaces(pageNum, pageSize, keyword, null);
    }

    public PageData<KnowledgeSpaceBO> pageSpaces(
            int pageNum, int pageSize, String keyword, String domainCode) {
        QueryWrapper query = QueryWrapper.create().eq(KnowledgeSpaceDO::getDelFlag, 0);
        if (keyword != null && !keyword.isBlank()) {
            query.like(KnowledgeSpaceDO::getName, keyword);
        }
        if (domainCode != null && !domainCode.isBlank()) {
            query.eq(KnowledgeSpaceDO::getDomainCode, domainCode);
        }
        Page<KnowledgeSpaceDO> page =
                spaceMapper.paginate(
                        pageNum, pageSize, query.orderBy(KnowledgeSpaceDO::getId, false));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), KnowledgeSpaceBO.class),
                page.getTotalRow());
    }

    public PageData<KnowledgeSpaceBO> pageSpacesByTenant(
            TenantId tenantId, int pageNum, int pageSize, String keyword, String domainCode) {
        requireTenant(tenantId);
        QueryWrapper query =
                QueryWrapper.create()
                        .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                        .eq(KnowledgeSpaceDO::getDelFlag, 0);
        if (keyword != null && !keyword.isBlank()) {
            query.like(KnowledgeSpaceDO::getName, keyword);
        }
        if (domainCode != null && !domainCode.isBlank()) {
            query.eq(KnowledgeSpaceDO::getDomainCode, domainCode);
        }
        Page<KnowledgeSpaceDO> page =
                spaceMapper.paginate(
                        pageNum, pageSize, query.orderBy(KnowledgeSpaceDO::getId, false));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), KnowledgeSpaceBO.class),
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
        requireAffected(
                spaceMapper.updateByQuery(
                        MapstructUtils.convert(space, KnowledgeSpaceDO.class),
                        QueryWrapper.create()
                                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                                .eq(KnowledgeSpaceDO::getId, space.getId())),
                "update knowledge space");
    }

    public void deleteSpace(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        requireAffected(
                spaceMapper.deleteByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeSpaceDO::getTenantId, tenantId.value())
                                .eq(KnowledgeSpaceDO::getId, id)),
                "delete knowledge space");
    }

    public List<KnowledgeSpaceMemberBO> findMembers(TenantId tenantId, Long spaceId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                memberMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeSpaceMemberDO::getTenantId, tenantId.value())
                                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0)
                                .orderBy(KnowledgeSpaceMemberDO::getId, true)),
                KnowledgeSpaceMemberBO.class);
    }

    public void replaceMembers(
            TenantId tenantId, Long spaceId, List<KnowledgeSpaceMemberBO> members) {
        requireTenant(tenantId);
        memberMapper.deleteByQuery(
                QueryWrapper.create()
                        .eq(KnowledgeSpaceMemberDO::getTenantId, tenantId.value())
                        .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId));
        if (!members.isEmpty()) {
            members.forEach(member -> member.setTenantId(tenantId.value()));
            requireAffected(
                    memberMapper.insertBatch(
                            MapstructUtils.convert(members, KnowledgeSpaceMemberDO.class)),
                    "replace knowledge space members");
        }
    }

    public boolean hasMember(
            TenantId tenantId,
            Long spaceId,
            String principalType,
            Long principalId,
            List<String> acceptedRoles) {
        requireTenant(tenantId);
        if (principalId == null) {
            return false;
        }
        return memberMapper.selectCountByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeSpaceMemberDO::getTenantId, tenantId.value())
                                .eq(KnowledgeSpaceMemberDO::getSpaceId, spaceId)
                                .eq(KnowledgeSpaceMemberDO::getPrincipalType, principalType)
                                .eq(KnowledgeSpaceMemberDO::getPrincipalId, principalId)
                                .in(KnowledgeSpaceMemberDO::getSpaceRole, acceptedRoles)
                                .eq(KnowledgeSpaceMemberDO::getDelFlag, 0))
                > 0;
    }

    public KnowledgeDocumentVersionBO findVersion(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                versionMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDocumentVersionDO::getId, id)
                                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0)),
                KnowledgeDocumentVersionBO.class);
    }

    public List<KnowledgeDocumentVersionBO> findVersions(TenantId tenantId, Long documentId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                versionMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                                .eq(KnowledgeDocumentVersionDO::getDelFlag, 0)
                                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false)),
                KnowledgeDocumentVersionBO.class);
    }

    public int nextVersionNo(TenantId tenantId, Long documentId) {
        requireTenant(tenantId);
        KnowledgeDocumentVersionDO latest =
                versionMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDocumentVersionDO::getDocumentId, documentId)
                                .orderBy(KnowledgeDocumentVersionDO::getVersionNo, false)
                                .limit(1));
        return latest == null ? 1 : latest.getVersionNo() + 1;
    }

    public KnowledgeDocumentVersionBO insertVersion(
            TenantId tenantId, KnowledgeDocumentVersionBO version) {
        requireTenant(tenantId);
        if (version == null) throw new IllegalArgumentException("version must not be null");
        version.setTenantId(tenantId.value());
        KnowledgeDocumentVersionDO data =
                MapstructUtils.convert(version, KnowledgeDocumentVersionDO.class);
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
        requireAffected(
                versionMapper.updateByQuery(
                        MapstructUtils.convert(version, KnowledgeDocumentVersionDO.class),
                        QueryWrapper.create()
                                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDocumentVersionDO::getId, version.getId())),
                "update knowledge document version");
    }

    public void insertReview(TenantId tenantId, KnowledgeReviewRecordBO review) {
        requireTenant(tenantId);
        if (review == null) throw new IllegalArgumentException("review must not be null");
        review.setTenantId(tenantId.value());
        KnowledgeReviewRecordDO data =
                MapstructUtils.convert(review, KnowledgeReviewRecordDO.class);
        requireAffected(reviewMapper.insert(data), "insert knowledge review");
        review.setId(data.getId());
    }

    public KnowledgeIngestionJobBO findJob(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                jobMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, id)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)),
                KnowledgeIngestionJobBO.class);
    }

    public KnowledgeIngestionJobBO findJobByKey(TenantId tenantId, String jobKey) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                jobMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getJobKey, jobKey)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)),
                KnowledgeIngestionJobBO.class);
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
        requireAffected(
                jobMapper.updateByQuery(
                        MapstructUtils.convert(job, KnowledgeIngestionJobDO.class),
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())),
                "update knowledge ingestion job");
    }

    @Override
    public KnowledgeIngestionJobBO claimIngestionJob(TenantId tenantId, Long jobId) {
        requireTenant(tenantId);
        KnowledgeIngestionJobBO job = findJob(tenantId, jobId);
        if (job == null || !"INGEST".equals(job.getJobType())
                || !"PENDING".equals(job.getJobStatus())) return null;
        long currentVersion = job.getLockVersion() == null ? 0L : job.getLockVersion();
        LocalDateTime now = LocalDateTime.now();
        KnowledgeIngestionJobDO update = new KnowledgeIngestionJobDO();
        update.setJobStatus("RUNNING");
        update.setStage("STARTING");
        update.setProgress(1);
        update.setAttempts((job.getAttempts() == null ? 0 : job.getAttempts()) + 1);
        update.setErrorMessage(null);
        update.setStartedTime(now);
        update.setHeartbeatTime(now);
        update.setFinishedTime(null);
        update.setLockVersion(currentVersion + 1);
        int affected =
                jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, jobId)
                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "PENDING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, currentVersion)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
        if (affected != 1) return null;
        job.setJobStatus("RUNNING");
        job.setStage("STARTING");
        job.setProgress(1);
        job.setAttempts(update.getAttempts());
        job.setErrorMessage(null);
        job.setStartedTime(now);
        job.setHeartbeatTime(now);
        job.setFinishedTime(null);
        job.setLockVersion(currentVersion + 1);
        return job;
    }

    @Override
    public boolean isIndexRebuildJobCurrent(
            TenantId tenantId, Long jobId, Long lockVersion) {
        requireTenant(tenantId);
        if (jobId == null || lockVersion == null) return false;
        return jobMapper.currentIndexRebuildJob(tenantId.value(), jobId, lockVersion) != null;
    }

    @Override
    public boolean beginIndexRebuildAttempt(TenantId tenantId, Long jobId, Long lockVersion) {
        requireTenant(tenantId);
        return jobMapper.beginIndexRebuildAttempt(
                        tenantId.value(), jobId, lockVersion, LocalDateTime.now())
                == 1;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean cancelIndexRebuildJob(TenantId tenantId, KnowledgeIngestionJobBO job) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null) return false;
        long lockVersion = job.getLockVersion() == null ? 0L : job.getLockVersion();
        if ("RUNNING".equals(job.getJobStatus())
                && jobMapper.lockCurrentIndexRebuildJob(
                                tenantId.value(), job.getId(), lockVersion)
                        == null) {
            return false;
        }
        int changed = jobMapper.cancelIndexRebuildJob(
                tenantId.value(), job.getId(), lockVersion, LocalDateTime.now());
        if (changed != 1) return false;
        if ("RUNNING".equals(job.getJobStatus()) && job.getSpaceId() != null) {
            jobMapper.fenceRunningIndexRebuildLease(
                    tenantId.value(),
                    job.getSpaceId(),
                    "INDEX_REBUILD_LEASE:" + job.getSpaceId(),
                    job.getId().toString(),
                    LocalDateTime.now());
        }
        return true;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean recoverStaleIngestionJob(
            TenantId tenantId, KnowledgeIngestionJobBO job, LocalDateTime heartbeatBefore) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null || job.getLockVersion() == null) return false;
        boolean retry = job.getAttempts() < job.getMaxAttempts();
        LocalDateTime now = LocalDateTime.now();
        KnowledgeIngestionJobDO update = new KnowledgeIngestionJobDO();
        update.setJobStatus(retry ? "PENDING" : "FAILED");
        update.setStage(retry ? "RECOVERED" : "FAILED");
        update.setErrorMessage("任务租约超时，等待恢复");
        update.setHeartbeatTime(now);
        update.setFinishedTime(retry ? null : now);
        update.setLockVersion(job.getLockVersion() + 1);
        int changed =
                jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())
                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, job.getLockVersion())
                                .lt(KnowledgeIngestionJobDO::getHeartbeatTime, heartbeatBefore)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
        if (changed != 1 || retry || job.getDocumentId() == null || job.getVersionId() == null) {
            return changed == 1;
        }
        KnowledgeDocumentDO documentUpdate = new KnowledgeDocumentDO();
        documentUpdate.setParseStatus("FAILED");
        documentMapper.updateByQuery(
                documentUpdate,
                QueryWrapper.create()
                        .eq(KnowledgeDocumentDO::getTenantId, tenantId.value())
                        .eq(KnowledgeDocumentDO::getId, job.getDocumentId())
                        .eq(KnowledgeDocumentDO::getCurrentVersionId, job.getVersionId()));
        KnowledgeDocumentVersionDO versionUpdate = new KnowledgeDocumentVersionDO();
        versionUpdate.setParseStatus("FAILED");
        versionMapper.updateByQuery(
                versionUpdate,
                QueryWrapper.create()
                        .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                        .eq(KnowledgeDocumentVersionDO::getId, job.getVersionId())
                        .eq(KnowledgeDocumentVersionDO::getDocumentId, job.getDocumentId()));
        return true;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean heartbeatIngestionJob(
            TenantId tenantId, Long jobId, Long lockVersion, LocalDateTime heartbeatTime) {
        requireTenant(tenantId);
        KnowledgeIngestionJobBO job = findJob(tenantId, jobId);
        if (job == null || job.getLockVersion() == null
                || !job.getLockVersion().equals(lockVersion)) return false;
        if (jobMapper.lockClaimedIngestionJob(
                        tenantId.value(), jobId, job.getDocumentId(), job.getVersionId(), lockVersion)
                == null) return false;
        KnowledgeIngestionJobDO update = new KnowledgeIngestionJobDO();
        update.setHeartbeatTime(heartbeatTime);
        return jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, jobId)
                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, lockVersion)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0))
                == 1;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean updateClaimedIngestionJob(TenantId tenantId, KnowledgeIngestionJobBO job) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null || job.getLockVersion() == null) return false;
        if (jobMapper.lockClaimedIngestionJob(
                        tenantId.value(),
                        job.getId(),
                        job.getDocumentId(),
                        job.getVersionId(),
                        job.getLockVersion())
                == null) return false;
        job.setTenantId(tenantId.value());
        return jobMapper.updateByQuery(
                        MapstructUtils.convert(job, KnowledgeIngestionJobDO.class),
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())
                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, job.getLockVersion())
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0))
                == 1;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean failClaimedIngestion(
            TenantId tenantId, KnowledgeIngestionJobBO job, String documentParseStatus) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null || job.getLockVersion() == null) return false;
        if (jobMapper.lockClaimedIngestionJob(
                        tenantId.value(),
                        job.getId(),
                        job.getDocumentId(),
                        job.getVersionId(),
                        job.getLockVersion())
                == null) return false;
        KnowledgeIngestionJobDO update = MapstructUtils.convert(job, KnowledgeIngestionJobDO.class);
        int changed =
                jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())
                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, job.getLockVersion())
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
        if (changed != 1) return false;
        com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO documentUpdate =
                new com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO();
        documentUpdate.setParseStatus(documentParseStatus);
        documentMapper.updateByQuery(
                documentUpdate,
                QueryWrapper.create()
                        .eq(KnowledgeDocumentDO::getTenantId, tenantId.value())
                        .eq(KnowledgeDocumentDO::getId, job.getDocumentId())
                        .eq(KnowledgeDocumentDO::getCurrentVersionId, job.getVersionId()));
        return true;
    }

    @Override
    public boolean cancelIngestionJob(TenantId tenantId, KnowledgeIngestionJobBO job) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null) return false;
        KnowledgeIngestionJobDO update = new KnowledgeIngestionJobDO();
        update.setJobStatus("CANCELLED");
        update.setStage("SUPERSEDED".equals(job.getStage()) ? "SUPERSEDED" : "CANCELLED");
        update.setFinishedTime(LocalDateTime.now());
        update.setLockVersion((job.getLockVersion() == null ? 0L : job.getLockVersion()) + 1);
        return jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())
                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                .eq(KnowledgeIngestionJobDO::getLockVersion,
                                        job.getLockVersion() == null ? 0L : job.getLockVersion())
                                .in(KnowledgeIngestionJobDO::getJobStatus, "PENDING", "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0))
                == 1;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean completeClaimedIngestion(
            TenantId tenantId,
            KnowledgeIngestionJobBO job,
            KnowledgeDocumentVersionBO version,
            com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO document,
            String parsedTitle) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null || job.getLockVersion() == null
                || version == null || document == null) return false;
        if (jobMapper.lockClaimedIngestionJob(
                        tenantId.value(),
                        job.getId(),
                        job.getDocumentId(),
                        job.getVersionId(),
                        job.getLockVersion())
                == null) return false;
        com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO docUpdate =
                new com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDocumentDO();
        docUpdate.setContent(document.getContent());
        docUpdate.setParseStatus(document.getParseStatus());
        requireAffected(
                documentMapper.updateByQuery(
                        docUpdate,
                        QueryWrapper.create()
                                .eq(KnowledgeDocumentDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDocumentDO::getId, document.getId())
                                .eq(KnowledgeDocumentDO::getCurrentVersionId, version.getId())),
                "publish ingested document content");
        if (parsedTitle != null && !parsedTitle.isBlank()
                && (version.getTitle() == null || version.getTitle().isBlank())) {
            version.setTitle(parsedTitle);
        }
        requireAffected(
                versionMapper.updateByQuery(
                        MapstructUtils.convert(version, KnowledgeDocumentVersionDO.class),
                        QueryWrapper.create()
                                .eq(KnowledgeDocumentVersionDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDocumentVersionDO::getId, version.getId())
                                .eq(KnowledgeDocumentVersionDO::getDocumentId, job.getDocumentId())),
                "publish ingested document version");
        KnowledgeIngestionJobDO completed = new KnowledgeIngestionJobDO();
        completed.setJobStatus("SUCCEEDED");
        completed.setStage("COMPLETED");
        completed.setProgress(100);
        completed.setHeartbeatTime(LocalDateTime.now());
        completed.setFinishedTime(LocalDateTime.now());
        requireAffected(
                jobMapper.updateByQuery(
                        completed,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())
                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, job.getLockVersion())
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)),
                "complete claimed ingestion job");
        job.setJobStatus("SUCCEEDED");
        job.setStage("COMPLETED");
        job.setProgress(100);
        job.setHeartbeatTime(completed.getHeartbeatTime());
        job.setFinishedTime(completed.getFinishedTime());
        return true;
    }

    public PageData<KnowledgeIngestionJobBO> pageJobsByTenant(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId, String status) {
        requireTenant(tenantId);
        QueryWrapper query =
                QueryWrapper.create()
                        .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                        .eq(KnowledgeIngestionJobDO::getDelFlag, 0);
        if (spaceId != null) {
            query.eq(KnowledgeIngestionJobDO::getSpaceId, spaceId);
        }
        if (status != null && !status.isBlank()) {
            query.eq(KnowledgeIngestionJobDO::getJobStatus, status);
        }
        Page<KnowledgeIngestionJobDO> page =
                jobMapper.paginate(
                        pageNum, pageSize, query.orderBy(KnowledgeIngestionJobDO::getId, false));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), KnowledgeIngestionJobBO.class),
                page.getTotalRow());
    }

    public List<KnowledgeIngestionJobBO> pollPendingJobs(int limit) {
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                jobMapper.selectListByQuery(
                                        QueryWrapper.create()
                                                .eq(
                                                        KnowledgeIngestionJobDO::getJobStatus,
                                                        "PENDING")
                                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)
                                                .orderBy(
                                                        KnowledgeIngestionJobDO::getCreateTime,
                                                        true)
                                                .limit(0, limit))),
                KnowledgeIngestionJobBO.class);
    }

    public List<KnowledgeIngestionJobBO> findStaleJobs(LocalDateTime heartbeatBefore) {
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                jobMapper.selectListByQuery(
                                        QueryWrapper.create()
                                                .eq(
                                                        KnowledgeIngestionJobDO::getJobStatus,
                                                        "RUNNING")
                                                .eq(KnowledgeIngestionJobDO::getJobType, "INGEST")
                                                .lt(
                                                        KnowledgeIngestionJobDO::getHeartbeatTime,
                                                        heartbeatBefore)
                                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0))),
                KnowledgeIngestionJobBO.class);
    }

    @Override
    public List<KnowledgeIngestionJobBO> pollPendingIndexRebuildJobs(int limit) {
        if (limit < 1) return List.of();
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                jobMapper.selectListByQuery(
                                        QueryWrapper.create()
                                                .eq(
                                                        KnowledgeIngestionJobDO::getJobType,
                                                        "INDEX_REBUILD")
                                                .eq(
                                                        KnowledgeIngestionJobDO::getJobStatus,
                                                        "PENDING")
                                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0)
                                                .orderBy(
                                                        KnowledgeIngestionJobDO::getCreateTime,
                                                        true)
                                                .limit(0, Math.min(limit, 100)))),
                KnowledgeIngestionJobBO.class);
    }

    @Override
    public KnowledgeIngestionJobBO claimIndexRebuildJob(TenantId tenantId, Long jobId) {
        requireTenant(tenantId);
        KnowledgeIngestionJobBO job = findJob(tenantId, jobId);
        if (job == null || !"INDEX_REBUILD".equals(job.getJobType())
                || !"PENDING".equals(job.getJobStatus())) return null;
        long currentVersion = job.getLockVersion() == null ? 0L : job.getLockVersion();
        long nextVersion = currentVersion + 1;
        LocalDateTime now = LocalDateTime.now();
        KnowledgeIngestionJobDO update = new KnowledgeIngestionJobDO();
        update.setJobStatus("RUNNING");
        update.setStage("BUILDING");
        update.setProgress(1);
        update.setErrorMessage(null);
        update.setStartedTime(now);
        update.setHeartbeatTime(now);
        update.setFinishedTime(null);
        update.setLockVersion(nextVersion);
        int affected =
                jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, jobId)
                                .eq(KnowledgeIngestionJobDO::getJobType, "INDEX_REBUILD")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "PENDING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, currentVersion)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
        if (affected != 1) return null;
        job.setJobStatus("RUNNING");
        job.setStage("BUILDING");
        job.setProgress(1);
        job.setErrorMessage(null);
        job.setStartedTime(now);
        job.setHeartbeatTime(now);
        job.setFinishedTime(null);
        job.setLockVersion(nextVersion);
        return job;
    }

    @Override
    public List<KnowledgeIngestionJobBO> findStaleIndexRebuildJobs(
            LocalDateTime heartbeatBefore) {
        return MapstructUtils.convert(
                TenantQueryExecutor.readAcrossTenants(
                        () ->
                                jobMapper.selectListByQuery(
                                        QueryWrapper.create()
                                                .eq(
                                                        KnowledgeIngestionJobDO::getJobType,
                                                        "INDEX_REBUILD")
                                                .eq(
                                                        KnowledgeIngestionJobDO::getJobStatus,
                                                        "RUNNING")
                                                .lt(
                                                        KnowledgeIngestionJobDO::getHeartbeatTime,
                                                        heartbeatBefore)
                                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0))),
                KnowledgeIngestionJobBO.class);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean recoverStaleIndexRebuildJob(
            TenantId tenantId, KnowledgeIngestionJobBO job, LocalDateTime heartbeatBefore) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null) return false;
        long currentVersion = job.getLockVersion() == null ? 0L : job.getLockVersion();
        int attempts = job.getAttempts() == null ? 0 : job.getAttempts();
        int maxAttempts = job.getMaxAttempts() == null ? 3 : job.getMaxAttempts();
        boolean retry = attempts < maxAttempts;
        LocalDateTime now = LocalDateTime.now();
        KnowledgeIngestionJobDO update = new KnowledgeIngestionJobDO();
        update.setJobStatus(retry ? "PENDING" : "FAILED");
        update.setStage(retry ? "RECOVERED" : "FAILED");
        update.setErrorMessage("索引任务租约超时，等待恢复");
        update.setHeartbeatTime(now);
        update.setFinishedTime(retry ? null : now);
        update.setLockVersion(currentVersion + 1);
        int changed =
                jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())
                                .eq(KnowledgeIngestionJobDO::getJobType, "INDEX_REBUILD")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, currentVersion)
                                .lt(KnowledgeIngestionJobDO::getHeartbeatTime, heartbeatBefore)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0));
        if (changed != 1) return false;
        if (job.getSpaceId() != null) {
            jobMapper.fenceRunningIndexRebuildLease(
                    tenantId.value(),
                    job.getSpaceId(),
                    "INDEX_REBUILD_LEASE:" + job.getSpaceId(),
                    job.getId().toString(),
                    now);
        }
        return true;
    }

    @Override
    public boolean heartbeatIndexRebuildJob(
            TenantId tenantId, Long jobId, Long lockVersion, LocalDateTime heartbeatTime) {
        requireTenant(tenantId);
        KnowledgeIngestionJobDO update = new KnowledgeIngestionJobDO();
        update.setHeartbeatTime(heartbeatTime);
        return jobMapper.updateByQuery(
                        update,
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, jobId)
                                .eq(KnowledgeIngestionJobDO::getJobType, "INDEX_REBUILD")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, lockVersion)
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0))
                == 1;
    }

    @Override
    public boolean updateIndexRebuildJob(TenantId tenantId, KnowledgeIngestionJobBO job) {
        requireTenant(tenantId);
        if (job == null || job.getId() == null || job.getLockVersion() == null) return false;
        job.setTenantId(tenantId.value());
        return jobMapper.updateByQuery(
                        MapstructUtils.convert(job, KnowledgeIngestionJobDO.class),
                        QueryWrapper.create()
                                .eq(KnowledgeIngestionJobDO::getTenantId, tenantId.value())
                                .eq(KnowledgeIngestionJobDO::getId, job.getId())
                                .eq(KnowledgeIngestionJobDO::getJobType, "INDEX_REBUILD")
                                .eq(KnowledgeIngestionJobDO::getJobStatus, "RUNNING")
                                .eq(KnowledgeIngestionJobDO::getLockVersion, job.getLockVersion())
                                .eq(KnowledgeIngestionJobDO::getDelFlag, 0))
                == 1;
    }

    public void insertAudit(TenantId tenantId, KnowledgeAuditLogBO audit) {
        requireTenant(tenantId);
        if (audit == null) throw new IllegalArgumentException("audit must not be null");
        audit.setTenantId(tenantId.value());
        KnowledgeAuditLogDO data = MapstructUtils.convert(audit, KnowledgeAuditLogDO.class);
        requireAffected(auditMapper.insert(data), "insert knowledge audit log");
        audit.setId(data.getId());
    }

    public PageData<KnowledgeAuditLogBO> pageAudit(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId) {
        requireTenant(tenantId);
        QueryWrapper query =
                QueryWrapper.create()
                        .eq(KnowledgeAuditLogDO::getTenantId, tenantId.value())
                        .eq(KnowledgeAuditLogDO::getDelFlag, 0);
        if (spaceId != null) {
            query.eq(KnowledgeAuditLogDO::getSpaceId, spaceId);
        }
        Page<KnowledgeAuditLogDO> page =
                auditMapper.paginate(
                        pageNum, pageSize, query.orderBy(KnowledgeAuditLogDO::getId, false));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), KnowledgeAuditLogBO.class),
                page.getTotalRow());
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        TenantScope.requireMatches(tenantId);
    }

    public KnowledgeEvaluationCaseBO insertEvaluation(
            TenantId tenantId, KnowledgeEvaluationCaseBO evaluation) {
        requireTenant(tenantId);
        evaluation.setTenantId(tenantId.value());
        KnowledgeEvaluationCaseDO data =
                MapstructUtils.convert(evaluation, KnowledgeEvaluationCaseDO.class);
        requireAffected(evaluationMapper.insert(data), "insert knowledge evaluation case");
        evaluation.setId(data.getId());
        return evaluation;
    }

    public PageData<KnowledgeEvaluationCaseBO> pageEvaluations(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId) {
        requireTenant(tenantId);
        Page<KnowledgeEvaluationCaseDO> page =
                evaluationMapper.paginate(
                        pageNum,
                        pageSize,
                        QueryWrapper.create()
                                .eq(KnowledgeEvaluationCaseDO::getTenantId, tenantId.value())
                                .eq(KnowledgeEvaluationCaseDO::getSpaceId, spaceId)
                                .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0)
                                .orderBy(KnowledgeEvaluationCaseDO::getId, false));
        return new PageData<>(
                MapstructUtils.convert(page.getRecords(), KnowledgeEvaluationCaseBO.class),
                page.getTotalRow());
    }

    public KnowledgeEvaluationCaseBO findEvaluation(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                evaluationMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeEvaluationCaseDO::getTenantId, tenantId.value())
                                .eq(KnowledgeEvaluationCaseDO::getId, id)
                                .eq(KnowledgeEvaluationCaseDO::getDelFlag, 0)),
                KnowledgeEvaluationCaseBO.class);
    }

    public void deleteEvaluation(TenantId tenantId, Long id) {
        requireTenant(tenantId);
        requireAffected(
                evaluationMapper.deleteByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeEvaluationCaseDO::getTenantId, tenantId.value())
                                .eq(KnowledgeEvaluationCaseDO::getId, id)),
                "delete knowledge evaluation case");
    }

    private static void requireAffected(int rows, String operation) {
        if (rows < 1) {
            throw new IllegalStateException(operation + " affected no rows");
        }
    }
}
