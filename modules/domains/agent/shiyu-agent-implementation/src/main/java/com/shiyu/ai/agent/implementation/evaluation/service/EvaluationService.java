package com.shiyu.ai.agent.implementation.evaluation.service;
import com.shiyu.ai.agent.implementation.evaluation.adapter.InMemoryEvaluationRepository;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalMetric;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;
import com.shiyu.ai.agent.implementation.evaluation.port.EvaluationRepository;

import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * {@code EvaluationService} 定义智能体模块的应用服务能力，供上层用例调用。
 */
@Service
public class EvaluationService {
    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final EvaluationRepository repository;

    /**
     * {@code EvaluationService} 创建并初始化当前类型实例。
     */
    public EvaluationService() {
        this(new InMemoryEvaluationRepository());
    }

    /**
     * {@code EvaluationService} 创建并初始化当前类型实例。
     *
     * @param repository 参数值，用于执行当前操作。
     */
    @Autowired
    public EvaluationService(EvaluationRepository repository) {
        this.repository = repository;
    }

    /**
     * {@code createDataset} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param description 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public EvalDataset createDataset(
            TenantId tenantId, long ownerUserId, String name, String description) {
        tenantId = requireTenant(tenantId);
        EvalDataset dataset =
                new EvalDataset(
                        UUID.randomUUID().toString(),
                        tenantId.value(),
                        ownerUserId,
                        name,
                        description,
                        Instant.now());
        repository.insertDataset(dataset);
        return dataset;
    }

    /**
     * {@code requireDataset} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public EvalDataset requireDataset(String id, TenantId tenantId, long ownerUserId) {
        return repository
                .findDataset(id, requireTenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("dataset not found"));
    }

    /**
     * {@code addCase} 执行当前类型定义的业务操作。
     *
     * @param datasetId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     * @param expected 参数值，用于执行当前操作。
     * @param metadata 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public EvalCase addCase(
            String datasetId,
            TenantId tenantId,
            long ownerUserId,
            String input,
            String expected,
            Map<String, Object> metadata) {
        tenantId = requireTenant(tenantId);
        requireDataset(datasetId, tenantId, ownerUserId);
        EvalCase value =
                new EvalCase(
                        UUID.randomUUID().toString(),
                        datasetId,
                        tenantId.value(),
                        input,
                        expected,
                        metadata,
                        Instant.now());
        repository.insertCase(value);
        return value;
    }

    /**
     * {@code cases} 执行当前类型定义的业务操作。
     *
     * @param datasetId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<EvalCase> cases(String datasetId, TenantId tenantId, long ownerUserId) {
        tenantId = requireTenant(tenantId);
        requireDataset(datasetId, tenantId, ownerUserId);
        return repository.listCases(datasetId, tenantId);
    }

    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param datasetId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param appVersionId 参数值，用于执行当前操作。
     * @param metric 参数值，用于执行当前操作。
     * @param executor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public EvalRun run(
            String datasetId,
            TenantId tenantId,
            long ownerUserId,
            String appVersionId,
            EvalMetric metric,
            java.util.function.Function<EvalCase, String> executor) {
        tenantId = requireTenant(tenantId);
        requireDataset(datasetId, tenantId, ownerUserId);
        List<EvalResult> results =
                cases(datasetId, tenantId, ownerUserId).stream()
                        .map(
                                test ->
                                        new DeterministicEvaluator(metric)
                                                .evaluate(test, executor.apply(test)))
                        .toList();
        double passRate =
                results.isEmpty()
                        ? 0D
                        : results.stream().filter(EvalResult::passed).count()
                                / (double) results.size();
        EvalRun value =
                new EvalRun(
                        UUID.randomUUID().toString(),
                        datasetId,
                        tenantId.value(),
                        ownerUserId,
                        appVersionId,
                        metric,
                        "COMPLETED",
                        passRate,
                        results,
                        Instant.now(),
                        Instant.now());
        repository.insertRun(value);
        return value;
    }

    /**
     * {@code requireRun} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public EvalRun requireRun(String id, TenantId tenantId, long ownerUserId) {
        return repository
                .findRun(id, requireTenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("evaluation run not found"));
    }

    /**
     * {@code results} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<EvalResult> results(String id, TenantId tenantId, long ownerUserId) {
        return requireRun(id, requireTenant(tenantId), ownerUserId).results();
    }

    private static TenantId requireTenant(TenantId tenantId) {
        return Objects.requireNonNull(tenantId, "tenantId must not be null");
    }
}
