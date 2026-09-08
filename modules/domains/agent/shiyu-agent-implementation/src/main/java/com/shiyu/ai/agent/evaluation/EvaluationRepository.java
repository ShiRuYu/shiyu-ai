package com.shiyu.ai.agent.evaluation;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import java.util.Optional;

public interface EvaluationRepository {
    void insertDataset(EvalDataset dataset);
    Optional<EvalDataset> findDataset(String id, TenantId tenantId, long ownerUserId);
    void insertCase(EvalCase evalCase);
    List<EvalCase> listCases(String datasetId, TenantId tenantId);
    void insertRun(EvalRun run);
    Optional<EvalRun> findRun(String id, TenantId tenantId, long ownerUserId);
}
