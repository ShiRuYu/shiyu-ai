package com.shiyu.ai.model.resilience;

import com.shiyu.ai.model.adapter.ModelManager;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 模型弹性注册表
 * 管理每个平台的熔断器、限流器，提供统一的弹性执行入口
 */
@Slf4j
@Component
public class ModelResilienceRegistry {

    private final ModelManager modelManager;
    private final Map<String, ModelResilienceWrapper> wrappers = new ConcurrentHashMap<>();

    /** 可用平台列表（用于负载均衡） */
    private final LoadBalancer loadBalancer = new LoadBalancer();

    public ModelResilienceRegistry(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @PostConstruct
    public void init() {
        // 为所有已知平台注册弹性包装器
        registerPlatform("OPENAI");
        registerPlatform("DEEPSEEK");
        registerPlatform("OPENROUTER");
        registerPlatform("SILICON_FLOW");
        registerPlatform("OLLAMA");
        log.info("模型弹性注册表初始化完成: {} 个平台", wrappers.size());
    }

    /**
     * 注册平台的弹性包装器
     */
    public void registerPlatform(String platform) {
        wrappers.putIfAbsent(platform, new ModelResilienceWrapper(platform));
    }

    /**
     * 带弹性策略的 ChatModel 调用
     * 组合：熔断器 → 限流器 → 降级
     */
    public ChatModel getChatModelWithResilience(String platform, String modelName) {
        ModelResilienceWrapper wrapper = wrappers.get(platform);
        if (wrapper == null) {
            log.warn("平台 {} 未注册弹性包装器，直接调用", platform);
            return modelManager.getChatModel(platform, modelName);
        }

        return wrapper.execute(
            () -> modelManager.getChatModel(platform, modelName),
            () -> getFallbackChatModel(platform, modelName)
        );
    }

    /**
     * 轮询选择可用平台后调用（负载均衡）
     */
    public ChatModel getChatModelWithLoadBalance(List<String> platforms, String modelName) {
        if (platforms == null || platforms.isEmpty()) {
            throw new IllegalStateException("无可用的平台列表");
        }

        // 过滤出未熔断的平台
        List<String> available = platforms.stream()
                .filter(p -> {
                    ModelResilienceWrapper w = wrappers.get(p);
                    return w == null || w.getCircuitBreaker().isAllowed();
                })
                .toList();

        if (available.isEmpty()) {
            log.warn("所有平台均已熔断，使用原始列表轮询");
            available = platforms;
        }

        String selected = loadBalancer.roundRobinSelect(available);
        log.debug("负载均衡选择平台: {}", selected);
        return getChatModelWithResilience(selected, modelName);
    }

    /**
     * 降级：尝试其他平台的同名模型
     */
    private ChatModel getFallbackChatModel(String failedPlatform, String modelName) {
        for (Map.Entry<String, ModelResilienceWrapper> entry : wrappers.entrySet()) {
            String platform = entry.getKey();
            if (!platform.equals(failedPlatform) && entry.getValue().getCircuitBreaker().isAllowed()) {
                try {
                    log.info("降级到平台: {}", platform);
                    return modelManager.getChatModel(platform, modelName);
                } catch (Exception e) {
                    log.warn("降级平台 {} 也失败: {}", platform, e.getMessage());
                }
            }
        }
        throw new RuntimeException("所有平台降级均失败");
    }

    /**
     * 获取平台熔断状态
     */
    public Map<String, String> getCircuitBreakerStates() {
        Map<String, String> states = new ConcurrentHashMap<>();
        wrappers.forEach((name, w) ->
            states.put(name, w.getCircuitBreaker().getState().name())
        );
        return states;
    }

    /**
     * 重置平台熔断器
     */
    public void resetCircuitBreaker(String platform) {
        ModelResilienceWrapper wrapper = wrappers.get(platform);
        if (wrapper != null) {
            wrapper.getCircuitBreaker().onSuccess();
            log.info("已重置平台 {} 的熔断器", platform);
        }
    }
}
