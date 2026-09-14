package com.shiyu.ai.agent.implementation.evaluation.adapter;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;
import com.shiyu.ai.agent.implementation.evaluation.port.EvaluationRepository;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code InMemoryEvaluationRepository} 定义智能体模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
public class InMemoryEvaluationRepository implements EvaluationRepository {
    private final Map<String, EvalDataset> datasets = new ConcurrentHashMap<>();
    private final Map<String, List<EvalCase>> cases = new ConcurrentHashMap<>();
    private final Map<String, EvalRun> runs = new ConcurrentHashMap<>();

    /**
     * {@code insertDataset} 执行当前类型定义的业务操作。
     *
     * @param value 参数值，用于执行当前操作。
     */
    @Override
    public void insertDataset(EvalDataset value) {
        if (datasets.putIfAbsent(value.id(), value) != null)
            throw new IllegalStateException("dataset already exists");
    }

    /**
     * {@code findDataset} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<EvalDataset> findDataset(String id, TenantId tenantId, long ownerUserId) {
        long value = requireTenant(tenantId);
        return Optional.ofNullable(datasets.get(id))
                .filter(v -> v.tenantId() == value && v.ownerUserId() == ownerUserId);
    }

    /**
     * {@code insertCase} 执行当前类型定义的业务操作。
     *
     * @param value 参数值，用于执行当前操作。
     */
    @Override
    public void insertCase(EvalCase value) {
        List<EvalCase> datasetCases =
                cases.computeIfAbsent(value.datasetId(), ignored -> new ArrayList<>());
        synchronized (datasetCases) {
            datasetCases.add(value);
        }
    }

    /**
     * {@code listCases} 查询并返回当前操作所需的数据。
     *
     * @param datasetId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<EvalCase> listCases(String datasetId, TenantId tenantId) {
        long value = requireTenant(tenantId);
        List<EvalCase> datasetCases = cases.get(datasetId);
        if (datasetCases == null) return List.of();
        List<EvalCase> snapshot;
        synchronized (datasetCases) {
            snapshot = new ArrayList<>(datasetCases);
        }
        return snapshot.stream().filter(v -> v.tenantId() == value).toList();
    }

    /**
     * {@code insertRun} 执行当前类型定义的业务操作。
     *
     * @param value 参数值，用于执行当前操作。
     */
    @Override
    public void insertRun(EvalRun value) {
        if (runs.putIfAbsent(value.id(), value) != null) {
            throw new IllegalStateException("evaluation run already exists");
        }
    }

    /**
     * {@code findRun} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<EvalRun> findRun(String id, TenantId tenantId, long ownerUserId) {
        long value = requireTenant(tenantId);
        return Optional.ofNullable(runs.get(id))
                .filter(v -> v.tenantId() == value && v.ownerUserId() == ownerUserId);
    }

    private static long requireTenant(TenantId tenantId) {
        return java.util.Objects.requireNonNull(tenantId, "tenantId must not be null").value();
    }
}
