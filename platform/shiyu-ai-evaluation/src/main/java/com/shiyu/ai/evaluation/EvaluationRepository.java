package com.shiyu.ai.evaluation;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository {
    void insertDataset(EvalDataset dataset);
    Optional<EvalDataset> findDataset(String id, long tenantId, long ownerUserId);
    void insertCase(EvalCase evalCase);
    List<EvalCase> listCases(String datasetId, long tenantId);
    void insertRun(EvalRun run);
    Optional<EvalRun> findRun(String id, long tenantId, long ownerUserId);
}
