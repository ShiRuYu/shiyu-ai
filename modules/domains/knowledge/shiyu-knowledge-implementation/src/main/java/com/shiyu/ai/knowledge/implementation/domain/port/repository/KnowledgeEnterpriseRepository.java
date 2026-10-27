package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeEvaluationCaseBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeReviewRecordBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceMemberBO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 负责 知识 Enterprise 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeEnterpriseRepository {
    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeSpaceBO findSpace(TenantId tenantId, Long id);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeSpaceBO findSpaceByTenant(TenantId tenantId, Long id);

    /** 领取跨实例的持久化索引重建租约，并返回隔离版本。 */
    Long claimIndexRebuildLease(TenantId tenantId, Long spaceId);

    /** 代表持久化索引重建任务领取租约。 */
    default Long claimIndexRebuildLease(TenantId tenantId, Long spaceId, Long jobId) {
        return claimIndexRebuildLease(tenantId, spaceId);
    }

    /** 仅在隔离版本仍有效时刷新租约。 */
    boolean heartbeatIndexRebuildLease(
            TenantId tenantId, Long spaceId, Long fencingVersion, LocalDateTime heartbeatTime);


    /** 通过隔离版本保护索引激活，阻止持有过期或被替换租约的线程发布索引。 */
    boolean activateIndexRebuild(
            TenantId tenantId,
            Long spaceId,
            Long fencingVersion,
            Long expectedActiveVersion,
            Long newActiveVersion);

    /** 激活工作线程重建的索引，并与对应持久化任务的取消操作串行协调。 */
    boolean activateIndexRebuild(
            TenantId tenantId,
            Long spaceId,
            Long fencingVersion,
            Long expectedActiveVersion,
            Long newActiveVersion,
            Long jobId,
            Long jobLockVersion);

    /** 仅在隔离版本仍有效时结束租约。 */
    boolean releaseIndexRebuildLease(
            TenantId tenantId, Long spaceId, Long fencingVersion, boolean succeeded);


    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeSpaceBO> findActiveSpacesByTenant(TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeSpaceBO> findAllActiveSpaces();

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeSpaceBO findSpaceByTenantAndCode(TenantId tenantId, String code);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param domainCode 用于完成本次业务处理的 domainCode 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    PageData<KnowledgeSpaceBO> pageSpaces(
            int pageNum, int pageSize, String keyword, String domainCode);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param domainCode 用于完成本次业务处理的 domainCode 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    PageData<KnowledgeSpaceBO> pageSpacesByTenant(
            TenantId tenantId, int pageNum, int pageSize, String keyword, String domainCode);

    /**
     * 创建或保存 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param space 用于完成本次业务处理的 space 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeSpaceBO insertSpace(TenantId tenantId, KnowledgeSpaceBO space);

    /**
     * 更新或设置 知识 Enterprise 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param space 用于完成本次业务处理的 space 参数。
     */
    void updateSpace(TenantId tenantId, KnowledgeSpaceBO space);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     */
    void deleteSpace(TenantId tenantId, Long id);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeSpaceMemberBO> findMembers(TenantId tenantId, Long spaceId);

    /**
     * 执行 知识 Enterprise 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param members 用于完成本次业务处理的 members 参数。
     */
    void replaceMembers(TenantId tenantId, Long spaceId, List<KnowledgeSpaceMemberBO> members);

    /**
     * 校验或判断 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param principalType 用于完成本次业务处理的 principalType 参数。
     * @param principalId 用于定位principal的标识。
     * @param acceptedRoles 用于完成本次业务处理的 acceptedRoles 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean hasMember(
            TenantId tenantId,
            Long spaceId,
            String principalType,
            Long principalId,
            List<String> acceptedRoles);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeDocumentVersionBO findVersion(TenantId tenantId, Long id);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param documentId 用于定位document的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDocumentVersionBO> findVersions(TenantId tenantId, Long documentId);

    /**
     * 执行 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param documentId 用于定位document的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    int nextVersionNo(TenantId tenantId, Long documentId);

    /**
     * 创建或保存 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeDocumentVersionBO insertVersion(TenantId tenantId, KnowledgeDocumentVersionBO version);

    /**
     * 更新或设置 知识 Enterprise 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param version 用于完成本次业务处理的 version 参数。
     */
    void updateVersion(TenantId tenantId, KnowledgeDocumentVersionBO version);

    /**
     * 创建或保存 知识 Enterprise 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param review 用于完成本次业务处理的 review 参数。
     */
    void insertReview(TenantId tenantId, KnowledgeReviewRecordBO review);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeIngestionJobBO findJob(TenantId tenantId, Long id);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param jobKey 用于完成本次业务处理的 jobKey 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeIngestionJobBO findJobByKey(TenantId tenantId, String jobKey);

    /**
     * 创建或保存 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param job 用于完成本次业务处理的 job 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeIngestionJobBO insertJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /**
     * 更新或设置 知识 Enterprise 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param job 用于完成本次业务处理的 job 参数。
     */
    void updateJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    PageData<KnowledgeIngestionJobBO> pageJobsByTenant(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId, String status);

    /**
     * 执行 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeIngestionJobBO> pollPendingJobs(int limit);

    /** 原子领取摄取任务并递增租约版本。 */
    KnowledgeIngestionJobBO claimIngestionJob(TenantId tenantId, Long jobId);

    /** 只有租约版本和心跳都未变化时，才恢复超时摄取任务。 */
    boolean recoverStaleIngestionJob(
            TenantId tenantId, KnowledgeIngestionJobBO job, LocalDateTime heartbeatBefore);

    /** 仅当前运行租约可更新摄取任务状态。 */
    boolean updateClaimedIngestionJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /** 仅当前租约持有者刷新摄取任务心跳。 */
    boolean heartbeatIngestionJob(
            TenantId tenantId, Long jobId, Long lockVersion, LocalDateTime heartbeatTime);

    /** 在一个事务中提交当前租约对应的版本、文档和完成状态。 */
    boolean completeClaimedIngestion(
            TenantId tenantId,
            KnowledgeIngestionJobBO job,
            KnowledgeDocumentVersionBO version,
            com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO document,
            String parsedTitle);

    /** 仅针对仍有效的任务版本更新失败或重试状态。 */
    boolean failClaimedIngestion(
            TenantId tenantId, KnowledgeIngestionJobBO job, String documentParseStatus);

    /** 取消摄取任务并递增租约版本，使旧 Worker 立即失去写权限。 */
    boolean cancelIngestionJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param heartbeatBefore 用于完成本次业务处理的 heartbeatBefore 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeIngestionJobBO> findStaleJobs(LocalDateTime heartbeatBefore);

    /** 查询待处理的索引重建任务，供独立索引 Worker 领取。 */
    List<KnowledgeIngestionJobBO> pollPendingIndexRebuildJobs(int limit);

    /** 原子领取索引重建任务并递增租约版本。 */
    KnowledgeIngestionJobBO claimIndexRebuildJob(TenantId tenantId, Long jobId);

    /** 查询租约超时的索引重建任务。 */
    List<KnowledgeIngestionJobBO> findStaleIndexRebuildJobs(LocalDateTime heartbeatBefore);

    /** 仅当任务仍由预期租约持有时，恢复超时任务。 */
    boolean recoverStaleIndexRebuildJob(
            TenantId tenantId, KnowledgeIngestionJobBO job, LocalDateTime heartbeatBefore);

    /** 仅为当前租约持有者刷新索引重建任务心跳。 */
    boolean heartbeatIndexRebuildJob(
            TenantId tenantId, Long jobId, Long lockVersion, LocalDateTime heartbeatTime);

    /** 仅允许当前租约持有者写回索引重建任务结果。 */
    boolean updateIndexRebuildJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /** 取消待执行的重建任务，或使运行中的重建任务及其租约失效。 */
    boolean cancelIndexRebuildJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /** 检查重建任务是否仍持有预期的运行版本。 */
    boolean isIndexRebuildJobCurrent(TenantId tenantId, Long jobId, Long lockVersion);

    /** 仅在任务领取凭据仍有效时原子累计工作线程的执行次数。 */
    boolean beginIndexRebuildAttempt(TenantId tenantId, Long jobId, Long lockVersion);

    /**
     * 创建或保存 知识 Enterprise 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param audit 用于完成本次业务处理的 audit 参数。
     */
    void insertAudit(TenantId tenantId, KnowledgeAuditLogBO audit);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    PageData<KnowledgeAuditLogBO> pageAudit(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId);

    /**
     * 创建或保存 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param evaluation 用于完成本次业务处理的 evaluation 参数。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeEvaluationCaseBO insertEvaluation(
            TenantId tenantId, KnowledgeEvaluationCaseBO evaluation);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    PageData<KnowledgeEvaluationCaseBO> pageEvaluations(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId);

    /**
     * 查询 知识 Enterprise 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Enterprise 相关操作生成的结果数据。
     */
    KnowledgeEvaluationCaseBO findEvaluation(TenantId tenantId, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     */
    void deleteEvaluation(TenantId tenantId, Long id);
}
