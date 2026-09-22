package com.shiyu.ai.model.implementation.web;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelRoutePolicy;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ProviderHealth;

import com.shiyu.ai.model.implementation.infrastructure.gateway.service.ModelRouter;

import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelProviderCapabilities;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 处理 模型 Gateway 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/model")
@SaCheckPermission("model:admin")
public class ModelGatewayController {
    /**
     * router 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ModelRouter router;

    /**
     * 执行 模型 Gateway 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param router 用于完成本次业务处理的 router 参数。
     */
    public ModelGatewayController(ModelRouter router) {
        this.router = router;
    }

    /**
     * 查询 模型 Gateway 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param providers 用于完成本次业务处理的 providers 参数。
     */
    @GetMapping("/providers")
    public Result<List<ModelProviderCapabilities>> models() {
        return Result.success(router.models());
    }

    /**
     * 查询 模型 Gateway 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param routes 用于完成本次业务处理的 routes 参数。
     */
    @GetMapping("/routes")
    public Result<List<ModelRoutePolicy>> routes() {
        return Result.success(router.policies(tenant()));
    }

    /**
     * 执行 模型 Gateway 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param routes 用于完成本次业务处理的 routes 参数。
     */
    @PostMapping("/routes")
    public Result<ModelRoutePolicy> save(@Valid @RequestBody RouteRequest request) {
        ModelRoutePolicy p =
                new ModelRoutePolicy(
                        java.util.UUID.randomUUID().toString(),
                        tenant().value(),
                        request.name,
                        request.orderedModels,
                        request.timeoutMs,
                        request.fallbackOnError,
                        request.maxTokens);
        router.savePolicy(p);
        return Result.success(p);
    }

    /**
     * 执行 模型 Gateway 相关业务数据，并返回处理结果。
     *
     * @param test 用于完成本次业务处理的 test 参数。
     * @return 返回 模型 Gateway 相关操作生成的结果数据。
     */
    @PostMapping("/routes/{id}/test")
    public Result<ModelProviderCapabilities> test(
            @PathVariable String id, @RequestBody(required = false) TestRequest request) {
        return Result.success(
                router.choose(
                        id, tenant(), request == null ? Set.of("chat") : request.requiredFeatures));
    }

    /**
     * 查询 模型 Gateway 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param health 用于完成本次业务处理的 health 参数。
     */
    @GetMapping("/providers/{provider}/{model}/health")
    public Result<ProviderHealth> health(
            @PathVariable String provider, @PathVariable String model) {
        return Result.success(router.health(provider, model));
    }

    /**
     * 查询 模型 Gateway 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param health 用于完成本次业务处理的 health 参数。
     */
    @GetMapping("/providers/{id}/health")
    public Result<ProviderHealth> healthById(@PathVariable String id) {
        int separator = id.indexOf(':');
        if (separator <= 0 || separator == id.length() - 1)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "provider id must be provider:model");
        return Result.success(
                router.health(id.substring(0, separator), id.substring(separator + 1)));
    }

    private TenantId tenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    /**
     * 封装 Route 操作所需的请求条件和输入数据。
     */
    @Data
    public static class RouteRequest {
        private String name;
        /**
         * orderedModels 属性，保存当前对象中的业务数据或协作依赖。
         */
        private List<String> orderedModels;
        /**
         * timeoutMs 属性，保存当前对象中的业务数据或协作依赖。
         */
        private int timeoutMs = 30000;
        /**
         * fallbackOnError 属性，保存当前对象中的业务数据或协作依赖。
         */
        private boolean fallbackOnError = true;
        /**
         * maxTokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        private long maxTokens = 16000;
    }

    /**
     * 封装 Test 操作所需的请求条件和输入数据。
     */
    @Data
    public static class TestRequest {
        private Set<String> requiredFeatures = Set.of("chat");
    }
}
