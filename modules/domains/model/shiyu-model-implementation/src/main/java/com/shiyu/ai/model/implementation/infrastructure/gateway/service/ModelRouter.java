package com.shiyu.ai.model.implementation.infrastructure.gateway.service;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelRoutePolicy;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ProviderHealth;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelProviderCapabilities;

import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 根据请求上下文解析或路由 模型 相关的处理能力。
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
     * 创建或保存 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     */
    public void register(ModelProviderCapabilities value) {
        capabilities.put(key(value.provider(), value.model()), value);
        health.put(
                key(value.provider(), value.model()),
                new ProviderHealth(
                        value.provider(), value.model(), true, 0, Instant.now(), "registered"));
    }

    /**
     * 执行 模型 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<ModelProviderCapabilities> models() {
        return capabilities.values().stream().toList();
    }

    /**
     * 创建或保存 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param policy 用于完成本次业务处理的 policy 参数。
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
     * 执行 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<ModelRoutePolicy> policies(TenantId tenantId) {
        return policies.values().stream()
                .filter(p -> tenantId != null && p.tenantId() == tenantId.value())
                .toList();
    }

    /**
     * 获取并校验 模型 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 模型 相关操作生成的结果数据。
     */
    public ModelRoutePolicy requirePolicy(String id, TenantId tenantId) {
        return Optional.ofNullable(policies.get(id))
                .filter(p -> tenantId != null && p.tenantId() == tenantId.value())
                .orElseThrow(() -> new IllegalArgumentException("model route not found"));
    }

    /**
     * 解析或路由 模型 相关业务数据，并返回处理结果。
     *
     * @param policyId 用于定位policy的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param requiredFeatures 用于完成本次业务处理的 requiredFeatures 参数。
     * @return 返回 模型 相关操作生成的结果数据。
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
     * 校验或判断 模型 相关业务数据，并返回处理结果。
     *
     * @param policyId 用于定位policy的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param requiredFeatures 用于完成本次业务处理的 requiredFeatures 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 调用 模型 相关业务数据，并返回处理结果。
     *
     * @param policyId 用于定位policy的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param requiredFeatures 用于完成本次业务处理的 requiredFeatures 参数。
     * @param call 用于完成本次业务处理的 call 参数。
     * @return 返回 模型 相关操作生成的结果数据。
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
     * 执行 模型 相关业务数据，并返回处理结果。
     *
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @return 返回 模型 相关操作生成的结果数据。
     */
    public ProviderHealth health(String provider, String model) {
        return health.getOrDefault(
                key(provider, model),
                new ProviderHealth(provider, model, false, 0, Instant.now(), "unknown model"));
    }

    /**
     * 执行 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @param message 本次流程携带的事件或业务数据。
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
