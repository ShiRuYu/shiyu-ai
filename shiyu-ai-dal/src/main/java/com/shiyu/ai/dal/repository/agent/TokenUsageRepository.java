package com.shiyu.ai.dal.repository.agent;

import com.shiyu.ai.dal.dataobject.agent.TokenUsageDO;
import com.shiyu.ai.dal.mapper.agent.TokenUsageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Token 用量数据访问
 * 使用 MyBatis-Flex 增删改查 + Mapper @Select 聚合查询
 */
@Component
public class TokenUsageRepository {

    @Resource
    private TokenUsageMapper tokenUsageMapper;

    /**
     * 插入用量记录
     */
    public void insert(TokenUsageDO tokenUsageDO) {
        tokenUsageMapper.insertSelective(tokenUsageDO);
    }

    public List<Map<String, Object>> aggregateByDay(int days) {
        return tokenUsageMapper.aggregateByDay(days);
    }

    public List<Map<String, Object>> aggregateByWeek(int weeks) {
        return tokenUsageMapper.aggregateByWeek(weeks);
    }

    public List<Map<String, Object>> aggregateByMonth(int months) {
        return tokenUsageMapper.aggregateByMonth(months);
    }

    public List<Map<String, Object>> aggregateByModel() {
        return tokenUsageMapper.aggregateByModel();
    }

    public Map<String, Object> getOverview() {
        return tokenUsageMapper.getOverview();
    }
}
