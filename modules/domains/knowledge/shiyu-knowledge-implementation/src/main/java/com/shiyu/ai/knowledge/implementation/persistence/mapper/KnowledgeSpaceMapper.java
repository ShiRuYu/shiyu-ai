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
    /**
     * 查询指定租户中未删除知识空间的当前生效索引版本。
     *
     * @param tenantId 记录所属租户标识
     * @param spaceId 知识空间标识
     *
     * @return 当前生效版本；空间不存在时为 null
     */
    @Select(
            "SELECT active_index_version FROM knowledge_space WHERE tenant_id = #{tenantId}"
                    + " AND id = #{spaceId} AND del_flag = 0")
    Long activeIndexVersion(
            @Param("tenantId") Long tenantId, @Param("spaceId") Long spaceId);

    /**
     * 为索引重建激活过程锁定指定租户中的知识空间记录。
     *
     * @param tenantId 记录所属租户标识
     * @param spaceId 知识空间标识
     *
     * @return 已锁定的空间标识；空间不存在时为 null
     */
    @Select(
            "SELECT id FROM knowledge_space WHERE tenant_id = #{tenantId}"
                    + " AND id = #{spaceId} AND del_flag = 0 FOR UPDATE")
    Long lockSpaceForIndexRebuild(
            @Param("tenantId") Long tenantId, @Param("spaceId") Long spaceId);

    /**
     * 校验空间旧版本、有效租约和任务激活状态后，原子切换生效索引版本。
     *
     * @param tenantId 记录所属租户标识
     * @param spaceId 知识空间标识
     * @param leaseKey 空间重建租约的唯一键
     * @param fencingVersion 当前工作线程持有的租约隔离版本
     * @param heartbeatBefore 激活时要求的最早有效心跳时间
     * @param expectedVersion 空间当前应匹配的生效索引版本
     * @param newVersion 准备激活的新索引版本
     * @param jobId 索引重建或入库任务标识
     * @param jobLockVersion 重建任务应匹配的乐观锁版本
     *
     * @return 成功切换时返回 1；版本、租约或任务条件不符时返回 0
     */
    @Update(
            "UPDATE knowledge_space SET active_index_version = #{newVersion}"
                    + " WHERE tenant_id = #{tenantId} AND id = #{spaceId}"
                    + " AND active_index_version = #{expectedVersion} AND del_flag = 0"
                    + " AND EXISTS (SELECT 1 FROM knowledge_ingestion_job j"
                    + " WHERE j.tenant_id = #{tenantId} AND j.space_id = #{spaceId}"
                    + " AND j.job_type = 'INDEX_REBUILD_LEASE' AND j.job_key = #{leaseKey}"
                    + " AND (#{jobId,jdbcType=VARCHAR} IS NULL OR j.checkpoint_data = #{jobId,jdbcType=VARCHAR})"
                    + " AND j.job_status = 'RUNNING' AND j.lock_version = #{fencingVersion}"
                    + " AND j.heartbeat_time >= #{heartbeatBefore} AND j.del_flag = 0)"
                    + " AND (#{jobId,jdbcType=VARCHAR} IS NULL OR EXISTS (SELECT 1 FROM knowledge_ingestion_job r"
                    + " WHERE r.tenant_id = #{tenantId} AND r.id = CAST(#{jobId,jdbcType=VARCHAR} AS DECIMAL(19,0))"
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
