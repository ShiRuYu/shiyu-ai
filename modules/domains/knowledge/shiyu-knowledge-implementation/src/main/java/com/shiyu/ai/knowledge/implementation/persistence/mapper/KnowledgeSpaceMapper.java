package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeSpaceDO;



/**
 * 负责 知识 空间 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface KnowledgeSpaceMapper extends BaseMapperFlex<KnowledgeSpaceDO> {
    @Select(
            "SELECT active_index_version FROM knowledge_space WHERE tenant_id = #{tenantId}"
                    + " AND id = #{spaceId} AND del_flag = 0")
    Long activeIndexVersion(
            @Param("tenantId") Long tenantId, @Param("spaceId") Long spaceId);

    @Select(
            "SELECT id FROM knowledge_space WHERE tenant_id = #{tenantId}"
                    + " AND id = #{spaceId} AND del_flag = 0 FOR UPDATE")
    Long lockSpaceForIndexRebuild(
            @Param("tenantId") Long tenantId, @Param("spaceId") Long spaceId);

    @Update(
            "UPDATE knowledge_space SET active_index_version = #{newVersion}"
                    + " WHERE tenant_id = #{tenantId} AND id = #{spaceId}"
                    + " AND active_index_version = #{expectedVersion} AND del_flag = 0"
                    + " AND EXISTS (SELECT 1 FROM knowledge_ingestion_job j"
                    + " WHERE j.tenant_id = #{tenantId} AND j.space_id = #{spaceId}"
                    + " AND j.job_type = 'INDEX_REBUILD_LEASE' AND j.job_key = #{leaseKey}"
                    + " AND (#{jobId} IS NULL OR j.checkpoint_data = #{jobId})"
                    + " AND j.job_status = 'RUNNING' AND j.lock_version = #{fencingVersion}"
                    + " AND j.heartbeat_time >= #{heartbeatBefore} AND j.del_flag = 0)"
                    + " AND (#{jobId} IS NULL OR EXISTS (SELECT 1 FROM knowledge_ingestion_job r"
                    + " WHERE r.tenant_id = #{tenantId} AND r.id = #{jobId}"
                    + " AND r.space_id = #{spaceId} AND r.job_type = 'INDEX_REBUILD'"
                    + " AND r.job_status = 'RUNNING' AND r.stage = 'ACTIVATING'"
                    + " AND r.lock_version = #{jobLockVersion} AND r.del_flag = 0))")
    int activateIndexRebuild(
            @Param("tenantId") Long tenantId,
            @Param("spaceId") Long spaceId,
            @Param("leaseKey") String leaseKey,
            @Param("fencingVersion") Long fencingVersion,
            @Param("heartbeatBefore") java.time.LocalDateTime heartbeatBefore,
            @Param("expectedVersion") Long expectedVersion,
            @Param("newVersion") Long newVersion,
            @Param("jobId") String jobId,
            @Param("jobLockVersion") Long jobLockVersion);
}
