package com.shiyu.ai.agent.implementation.evaluation.port;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * EvaluationRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface EvaluationRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param dataset 方法参数。
     */
    void insertDataset(EvalDataset dataset);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    Optional<EvalDataset> findDataset(String id, TenantId tenantId, long ownerUserId);

    /**
     * 创建并保存业务对象。
     *
     * @param evalCase 方法参数。
     */
    void insertCase(EvalCase evalCase);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param datasetId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<EvalCase> listCases(String datasetId, TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param run 方法参数。
     */
    void insertRun(EvalRun run);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<EvalRun> findRun(String id, TenantId tenantId, long ownerUserId);
}
