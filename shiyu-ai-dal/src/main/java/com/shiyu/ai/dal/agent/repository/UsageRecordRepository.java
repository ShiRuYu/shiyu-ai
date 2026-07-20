package com.shiyu.ai.dal.agent.repository;

import com.shiyu.ai.dal.agent.dataobject.UsageRecordDO;
import com.shiyu.ai.dal.agent.mapper.UsageRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 统一用量记录数据访问
 */
@Component
public class UsageRecordRepository {

    @Resource
    private UsageRecordMapper usageRecordMapper;

    public void insert(UsageRecordDO record) {
        usageRecordMapper.insertSelective(record);
    }

    // ====== 通用聚合 ======

    public List<Map<String, Object>> aggregateByDay(int days) {
        return usageRecordMapper.aggregateByDay(days);
    }

    public List<Map<String, Object>> aggregateByWeek(int weeks) {
        return usageRecordMapper.aggregateByWeek(weeks);
    }

    public List<Map<String, Object>> aggregateByMonth(int months) {
        return usageRecordMapper.aggregateByMonth(months);
    }

    public Map<String, Object> getOverview() {
        return usageRecordMapper.getOverview();
    }

    // ====== LLM 专用聚合 ======

    public List<Map<String, Object>> aggregateByModel() {
        return usageRecordMapper.aggregateByModel();
    }

    public List<Map<String, Object>> aggregateLlmByDay(int days) {
        return usageRecordMapper.aggregateLlmByDay(days);
    }

    public List<Map<String, Object>> aggregateLlmByWeek(int weeks) {
        return usageRecordMapper.aggregateLlmByWeek(weeks);
    }

    public List<Map<String, Object>> aggregateLlmByMonth(int months) {
        return usageRecordMapper.aggregateLlmByMonth(months);
    }

    // ====== Embedding 专用聚合 ======

    public Map<String, Object> getEmbeddingOverview() {
        return usageRecordMapper.getEmbeddingOverview();
    }

    /** 查询指定租户当天的 LLM Token 总消耗 */
    public Long sumLlmTodayTokensByTenantId(Long tenantId) {
        return usageRecordMapper.sumLlmTodayTokensByTenantId(tenantId);
    }
}
