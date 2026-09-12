package com.shiyu.ai.agent.implementation.web;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalMetric;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;
import com.shiyu.ai.agent.implementation.evaluation.service.EvaluationService;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * {@code EvaluationController} 是智能体模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/agent/evaluations")
public class EvaluationController {

    /**
     * evaluations 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EvaluationService evaluations;

    /**
     * {@code EvaluationController} 创建并初始化当前类型实例。
     *
     * @param evaluations 参数值，用于执行当前操作。
     */
    public EvaluationController(EvaluationService evaluations) {
        this.evaluations = evaluations;
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/datasets")
    public Result<EvalDataset> create(@Valid @RequestBody DatasetRequest request) {
        return Result.success(
                evaluations.createDataset(tenant(), user(), request.name, request.description));
    }

    /**
     * {@code addCase} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/datasets/{id}/cases")
    public Result<EvalCase> addCase(
            @PathVariable String id, @Valid @RequestBody CaseRequest request) {
        return Result.success(
                evaluations.addCase(
                        id, tenant(), user(), request.input, request.expected, request.metadata));
    }

    /**
     * {@code cases} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/datasets/{id}/cases")
    public Result<List<EvalCase>> cases(@PathVariable String id) {
        return Result.success(evaluations.cases(id, tenant(), user()));
    }

    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/runs")
    public Result<EvalRun> run(@Valid @RequestBody RunRequest request) {
        return Result.success(
                evaluations.run(
                        request.datasetId,
                        tenant(),
                        user(),
                        request.appVersionId,
                        request.metric,
                        evaluationCase ->
                                request.outputs == null
                                        ? ""
                                        : request.outputs.getOrDefault(evaluationCase.id(), "")));
    }

    /**
     * {@code detail} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/runs/{id}")
    public Result<EvalRun> detail(@PathVariable String id) {
        return Result.success(evaluations.requireRun(id, tenant(), user()));
    }

    /**
     * {@code results} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/runs/{id}/results")
    public Result<List<EvalResult>> results(@PathVariable String id) {
        return Result.success(evaluations.results(id, tenant(), user()));
    }

    private TenantId tenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long user() {
        return ActorContextHttpAdapter.userId();
    }

    /**
     * {@code DatasetRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class DatasetRequest {
        private String name;
        /**
         * 描述，表示当前对象中的对应属性。
         */
        private String description;
    }

    /**
     * {@code CaseRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class CaseRequest {
        private String input;
        /**
         * expected 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String expected;
        /**
         * 元数据，表示当前对象中的对应属性。
         */
        private Map<String, Object> metadata;
    }

    /**
     * {@code RunRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class RunRequest {
        private String datasetId;
        /**
         * appVersionId 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String appVersionId;
        /**
         * metric 属性，保存当前对象中的业务数据或协作依赖。
         */
        private EvalMetric metric = EvalMetric.EXACT_MATCH;
        /**
         * outputs 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Map<String, String> outputs;
    }
}
