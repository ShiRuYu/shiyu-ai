package com.shiyu.ai.dal.usage.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import com.shiyu.ai.dal.datasource.DataSourceConfig;
import com.shiyu.ai.dal.usage.dataobject.UsageRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
@UseDataSource(DataSourceConfig.AGENT)
public interface UsageRecordMapper extends BaseMapperFlex<UsageRecordDO> {

    /**
     * Custom {@code @Select} statements do not use MyBatis-Flex's generated
     * column mapping. Alias every snake-case column explicitly so a
     * {@link UsageRecordDO} retains its JSON metadata and latency fields.
     */
    String RECORD_COLUMNS = "id, usage_type AS usageType, latency_ms AS latencyMs, "
            + "user_id AS userId, session_id AS sessionId, ext_info AS extInfo, "
            + "create_time AS createTime";

    @Select("SELECT CAST(create_time AS DATE) as usage_date, " +
            "usage_type, " +
            "COUNT(*) as call_count, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE create_time >= DATEADD('DAY', -#{days}, CURRENT_TIMESTAMP) " +
            "GROUP BY CAST(create_time AS DATE), usage_type " +
            "ORDER BY usage_date DESC")
    List<Map<String, Object>> aggregateByDay(@Param("days") int days);

    @Select("SELECT FORMATDATETIME(create_time, 'yyyy-ww') as usage_week, " +
            "usage_type, " +
            "COUNT(*) as call_count, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE create_time >= DATEADD('WEEK', -#{weeks}, CURRENT_TIMESTAMP) " +
            "GROUP BY FORMATDATETIME(create_time, 'yyyy-ww'), usage_type " +
            "ORDER BY usage_week DESC")
    List<Map<String, Object>> aggregateByWeek(@Param("weeks") int weeks);

    @Select("SELECT FORMATDATETIME(create_time, 'yyyy-MM') as usage_month, " +
            "usage_type, " +
            "COUNT(*) as call_count, " +
            "AVG(latency_ms) as avg_latency_ms " +
            "FROM agent_usage_record " +
            "WHERE create_time >= DATEADD('MONTH', -#{months}, CURRENT_TIMESTAMP) " +
            "GROUP BY FORMATDATETIME(create_time, 'yyyy-MM'), usage_type " +
            "ORDER BY usage_month DESC")
    List<Map<String, Object>> aggregateByMonth(@Param("months") int months);

    @Select("SELECT COUNT(*) as total_calls, " +
            "AVG(latency_ms) as avg_latency_ms, " +
            "COUNT(DISTINCT usage_type) as type_count " +
            "FROM agent_usage_record")
    Map<String, Object> getOverview();

    @Select("SELECT COUNT(*) FROM agent_ai_platform WHERE status = 1 AND del_flag = 0")
    long countEnabledPlatforms();

    @Select("SELECT COUNT(*) FROM agent_ai_model WHERE status = 1 AND del_flag = 0")
    long countEnabledModels();

    /**
     * H2 does not expose the MySQL JSON extraction functions used by the old
     * aggregation queries. The repository parses the JSON payloads after
     * loading these bounded record sets instead.
     */
    @Select("SELECT " + RECORD_COLUMNS + " FROM agent_usage_record WHERE usage_type = 'LLM'")
    List<UsageRecordDO> selectLlmRecords();

    @Select("SELECT " + RECORD_COLUMNS
            + " FROM agent_usage_record WHERE usage_type = 'LLM' AND create_time >= #{start}")
    List<UsageRecordDO> selectLlmRecordsSince(@Param("start") LocalDateTime start);

    @Select("SELECT " + RECORD_COLUMNS + " FROM agent_usage_record WHERE usage_type = 'EMBEDDING'")
    List<UsageRecordDO> selectEmbeddingRecords();

    @Select("SELECT r.id, r.usage_type AS usageType, r.latency_ms AS latencyMs, "
            + "r.user_id AS userId, r.session_id AS sessionId, r.ext_info AS extInfo, "
            + "r.create_time AS createTime FROM agent_usage_record r " +
            "JOIN auth_user u ON r.user_id = u.id " +
            "WHERE r.usage_type = 'LLM' AND u.tenant_id = #{tenantId} AND r.create_time >= #{start}")
    List<UsageRecordDO> selectLlmTodayByTenantId(@Param("tenantId") Long tenantId,
                                                   @Param("start") LocalDateTime start);
}
