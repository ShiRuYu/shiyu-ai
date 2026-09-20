package com.shiyu.ai.governance.implementation.usage.persistence.mapper;

import com.mybatisflex.annotation.UseDataSource;
import com.shiyu.ai.common.mybatis.mapper.BaseMapperFlex;
import com.shiyu.ai.governance.implementation.usage.persistence.dataobject.UsageRecordDO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 负责 用量 Record 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Mapper
@UseDataSource("agent")
public interface UsageRecordMapper extends BaseMapperFlex<UsageRecordDO> {

    /** RECORD_COLUMNS 字段，保存columns。 */
    String RECORD_COLUMNS =
            "id, usage_type AS usageType, latency_ms AS latencyMs, "
                    + "user_id AS userId, session_id AS sessionId, ext_info AS extInfo, "
                    + "tenant_id AS tenantId, "
                    + "create_time AS createTime";

    /**
     * 按日统计当前租户的调用次数和平均延迟。
     *
     * @param days 向前统计的天数。
     * @param tenantId 当前租户标识。
     * @return 按日期和用量类型聚合的统计结果。
     */
    @Select(
            "SELECT CAST(create_time AS DATE) as usage_date, "
                    + "usage_type, "
                    + "COUNT(*) as call_count, "
                    + "AVG(latency_ms) as avg_latency_ms "
                    + "FROM governance_usage_record "
                    + "WHERE create_time >= DATEADD('DAY', -#{days}, CURRENT_TIMESTAMP) "
                    + "AND tenant_id = #{tenantId} "
                    + "GROUP BY CAST(create_time AS DATE), usage_type "
                    + "ORDER BY usage_date DESC")
    List<Map<String, Object>> aggregateByDay(
            @Param("days") int days, @Param("tenantId") long tenantId);




    /**
     * 按周统计租户用量记录的调用次数和平均延迟。
     *
     * @param weeks 向前统计的周数。
     * @param tenantId 当前租户标识。
     * @return 按周和用量类型聚合的统计结果。
     */
    @Select(
            "SELECT FORMATDATETIME(create_time, 'yyyy-ww') as usage_week, "
                    + "usage_type, "
                    + "COUNT(*) as call_count, "
                    + "AVG(latency_ms) as avg_latency_ms "
                    + "FROM governance_usage_record "
                    + "WHERE create_time >= DATEADD('WEEK', -#{weeks}, CURRENT_TIMESTAMP) "
                    + "AND tenant_id = #{tenantId} "
                    + "GROUP BY FORMATDATETIME(create_time, 'yyyy-ww'), usage_type "
                    + "ORDER BY usage_week DESC")
    List<Map<String, Object>> aggregateByWeek(
            @Param("weeks") int weeks, @Param("tenantId") long tenantId);



    /**
     * 按月统计租户用量记录的调用次数和平均延迟。
     *
     * @param months 向前统计的月数。
     * @param tenantId 当前租户标识。
     * @return 按月和用量类型聚合的统计结果。
     */
    @Select(
            "SELECT FORMATDATETIME(create_time, 'yyyy-MM') as usage_month, "
                    + "usage_type, "
                    + "COUNT(*) as call_count, "
                    + "AVG(latency_ms) as avg_latency_ms "
                    + "FROM governance_usage_record "
                    + "WHERE create_time >= DATEADD('MONTH', -#{months}, CURRENT_TIMESTAMP) "
                    + "AND tenant_id = #{tenantId} "
                    + "GROUP BY FORMATDATETIME(create_time, 'yyyy-MM'), usage_type "
                    + "ORDER BY usage_month DESC")
    List<Map<String, Object>> aggregateByMonth(
            @Param("months") int months, @Param("tenantId") long tenantId);


    /**
     * 汇总当前租户的调用次数、平均延迟和用量类型数量。
     *
     * @param tenantId 当前租户标识。
     * @return 当前租户的概览统计结果。
     */
    @Select(
            "SELECT COUNT(*) as total_calls, "
                    + "AVG(latency_ms) as avg_latency_ms, "
                    + "COUNT(DISTINCT usage_type) as type_count "
                    + "FROM governance_usage_record WHERE 1 = 1 "
                    + "AND tenant_id = #{tenantId}")
    Map<String, Object> getOverview(@Param("tenantId") long tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    @Select("SELECT " + RECORD_COLUMNS + " FROM governance_usage_record WHERE usage_type = 'LLM' AND tenant_id = #{tenantId}")
    List<UsageRecordDO> selectLlmRecords(@Param("tenantId") long tenantId);

    /**
     * 查询指定时间之后当前租户的用量记录。
     *
     * @param start 起始时间，不包含早于该时间的记录。
     * @param tenantId 当前租户标识。
     * @return 当前租户的用量记录。
     */
    @Select(
            "SELECT "
                    + RECORD_COLUMNS
                    + " FROM governance_usage_record WHERE create_time >= #{start} "
                    + "AND tenant_id = #{tenantId}")
    List<UsageRecordDO> selectRecordsSince(
            @Param("start") LocalDateTime start, @Param("tenantId") long tenantId);


    /**
     * 查询指定时间之后当前租户的 LLM 用量记录。
     *
     * @param start 起始时间，不包含早于该时间的记录。
     * @param tenantId 当前租户标识。
     * @return 当前租户的 LLM 用量记录。
     */
    @Select(
            "SELECT "
                    + RECORD_COLUMNS
                    + " FROM governance_usage_record WHERE usage_type = 'LLM' AND create_time >="
                    + " #{start} "
                    + "AND tenant_id = #{tenantId}")
    List<UsageRecordDO> selectLlmRecordsSince(
            @Param("start") LocalDateTime start, @Param("tenantId") long tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    @Select(
            "SELECT "
                    + RECORD_COLUMNS
                     + " FROM governance_usage_record WHERE usage_type = 'EMBEDDING' "
                     + "AND tenant_id = #{tenantId}")
    List<UsageRecordDO> selectEmbeddingRecords(@Param("tenantId") long tenantId);

    /**
     * 查询 用量 Record 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param usageType 用于完成本次业务处理的 usageType 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     * @param userId 当前操作涉及的用户标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param sessionId 用于定位session的标识。
     * @param extInfo 用于完成本次业务处理的 extInfo 参数。
     * @param start 用于完成本次业务处理的 start 参数。
     */
    @Select(
            "SELECT r.id, r.usage_type AS usageType, r.latency_ms AS latencyMs, r.user_id AS"
                    + " userId, r.tenant_id AS tenantId, r.session_id AS sessionId, r.ext_info AS"
                    + " extInfo, r.create_time AS createTime FROM governance_usage_record r WHERE"
                    + " r.usage_type = 'LLM' AND r.tenant_id = #{tenantId} AND r.create_time >="
                    + " #{start}")
    List<UsageRecordDO> selectLlmTodayByTenantId(
            @Param("tenantId") Long tenantId, @Param("start") LocalDateTime start);

    /** 查询平台统计所需的全部用量记录。 */
    @Select("SELECT " + RECORD_COLUMNS + " FROM governance_usage_record")
    List<UsageRecordDO> selectAllRecords();

    /** 查询平台统计所需的全部 LLM 用量记录。 */
    @Select("SELECT " + RECORD_COLUMNS + " FROM governance_usage_record WHERE usage_type = 'LLM'")
    List<UsageRecordDO> selectAllLlmRecords();

    /** 查询平台统计所需的时间范围内全部记录。 */
    @Select(
            "SELECT " + RECORD_COLUMNS
                    + " FROM governance_usage_record WHERE create_time >= #{start}")
    List<UsageRecordDO> selectAllRecordsSince(@Param("start") LocalDateTime start);

    /** 查询平台统计所需的时间范围内全部 LLM 记录。 */
    @Select(
            "SELECT " + RECORD_COLUMNS
                    + " FROM governance_usage_record WHERE usage_type = 'LLM'"
                    + " AND create_time >= #{start}")
    List<UsageRecordDO> selectAllLlmRecordsSince(@Param("start") LocalDateTime start);

    /** 查询平台统计所需的全部 Embedding 记录。 */
    @Select(
            "SELECT " + RECORD_COLUMNS
                    + " FROM governance_usage_record WHERE usage_type = 'EMBEDDING'")
    List<UsageRecordDO> selectAllEmbeddingRecords();

    /** 查询全部租户的按日统计。 */
    @Select(
            "SELECT CAST(create_time AS DATE) as usage_date, usage_type, "
                    + "COUNT(*) as call_count, AVG(latency_ms) as avg_latency_ms "
                    + "FROM governance_usage_record "
                    + "WHERE create_time >= DATEADD('DAY', -#{days}, CURRENT_TIMESTAMP) "
                    + "GROUP BY CAST(create_time AS DATE), usage_type ORDER BY usage_date DESC")
    List<Map<String, Object>> aggregateByDayAllTenants(@Param("days") int days);

    /** 查询全部租户的按周统计。 */
    @Select(
            "SELECT FORMATDATETIME(create_time, 'yyyy-ww') as usage_week, usage_type, "
                    + "COUNT(*) as call_count, AVG(latency_ms) as avg_latency_ms "
                    + "FROM governance_usage_record "
                    + "WHERE create_time >= DATEADD('WEEK', -#{weeks}, CURRENT_TIMESTAMP) "
                    + "GROUP BY FORMATDATETIME(create_time, 'yyyy-ww'), usage_type "
                    + "ORDER BY usage_week DESC")
    List<Map<String, Object>> aggregateByWeekAllTenants(@Param("weeks") int weeks);

    /** 查询全部租户的按月统计。 */
    @Select(
            "SELECT FORMATDATETIME(create_time, 'yyyy-MM') as usage_month, usage_type, "
                    + "COUNT(*) as call_count, AVG(latency_ms) as avg_latency_ms "
                    + "FROM governance_usage_record "
                    + "WHERE create_time >= DATEADD('MONTH', -#{months}, CURRENT_TIMESTAMP) "
                    + "GROUP BY FORMATDATETIME(create_time, 'yyyy-MM'), usage_type "
                    + "ORDER BY usage_month DESC")
    List<Map<String, Object>> aggregateByMonthAllTenants(@Param("months") int months);

    /** 查询全部租户的概览。 */
    @Select(
            "SELECT COUNT(*) as total_calls, AVG(latency_ms) as avg_latency_ms, "
                    + "COUNT(DISTINCT usage_type) as type_count "
                    + "FROM governance_usage_record")
    Map<String, Object> getOverviewAllTenants();
}
