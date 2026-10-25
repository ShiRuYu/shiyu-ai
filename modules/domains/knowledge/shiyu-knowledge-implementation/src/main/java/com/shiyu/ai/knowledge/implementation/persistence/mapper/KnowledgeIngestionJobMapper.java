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

    @Select(
            "SELECT id FROM knowledge_ingestion_job WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion} AND del_flag = 0 FOR UPDATE")
    Long lockCurrentIndexRebuildJob(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion);

    @Select(
            "SELECT id FROM knowledge_ingestion_job WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion} AND del_flag = 0")
    Long currentIndexRebuildJob(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion);

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

    @Update(
            "UPDATE knowledge_ingestion_job SET stage = 'LEASED_BUILD'"
                    + " WHERE tenant_id = #{tenantId} AND id = #{jobId}"
                    + " AND job_type = 'INDEX_REBUILD' AND job_status = 'RUNNING'"
                    + " AND lock_version = #{lockVersion} AND stage = 'ACTIVATING' AND del_flag = 0")
    int resetIndexRebuildActivation(
            @Param("tenantId") Long tenantId,
            @Param("jobId") Long jobId,
            @Param("lockVersion") Long lockVersion);

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
