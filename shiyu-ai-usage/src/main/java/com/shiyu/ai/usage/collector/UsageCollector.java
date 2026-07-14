package com.shiyu.ai.usage.collector;

import com.shiyu.ai.dal.dataobject.agent.TokenUsageDO;
import com.shiyu.ai.dal.repository.agent.TokenUsageRepository;
import com.shiyu.ai.usage.model.ModelPricing;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用量收集器
 * 接收模型调用事件，记录 Token/Cost/Latency
 */
@Slf4j
public class UsageCollector {

    private final TokenUsageRepository repository;
    private final Map<String, ModelPricing> pricingMap = new ConcurrentHashMap<>();

    public UsageCollector(TokenUsageRepository repository) {
        this.repository = repository;
        registerPricing(ModelPricing.defaultOpenAI());
    }

    /**
     * 注册模型定价
     */
    public void registerPricing(ModelPricing pricing) {
        pricingMap.put(pricing.getPlatform() + ":" + pricing.getModel(), pricing);
    }

    /**
     * 记录用量
     */
    public void recordUsage(String platform, String model,
                            int promptTokens, int completionTokens,
                            long latencyMs, Long userId, String sessionId) {
        ModelPricing pricing = pricingMap.getOrDefault(
            platform + ":" + model,
            new ModelPricing(platform, model, 0.0, 0.0)
        );
        double cost = pricing.calculateCost(promptTokens, completionTokens);

        TokenUsageDO record = new TokenUsageDO();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setPlatform(platform);
        record.setModel(model);
        record.setPromptTokens(promptTokens);
        record.setCompletionTokens(completionTokens);
        record.setTotalTokens(promptTokens + completionTokens);
        record.setLatencyMs(latencyMs);
        record.setCost(cost);
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setCreateTime(LocalDateTime.now());

        try {
            repository.insert(record);
            log.debug("用量记录已保存: platform={}, model={}, tokens={}, cost={}, latency={}ms",
                    platform, model, record.getTotalTokens(), cost, latencyMs);
        } catch (Exception e) {
            log.error("保存用量记录失败: platform={}, model={}", platform, model, e);
        }
    }

    /**
     * 获取已注册的定价数
     */
    public int getPricingCount() {
        return pricingMap.size();
    }
}
