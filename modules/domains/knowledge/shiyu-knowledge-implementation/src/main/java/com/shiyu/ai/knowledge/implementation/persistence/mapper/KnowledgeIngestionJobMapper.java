package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeIngestionJobDO;



/**
 * 负责 知识 Ingestion Job 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface KnowledgeIngestionJobMapper extends BaseMapperFlex<KnowledgeIngestionJobDO> {
    /**
     * 锁定仍持有指定版本领取凭据、且文档当前版本匹配的入库任务。
     *
     * @param tenantId 记录所属租户标识
     * @param jobId 索引重建或入库任务标识
     * @param documentId 入库文档标识
     * @param versionId 入库文档版本标识
     * @param lockVersion 任务应匹配的乐观锁版本
     *
     * @return 仍有效的任务标识；领取或文档版本已变化时为 null
     */
    @Select(
            "SELECT j.id FROM knowledge_ingestion_job j JOIN knowledge_document d"
                    + " ON d.tenant_id = j.tenant_id AND d.id = j.document_id"
                    + " WHERE j.tenant_id = #{tenantId} AND j.id = #{jobId}"
                    + " AND j.document_id = #{documentId} AND j.version_id = #{versionId}"
                    + " AND d.current_version_id = #{versionId}"
                    + " AND j.job_type = 'INGEST' AND j.job_status = 'RUNNING'"
                    + " AND j.lock_version = #{lockVersion} AND j.del_flag = 0 FOR UPDATE")
    Long lockClaimedIngestionJob(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("documentId") Long documentId,
            @Param("versionId") Long versionId,
            @Param("lockVersion") Long lockVersion);

    /**
     * 按旧隔离版本领取空闲或过期的索引重建租约，并写入新隔离版本。
     *
     * @param tenantId 记录所属租户标识
     * @param leaseId 重建租约记录标识
     * @param leaseKey 空间重建租约的唯一键
     * @param jobId 索引重建或入库任务标识
     * @param oldFencingVersion 领取前应匹配的隔离版本
     * @param newFencingVersion 领取成功后写入的新隔离版本
     * @param heartbeatTime 本次写入的心跳时间
     * @param staleBefore 允许接管的过期心跳时间上界
     *
     * @return 成功领取时返回 1；租约条件不匹配时返回 0
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET job_status = 'RUNNING', stage = 'BUILDING',"
                    + " progress = 1, checkpoint_data = #{jobId}, heartbeat_time = #{heartbeatTime},"
                    + " finished_time = NULL, lock_version = #{newFencingVersion}"
                    + " WHERE tenant_id = #{tenantId} AND id = #{leaseId}"
                    + " AND job_key = #{leaseKey} AND lock_version = #{oldFencingVersion}"
                    + " AND (job_status <> 'RUNNING' OR heartbeat_time IS NULL"
                    + " OR heartbeat_time < #{staleBefore}) AND del_flag = 0")
    int claimIndexRebuildLease(
            @Param("tenantId") Long tenantId,
            @Param("leaseId") Long leaseId,
            @Param("leaseKey") String leaseKey,
            @Param("jobId") String jobId,
            @Param("oldFencingVersion") Long oldFencingVersion,
            @Param("newFencingVersion") Long newFencingVersion,
            @Param("heartbeatTime") java.time.LocalDateTime heartbeatTime,
            @Param("staleBefore") java.time.LocalDateTime staleBefore);


    /**
     * 仅在隔离版本仍匹配时刷新运行中索引重建租约的心跳。
     *
     * @param tenantId 记录所属租户标识
     * @param spaceId 知识空间标识
     * @param leaseKey 空间重建租约的唯一键
     * @param fencingVersion 当前工作线程持有的租约隔离版本
     * @param heartbeatTime 本次写入的心跳时间
     *
     * @return 成功刷新时返回 1；租约已失效时返回 0
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET heartbeat_time = #{heartbeatTime}"
                    + " WHERE tenant_id = #{tenantId} AND space_id = #{spaceId}"
                    + " AND job_type = 'INDEX_REBUILD_LEASE' AND job_key = #{leaseKey}"
                    + " AND job_status = 'RUNNING' AND lock_version = #{fencingVersion}"
                    + " AND del_flag = 0")
    int heartbeatIndexRebuildLease(
            @Param("tenantId") Long tenantId,
            @Param("spaceId") Long spaceId,
            @Param("leaseKey") String leaseKey,
            @Param("fencingVersion") Long fencingVersion,
            @Param("heartbeatTime") java.time.LocalDateTime heartbeatTime);

    /**
     * 锁定处于运行状态且乐观锁版本仍匹配的索引重建任务。
     *
     * @param tenantId 记录所属租户标识
     * @param jobId 索引重建或入库任务标识
     * @param lockVersion 任务应匹配的乐观锁版本
     *
     * @return 匹配的任务标识；任务已停止或版本不符时为 null
     */
    @Select(
            "SELECT id FROM knowledge_ingestion_job WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion} AND del_flag = 0 FOR UPDATE")
    Long lockCurrentIndexRebuildJob(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion);

    /**
     * 查询索引重建任务是否仍运行在预期的乐观锁版本。
     *
     * @param tenantId 记录所属租户标识
     * @param jobId 索引重建或入库任务标识
     * @param lockVersion 任务应匹配的乐观锁版本
     *
     * @return 匹配的任务标识；任务已停止或版本不符时为 null
     */
    @Select(
            "SELECT id FROM knowledge_ingestion_job WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion} AND del_flag = 0")
    Long currentIndexRebuildJob(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion);

    /**
     * 将仍持有任务版本的重建任务标记为激活阶段，阻止并发取消。
     *
     * @param tenantId 记录所属租户标识
     * @param jobId 索引重建或入库任务标识
     * @param lockVersion 任务应匹配的乐观锁版本
     *
     * @return 成功进入激活阶段时返回 1；任务状态或版本不符时返回 0
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET stage = 'ACTIVATING'"
                    + " WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion}"
                    + " AND (stage IS NULL OR stage <> 'ACTIVATING') AND del_flag = 0")
    int markIndexRebuildActivating(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion);

    /**
     * 将尚未完成激活的当前重建任务恢复到持租约构建阶段。
     *
     * @param tenantId 记录所属租户标识
     * @param jobId 索引重建或入库任务标识
     * @param lockVersion 任务应匹配的乐观锁版本
     *
     * @return 成功恢复时返回 1；任务阶段或版本不符时返回 0
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET stage = 'LEASED_BUILD'"
                    + " WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion} AND stage = 'ACTIVATING' AND del_flag = 0")
    int resetIndexRebuildActivation(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion);

    /**
     * 在任务领取版本仍有效时累计执行次数，并更新构建阶段与心跳。
     *
     * @param tenantId 记录所属租户标识
     * @param jobId 索引重建或入库任务标识
     * @param lockVersion 任务应匹配的乐观锁版本
     * @param heartbeatTime 本次写入的心跳时间
     *
     * @return 成功开始本次尝试时返回 1；任务已失效时返回 0
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET attempts = attempts + 1, stage = 'LEASED_BUILD',"
                    + " heartbeat_time = #{heartbeatTime}"
                    + " WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion} AND del_flag = 0")
    int beginIndexRebuildAttempt(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion,
            @Param("heartbeatTime") java.time.LocalDateTime heartbeatTime);

    /**
     * 取消待执行或运行中且尚未进入激活阶段的重建任务，并递增锁版本。
     *
     * @param tenantId 记录所属租户标识
     * @param jobId 索引重建或入库任务标识
     * @param lockVersion 任务应匹配的乐观锁版本
     * @param cancelledAt 取消操作的完成时间
     *
     * @return 成功取消时返回 1；任务不可取消或版本不符时返回 0
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET job_status = 'CANCELLED', stage = 'CANCELLED',"
                    + " finished_time = #{cancelledAt}, lock_version = lock_version + 1"
                    + " WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND lock_version = #{lockVersion}"
                    + " AND job_status IN ('PENDING', 'RUNNING')"
                    + " AND (stage IS NULL OR stage <> 'ACTIVATING') AND del_flag = 0")
    int cancelIndexRebuildJob(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion,
            @Param("cancelledAt") java.time.LocalDateTime cancelledAt);

    /**
     * 取消指定任务当前持有的重建租约，并递增锁版本使旧工作线程失效。
     *
     * @param tenantId 记录所属租户标识
     * @param spaceId 知识空间标识
     * @param leaseKey 空间重建租约的唯一键
     * @param jobId 索引重建或入库任务标识
     * @param cancelledAt 取消操作的完成时间
     *
     * @return 被取消的运行中租约数量
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET job_status = 'CANCELLED', stage = 'CANCELLED',"
                    + " finished_time = #{cancelledAt}, heartbeat_time = #{cancelledAt},"
                    + " lock_version = lock_version + 1 WHERE tenant_id = #{tenantId}"
                    + " AND space_id = #{spaceId} AND job_type = 'INDEX_REBUILD_LEASE'"
                    + " AND job_key = #{leaseKey} AND checkpoint_data = #{jobId}"
                    + " AND job_status = 'RUNNING' AND del_flag = 0")
    int fenceRunningIndexRebuildLease(
            @Param("tenantId") Long tenantId,
            @Param("spaceId") Long spaceId,
            @Param("leaseKey") String leaseKey,
            @Param("jobId") String jobId,
            @Param("cancelledAt") java.time.LocalDateTime cancelledAt);

    /**
     * 仅在隔离版本仍有效时结束租约，成功则完成，失败则转为等待重试。
     *
     * @param tenantId 记录所属租户标识
     * @param spaceId 知识空间标识
     * @param leaseKey 空间重建租约的唯一键
     * @param fencingVersion 当前工作线程持有的租约隔离版本
     * @param heartbeatTime 本次写入的心跳时间
     * @param succeeded 本次租约构建是否成功
     *
     * @return 成功释放时返回 1；租约已失效时返回 0
     */
    @Update(
            "UPDATE knowledge_ingestion_job SET job_status = CASE WHEN #{succeeded} THEN 'SUCCEEDED'"
                    + " ELSE 'PENDING' END, stage = CASE WHEN #{succeeded} THEN 'COMPLETED'"
                    + " ELSE 'RETRY_WAIT' END, progress = CASE WHEN #{succeeded} THEN 100 ELSE 0 END,"
                    + " heartbeat_time = #{heartbeatTime}, finished_time = #{heartbeatTime},"
                    + " error_message = CASE WHEN #{succeeded} THEN NULL ELSE '索引租约构建失败' END"
                    + " WHERE tenant_id = #{tenantId} AND space_id = #{spaceId}"
                    + " AND job_type = 'INDEX_REBUILD_LEASE' AND job_key = #{leaseKey}"
                    + " AND job_status = 'RUNNING' AND lock_version = #{fencingVersion}"
                    + " AND del_flag = 0")
    int releaseIndexRebuildLease(
            @Param("tenantId") Long tenantId,
            @Param("spaceId") Long spaceId,
            @Param("leaseKey") String leaseKey,
            @Param("fencingVersion") Long fencingVersion,
            @Param("heartbeatTime") java.time.LocalDateTime heartbeatTime,
            @Param("succeeded") boolean succeeded);
}
