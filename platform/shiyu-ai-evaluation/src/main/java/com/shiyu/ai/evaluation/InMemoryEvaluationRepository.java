package com.shiyu.ai.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEvaluationRepository implements EvaluationRepository {
    private final Map<String, EvalDataset> datasets = new ConcurrentHashMap<>();
    private final Map<String, List<EvalCase>> cases = new ConcurrentHashMap<>();
    private final Map<String, EvalRun> runs = new ConcurrentHashMap<>();
    @Override public void insertDataset(EvalDataset value) { if (datasets.putIfAbsent(value.id(), value) != null) throw new IllegalStateException("dataset already exists"); }
    @Override public Optional<EvalDataset> findDataset(String id, long tenantId, long ownerUserId) { return Optional.ofNullable(datasets.get(id)).filter(v -> v.tenantId() == tenantId && v.ownerUserId() == ownerUserId); }
    @Override public void insertCase(EvalCase value) { cases.computeIfAbsent(value.datasetId(), ignored -> new ArrayList<>()).add(value); }
    @Override public List<EvalCase> listCases(String datasetId, long tenantId) { return cases.getOrDefault(datasetId, List.of()).stream().filter(v -> v.tenantId() == tenantId).toList(); }
    @Override public void insertRun(EvalRun value) { runs.putIfAbsent(value.id(), value); }
    @Override public Optional<EvalRun> findRun(String id, long tenantId, long ownerUserId) { return Optional.ofNullable(runs.get(id)).filter(v -> v.tenantId() == tenantId && v.ownerUserId() == ownerUserId); }
}
