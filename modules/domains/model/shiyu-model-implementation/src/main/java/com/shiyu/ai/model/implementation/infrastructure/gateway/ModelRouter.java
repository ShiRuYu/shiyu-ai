package com.shiyu.ai.model.implementation.infrastructure.gateway;

import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * {@code ModelRouter} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@SuppressWarnings("this-escape")
@Service
public class ModelRouter {
    private final Map<String, ModelProviderCapabilities> capabilities = new ConcurrentHashMap<>();
    private final Map<String, ModelRoutePolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, ProviderHealth> health = new ConcurrentHashMap<>();

    /**
     * {@code ModelRouter} 创建并初始化当前类型实例。
     */
    public ModelRouter() {
        ModelProviderCapabilities configured =
                new ModelProviderCapabilities(
                        "default",
                        "configured",
                        java.util.Set.of("chat", "stream", "tool_calls", "structured"),
                        128000);
        register(configured);
        register(
                new ModelProviderCapabilities(
                        "DEEPSEEK",
                        "deepseek-v4-flash",
                        java.util.Set.of(
                                "chat",
                                "stream",
                                "tool_calls",
                                "parallel_tool_calls",
                                "structured",
                                "multimodal",
                                "reasoning",
                                "json_schema"),
                        128000,
                        true,
                        true,
                        true,
                        true,
                        true,
                        List.of("low", "medium", "high"),
                        8192,
                        true,
                        true,
                        true));
        register(
                new ModelProviderCapabilities(
                        "OPENAI",
                        "gpt-4o",
                        java.util.Set.of(
                                "chat",
                                "stream",
                                "tool_calls",
                                "parallel_tool_calls",
                                "structured",
                                "multimodal",
                                "json_schema"),
                        128000,
                        true,
                        true,
                        true,
                        true,
                        true,
                        List.of(),
                        16384,
                        true,
                        true,
                        true));
    }

    /**
     * {@code register} 写入或更新当前模块中的业务数据。
     *
     * @param value 参数值，用于执行当前操作。
     */
    public void register(ModelProviderCapabilities value) {
        capabilities.put(key(value.provider(), value.model()), value);
        health.put(
                key(value.provider(), value.model()),
                new ProviderHealth(
                        value.provider(), value.model(), true, 0, Instant.now(), "registered"));
    }

    /**
     * {@code models} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<ModelProviderCapabilities> models() {
        return capabilities.values().stream().toList();
    }

    /**
     * {@code savePolicy} 写入或更新当前模块中的业务数据。
     *
     * @param policy 参数值，用于执行当前操作。
     */
    public void savePolicy(ModelRoutePolicy policy) {
        if (policy == null) throw new IllegalArgumentException("route is required");
        if (policy.orderedModels().stream()
                .map(this::parse)
                .anyMatch(model -> !capabilities.containsKey(model))) {
            throw new IllegalArgumentException("route contains unknown model");
        }
        policies.put(policy.id(), policy);
    }

    /**
     * {@code policies} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<ModelRoutePolicy> policies(TenantId tenantId) {
        return policies.values().stream()
                .filter(p -> tenantId != null && p.tenantId() == tenantId.value())
                .toList();
    }

    /**
     * {@code requirePolicy} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ModelRoutePolicy requirePolicy(String id, TenantId tenantId) {
        return Optional.ofNullable(policies.get(id))
                .filter(p -> tenantId != null && p.tenantId() == tenantId.value())
                .orElseThrow(() -> new IllegalArgumentException("model route not found"));
    }

    /**
     * {@code choose} 执行当前类型定义的业务操作。
     *
     * @param policyId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param requiredFeatures 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ModelProviderCapabilities choose(
            String policyId, TenantId tenantId, java.util.Set<String> requiredFeatures) {
        return candidates(policyId, tenantId, requiredFeatures).stream()
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "no healthy model matches required capabilities"));
    }

    /**
     * 判断模型router是否满足条件。
     *
     * @param policyId policyId 参数。
     * @param tenantId 租户标识。
     * @param requiredFeatures requiredFeatures 参数。
     *
     * @return 结果列表。
     */
    public List<ModelProviderCapabilities> candidates(
            String policyId, TenantId tenantId, java.util.Set<String> requiredFeatures) {
        ModelRoutePolicy policy = requirePolicy(policyId, tenantId);
        return policy.orderedModels().stream()
                .map(this::parse)
                .map(capabilities::get)
                .filter(java.util.Objects::nonNull)
                .filter(c -> requiredFeatures == null || c.features().containsAll(requiredFeatures))
                .filter(c -> health(c.provider(), c.model()).healthy())
                .toList();
    }

    /**
     * 执行使用备用。
     *
     * @param policyId policyId 参数。
     * @param tenantId 租户标识。
     * @param requiredFeatures requiredFeatures 参数。
     * @param call call 参数。
     *
     * @return 结果列表。
     */
    public <T> T executeWithFallback(
            String policyId,
            TenantId tenantId,
            java.util.Set<String> requiredFeatures,
            Function<ModelProviderCapabilities, T> call) {
        ModelRoutePolicy policy = requirePolicy(policyId, tenantId);
        RuntimeException last = null;
        for (ModelProviderCapabilities candidate :
                candidates(policyId, tenantId, requiredFeatures)) {
            try {
                T result = call.apply(candidate);
                if (result != null) return result;
            } catch (RuntimeException ex) {
                last = ex;
                markFailure(candidate.provider(), candidate.model(), ex.getClass().getSimpleName());
                if (!policy.fallbackOnError()) throw ex;
            }
        }
        throw last == null
                ? new IllegalStateException("no healthy model matches required capabilities")
                : last;
    }

    /**
     * {@code health} 执行当前类型定义的业务操作。
     *
     * @param provider 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ProviderHealth health(String provider, String model) {
        return health.getOrDefault(
                key(provider, model),
                new ProviderHealth(provider, model, false, 0, Instant.now(), "unknown model"));
    }

    /**
     * {@code markFailure} 执行当前类型定义的业务操作。
     *
     * @param provider 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     */
    public void markFailure(String provider, String model, String message) {
        String modelKey = key(provider, model);
        health.compute(
                modelKey,
                (ignored, old) -> {
                    int failures = old == null ? 1 : old.consecutiveFailures() + 1;
                    return new ProviderHealth(
                            provider, model, failures < 3, failures, Instant.now(), message);
                });
    }

    private String key(String provider, String model) {
        return provider + ":" + model;
    }

    private String parse(String model) {
        return model.contains(":") ? model : key("default", model);
    }
}
