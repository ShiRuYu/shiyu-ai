package com.shiyu.ai.agent.implementation.web;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalMetric;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;
import com.shiyu.ai.agent.implementation.evaluation.service.EvaluationService;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 处理 Evaluation 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/agent/evaluations")
public class EvaluationController {

    /**
     * evaluations 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EvaluationService evaluations;

    /**
     * 执行 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param evaluations 用于完成本次业务处理的 evaluations 参数。
     */
    public EvaluationController(EvaluationService evaluations) {
        this.evaluations = evaluations;
    }

    /**
     * 执行 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param datasets 用于完成本次业务处理的 datasets 参数。
     */
    @PostMapping("/datasets")
    public Result<EvalDataset> create(@Valid @RequestBody DatasetRequest request) {
        return Result.success(
                evaluations.createDataset(tenant(), user(), request.name, request.description));
    }

    /**
     * 执行 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param cases 用于完成本次业务处理的 cases 参数。
     */
    @PostMapping("/datasets/{id}/cases")
    public Result<EvalCase> addCase(
            @PathVariable String id, @Valid @RequestBody CaseRequest request) {
        return Result.success(
                evaluations.addCase(
                        id, tenant(), user(), request.input, request.expected, request.metadata));
    }

    /**
     * 查询 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param cases 用于完成本次业务处理的 cases 参数。
     */
    @GetMapping("/datasets/{id}/cases")
    public Result<List<EvalCase>> cases(@PathVariable String id) {
        return Result.success(evaluations.cases(id, tenant(), user()));
    }

    /**
     * 执行 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param runs 用于完成本次业务处理的 runs 参数。
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
     * 查询 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    @GetMapping("/runs/{id}")
    public Result<EvalRun> detail(@PathVariable String id) {
        return Result.success(evaluations.requireRun(id, tenant(), user()));
    }

    /**
     * 查询 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param results 用于完成本次业务处理的 results 参数。
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
     * 封装 Dataset 操作所需的请求条件和输入数据。
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
     * 封装 Case 操作所需的请求条件和输入数据。
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
     * 封装 运行 操作所需的请求条件和输入数据。
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
