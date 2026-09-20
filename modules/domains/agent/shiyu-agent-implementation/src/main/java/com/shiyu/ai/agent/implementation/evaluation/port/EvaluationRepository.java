package com.shiyu.ai.agent.implementation.evaluation.port;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * 负责 Evaluation 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface EvaluationRepository {
    /**
     * 创建或保存 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataset 用于完成本次业务处理的 dataset 参数。
     */
    void insertDataset(EvalDataset dataset);

    /**
     * 查询 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<EvalDataset> findDataset(String id, TenantId tenantId, long ownerUserId);

    /**
     * 创建或保存 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param evalCase 用于完成本次业务处理的 evalCase 参数。
     */
    void insertCase(EvalCase evalCase);

    /**
     * 查询 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param datasetId 用于定位dataset的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<EvalCase> listCases(String datasetId, TenantId tenantId);

    /**
     * 创建或保存 Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     */
    void insertRun(EvalRun run);

    /**
     * 查询 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<EvalRun> findRun(String id, TenantId tenantId, long ownerUserId);
}
