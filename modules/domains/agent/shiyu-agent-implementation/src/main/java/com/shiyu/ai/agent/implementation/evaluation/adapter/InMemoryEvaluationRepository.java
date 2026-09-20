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
 * 负责 In 记忆 Evaluation 的持久化查询、保存和删除，并维护数据访问边界。
 */
public class InMemoryEvaluationRepository implements EvaluationRepository {
    private final Map<String, EvalDataset> datasets = new ConcurrentHashMap<>();
    private final Map<String, List<EvalCase>> cases = new ConcurrentHashMap<>();
    private final Map<String, EvalRun> runs = new ConcurrentHashMap<>();

    /**
     * 创建或保存 In 记忆 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     */
    @Override
    public void insertDataset(EvalDataset value) {
        if (datasets.putIfAbsent(value.id(), value) != null)
            throw new IllegalStateException("dataset already exists");
    }

    /**
     * 查询 In 记忆 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<EvalDataset> findDataset(String id, TenantId tenantId, long ownerUserId) {
        long value = requireTenant(tenantId);
        return Optional.ofNullable(datasets.get(id))
                .filter(v -> v.tenantId() == value && v.ownerUserId() == ownerUserId);
    }

    /**
     * 创建或保存 In 记忆 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param value 用于完成本次业务处理的 value 参数。
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
     * 查询 In 记忆 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param datasetId 用于定位dataset的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 创建或保存 In 记忆 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     */
    @Override
    public void insertRun(EvalRun value) {
        if (runs.putIfAbsent(value.id(), value) != null) {
            throw new IllegalStateException("evaluation run already exists");
        }
    }

    /**
     * 查询 In 记忆 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
