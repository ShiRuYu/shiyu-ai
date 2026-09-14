package com.shiyu.ai.agent.implementation.web;

import com.shiyu.ai.agent.implementation.runtime.service.PromptService;
import com.shiyu.ai.agent.implementation.runtime.model.PromptTemplate;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * {@code PromptController} 是智能体模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/conversation/prompts")
public class PromptController {
    /**
     * prompts 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PromptService prompts;

    /**
     * {@code PromptController} 创建并初始化当前类型实例。
     *
     * @param prompts 参数值，用于执行当前操作。
     */
    public PromptController(PromptService prompts) {
        this.prompts = prompts;
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping
    public Result<List<PromptTemplate>> list() {
        return Result.success(prompts.list(tenant(), user()));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping
    public Result<PromptTemplate> create(@Valid @RequestBody CreateRequest request) {
        return Result.success(
                prompts.create(
                        tenant(), user(), request.name, request.template, request.variables));
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/{id}/publish")
    public Result<PromptTemplate> publish(@PathVariable String id) {
        return Result.success(prompts.publish(id, tenant(), user()));
    }

    /**
     * {@code preview} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/preview")
    public Result<PromptService.PromptPreview> preview(@Valid @RequestBody PreviewRequest request) {
        return Result.success(prompts.preview(request.template, request.variables));
    }

    private TenantId tenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long user() {
        return ActorContextHttpAdapter.userId();
    }

    /**
     * {@code CreateRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class CreateRequest {
        /**
         * 名称，表示当前对象中的对应属性。
         */
        private String name;
        /**
         * 模板，表示当前对象中的对应属性。
         */
        private String template;
        /**
         * variables 属性，保存当前对象中的业务数据或协作依赖。
         */
        private List<String> variables;
    }

    /**
     * {@code PreviewRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class PreviewRequest {
        private String template;
        /**
         * variables 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Map<String, Object> variables;
    }
}
