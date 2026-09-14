package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.common.core.api.PageData;
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
 * KnowledgeEnterpriseRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeEnterpriseRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    KnowledgeSpaceBO findSpace(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    KnowledgeSpaceBO findSpaceByTenant(TenantId tenantId, Long id);

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
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param code 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeSpaceBO findSpaceByTenantAndCode(TenantId tenantId, String code);

    /**
     * 执行 {@code pageSpaces} 定义的接口操作。
     *
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeSpaceBO> pageSpaces(int pageNum, int pageSize, String keyword);

    /**
     * 执行 {@code pageSpaces} 定义的接口操作。
     *
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     * @param domainCode 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeSpaceBO> pageSpaces(
            int pageNum, int pageSize, String keyword, String domainCode);

    /**
     * 执行 {@code pageSpacesByTenant} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     * @param domainCode 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeSpaceBO> pageSpacesByTenant(
            TenantId tenantId, int pageNum, int pageSize, String keyword, String domainCode);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param space 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeSpaceBO insertSpace(TenantId tenantId, KnowledgeSpaceBO space);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param space 方法参数。
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
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeSpaceMemberBO> findMembers(TenantId tenantId, Long spaceId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param members 方法参数。
     */
    void replaceMembers(TenantId tenantId, Long spaceId, List<KnowledgeSpaceMemberBO> members);

    /**
     * 判断当前条件是否满足。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param principalType 方法参数。
     * @param principalId 方法参数。
     * @param acceptedRoles 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean hasMember(
            TenantId tenantId,
            Long spaceId,
            String principalType,
            Long principalId,
            List<String> acceptedRoles);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    KnowledgeDocumentVersionBO findVersion(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param documentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDocumentVersionBO> findVersions(TenantId tenantId, Long documentId);

    /**
     * 执行 {@code nextVersionNo} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param documentId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int nextVersionNo(TenantId tenantId, Long documentId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param version 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeDocumentVersionBO insertVersion(TenantId tenantId, KnowledgeDocumentVersionBO version);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param version 方法参数。
     */
    void updateVersion(TenantId tenantId, KnowledgeDocumentVersionBO version);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param review 方法参数。
     */
    void insertReview(TenantId tenantId, KnowledgeReviewRecordBO review);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    KnowledgeIngestionJobBO findJob(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param jobKey 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeIngestionJobBO findJobByKey(TenantId tenantId, String jobKey);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param job 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeIngestionJobBO insertJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param job 方法参数。
     */
    void updateJob(TenantId tenantId, KnowledgeIngestionJobBO job);

    /**
     * 执行 {@code pageJobsByTenant} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param spaceId 方法参数。
     * @param status 对象状态。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeIngestionJobBO> pageJobsByTenant(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId, String status);

    /**
     * 执行 {@code pollPendingJobs} 定义的接口操作。
     *
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeIngestionJobBO> pollPendingJobs(int limit);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param heartbeatBefore 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeIngestionJobBO> findStaleJobs(LocalDateTime heartbeatBefore);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param audit 方法参数。
     */
    void insertAudit(TenantId tenantId, KnowledgeAuditLogBO audit);

    /**
     * 执行 {@code pageAudit} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param spaceId 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeAuditLogBO> pageAudit(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param evaluation 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeEvaluationCaseBO insertEvaluation(
            TenantId tenantId, KnowledgeEvaluationCaseBO evaluation);

    /**
     * 执行 {@code pageEvaluations} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param spaceId 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeEvaluationCaseBO> pageEvaluations(
            TenantId tenantId, int pageNum, int pageSize, Long spaceId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
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
