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
 * 提供 Evaluation 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 执行 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param repository 用于完成本次业务处理的 repository 参数。
     */
    @Autowired
    public EvaluationService(EvaluationRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建或保存 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param description 用于完成本次业务处理的 description 参数。
     * @return 返回 Evaluation 相关操作生成的结果数据。
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
     * 获取并校验 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 Evaluation 相关操作生成的结果数据。
     */
    public EvalDataset requireDataset(String id, TenantId tenantId, long ownerUserId) {
        return repository
                .findDataset(id, requireTenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("dataset not found"));
    }

    /**
     * 创建或保存 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param datasetId 用于定位dataset的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param input 用于完成本次业务处理的 input 参数。
     * @param expected 用于完成本次业务处理的 expected 参数。
     * @param metadata 用于完成本次业务处理的 metadata 参数。
     * @return 返回 Evaluation 相关操作生成的结果数据。
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
     * 执行 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param datasetId 用于定位dataset的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<EvalCase> cases(String datasetId, TenantId tenantId, long ownerUserId) {
        tenantId = requireTenant(tenantId);
        requireDataset(datasetId, tenantId, ownerUserId);
        return repository.listCases(datasetId, tenantId);
    }

    /**
     * 执行 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param datasetId 用于定位dataset的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param appVersionId 用于定位app Version的标识。
     * @param metric 用于完成本次业务处理的 metric 参数。
     * @param executor 用于完成本次业务处理的 executor 参数。
     * @return 返回 Evaluation 相关操作生成的结果数据。
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
     * 获取并校验 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 Evaluation 相关操作生成的结果数据。
     */
    public EvalRun requireRun(String id, TenantId tenantId, long ownerUserId) {
        return repository
                .findRun(id, requireTenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("evaluation run not found"));
    }

    /**
     * 执行 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<EvalResult> results(String id, TenantId tenantId, long ownerUserId) {
        return requireRun(id, requireTenant(tenantId), ownerUserId).results();
    }

    private static TenantId requireTenant(TenantId tenantId) {
        return Objects.requireNonNull(tenantId, "tenantId must not be null");
    }
}
