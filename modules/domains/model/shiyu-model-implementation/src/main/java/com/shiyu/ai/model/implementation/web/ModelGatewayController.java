package com.shiyu.ai.model.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.implementation.infrastructure.gateway.*;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * {@code ModelGatewayController} 是模型模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
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
     * {@code ModelGatewayController} 创建并初始化当前类型实例。
     *
     * @param router 参数值，用于执行当前操作。
     */
    public ModelGatewayController(ModelRouter router) {
        this.router = router;
    }

    /**
     * {@code models} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/providers")
    public Result<List<ModelProviderCapabilities>> models() {
        return Result.success(router.models());
    }

    /**
     * {@code routes} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/routes")
    public Result<List<ModelRoutePolicy>> routes() {
        return Result.success(router.policies(tenant()));
    }

    /**
     * {@code save} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code test} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/routes/{id}/test")
    public Result<ModelProviderCapabilities> test(
            @PathVariable String id, @RequestBody(required = false) TestRequest request) {
        return Result.success(
                router.choose(
                        id, tenant(), request == null ? Set.of("chat") : request.requiredFeatures));
    }

    /**
     * {@code health} 执行当前类型定义的业务操作。
     *
     * @param provider 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/providers/{provider}/{model}/health")
    public Result<ProviderHealth> health(
            @PathVariable String provider, @PathVariable String model) {
        return Result.success(router.health(provider, model));
    }

    /**
     * {@code healthById} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code RouteRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
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
     * {@code TestRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class TestRequest {
        private Set<String> requiredFeatures = Set.of("chat");
    }
}
