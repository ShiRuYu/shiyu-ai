package com.shiyu.ai.web.model;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.model.gateway.*;
import jakarta.validation.Valid;
import lombok.Data;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/v1")
@SaCheckPermission("model:admin")
public class ModelGatewayController {
    private final ModelRouter router;
    public ModelGatewayController(ModelRouter router) { this.router = router; }
    // /v1/models is owned by the OpenAI-compatible facade; the admin catalog exposes capabilities here.
    @GetMapping("/admin/model-providers") public Result<List<ModelProviderCapabilities>> models() { return Result.success(router.models()); }
    @GetMapping("/admin/model-routes") public Result<List<ModelRoutePolicy>> routes() { return Result.success(router.policies(tenant())); }
    @PostMapping("/admin/model-routes") public Result<ModelRoutePolicy> save(@Valid @RequestBody RouteRequest request) { ModelRoutePolicy p = new ModelRoutePolicy(java.util.UUID.randomUUID().toString(), tenant(), request.name, request.orderedModels, request.timeoutMs, request.fallbackOnError, request.maxTokens); router.savePolicy(p); return Result.success(p); }
    @PostMapping("/admin/model-routes/{id}/test") public Result<ModelProviderCapabilities> test(@PathVariable String id, @RequestBody(required = false) TestRequest request) {
        return Result.success(router.choose(id, tenant(), request == null ? Set.of("chat") : request.requiredFeatures));
    }
    @GetMapping("/admin/model-providers/{provider}/{model}/health") public Result<ProviderHealth> health(@PathVariable String provider, @PathVariable String model) { return Result.success(router.health(provider, model)); }
    @GetMapping("/admin/model-providers/{id}/health") public Result<ProviderHealth> healthById(@PathVariable String id) {
        int separator = id.indexOf(':');
        if (separator <= 0 || separator == id.length() - 1) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "provider id must be provider:model");
        return Result.success(router.health(id.substring(0, separator), id.substring(separator + 1)));
    }
    private long tenant() { Long id=UserContextHolder.getCurrentTenantId(); if(id==null) throw new IllegalStateException("tenant context is required"); return id; }
    @Data public static class RouteRequest { private String name; private List<String> orderedModels; private int timeoutMs = 30000; private boolean fallbackOnError = true; private long maxTokens = 16000; }
    @Data public static class TestRequest { private Set<String> requiredFeatures = Set.of("chat"); }
}
