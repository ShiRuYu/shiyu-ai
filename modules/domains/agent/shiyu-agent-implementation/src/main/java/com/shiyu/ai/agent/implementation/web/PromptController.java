package com.shiyu.ai.agent.implementation.web;

import com.shiyu.ai.agent.implementation.runtime.service.PromptService;
import com.shiyu.ai.agent.implementation.runtime.model.PromptTemplate;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 处理 提示词 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/conversation/prompts")
public class PromptController {
    /**
     * prompts 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PromptService prompts;

    /**
     * 执行 提示词 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param prompts 用于完成本次业务处理的 prompts 参数。
     */
    public PromptController(PromptService prompts) {
        this.prompts = prompts;
    }

    /**
     * 查询 提示词 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @GetMapping
    public Result<List<PromptTemplate>> list() {
        return Result.success(prompts.list(tenant(), user()));
    }

    /**
     * 创建或保存 提示词 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 提示词 相关操作生成的结果数据。
     */
    @PostMapping
    public Result<PromptTemplate> create(@Valid @RequestBody CreateRequest request) {
        return Result.success(
                prompts.create(
                        tenant(), user(), request.name, request.template, request.variables));
    }

    /**
     * 执行 提示词 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param publish 用于完成本次业务处理的 publish 参数。
     */
    @PostMapping("/{id}/publish")
    public Result<PromptTemplate> publish(@PathVariable String id) {
        return Result.success(prompts.publish(id, tenant(), user()));
    }

    /**
     * 执行 提示词 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param preview 用于完成本次业务处理的 preview 参数。
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
     * 封装 Create 操作所需的请求条件和输入数据。
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
     * 封装 Preview 操作所需的请求条件和输入数据。
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
