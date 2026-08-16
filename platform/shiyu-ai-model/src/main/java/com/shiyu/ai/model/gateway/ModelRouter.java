package com.shiyu.ai.model.gateway;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ModelRouter {
    private final Map<String, ModelProviderCapabilities> capabilities = new ConcurrentHashMap<>();
    private final Map<String, ModelRoutePolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, ProviderHealth> health = new ConcurrentHashMap<>();
    public ModelRouter() {
        ModelProviderCapabilities configured = new ModelProviderCapabilities("default", "configured", java.util.Set.of("chat", "stream", "tool_calls", "structured"), 128000);
        String key = key(configured.provider(), configured.model());
        capabilities.put(key, configured);
        health.put(key, new ProviderHealth(configured.provider(), configured.model(), true, 0, Instant.now(), "registered"));
    }
    public void register(ModelProviderCapabilities value) { capabilities.put(key(value.provider(), value.model()), value); health.put(key(value.provider(), value.model()), new ProviderHealth(value.provider(), value.model(), true, 0, Instant.now(), "registered")); }
    public List<ModelProviderCapabilities> models() { return capabilities.values().stream().toList(); }
    public void savePolicy(ModelRoutePolicy policy) {
        if (policy == null) throw new IllegalArgumentException("route is required");
        if (policy.orderedModels().stream().map(this::parse).anyMatch(model -> !capabilities.containsKey(model))) {
            throw new IllegalArgumentException("route contains unknown model");
        }
        policies.put(policy.id(), policy);
    }
    public List<ModelRoutePolicy> policies(long tenantId) { return policies.values().stream().filter(p -> p.tenantId() == tenantId).toList(); }
    public ModelRoutePolicy requirePolicy(String id, long tenantId) { return Optional.ofNullable(policies.get(id)).filter(p -> p.tenantId() == tenantId).orElseThrow(() -> new IllegalArgumentException("model route not found")); }
    public ModelProviderCapabilities choose(String policyId, long tenantId, java.util.Set<String> requiredFeatures) {
        ModelRoutePolicy policy = requirePolicy(policyId, tenantId);
        return policy.orderedModels().stream().map(this::parse)
                .map(capabilities::get)
                .filter(java.util.Objects::nonNull)
                .filter(c -> requiredFeatures == null || c.features().containsAll(requiredFeatures))
                .filter(c -> health(c.provider(), c.model()).healthy())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no healthy model matches required capabilities"));
    }
    public ProviderHealth health(String provider, String model) { return health.getOrDefault(key(provider, model), new ProviderHealth(provider, model, false, 0, Instant.now(), "unknown model")); }
    public void markFailure(String provider, String model, String message) { String key=key(provider,model); ProviderHealth old=health(provider,model); int failures = old.consecutiveFailures() + 1; health.put(key,new ProviderHealth(provider,model,failures < 3,failures,Instant.now(),message)); }
    private String key(String provider,String model){return provider+":"+model;}
    private String parse(String model){return model.contains(":")?model:key("default",model);}
}
