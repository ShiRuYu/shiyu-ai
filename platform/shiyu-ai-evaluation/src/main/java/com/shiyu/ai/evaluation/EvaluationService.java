package com.shiyu.ai.evaluation;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class EvaluationService {
    private final EvaluationRepository repository;

    public EvaluationService() { this(new InMemoryEvaluationRepository()); }
    @Autowired public EvaluationService(EvaluationRepository repository) { this.repository = repository; }

    public EvalDataset createDataset(long tenantId, long ownerUserId, String name, String description) {
        EvalDataset dataset = new EvalDataset(UUID.randomUUID().toString(), tenantId, ownerUserId, name, description, Instant.now());
        repository.insertDataset(dataset);
        return dataset;
    }
    public EvalDataset requireDataset(String id, long tenantId, long ownerUserId) { return repository.findDataset(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("dataset not found")); }
    public EvalCase addCase(String datasetId, long tenantId, long ownerUserId, String input, String expected, Map<String, Object> metadata) {
        requireDataset(datasetId, tenantId, ownerUserId);
        EvalCase value = new EvalCase(UUID.randomUUID().toString(), datasetId, tenantId, input, expected, metadata, Instant.now());
        repository.insertCase(value);
        return value;
    }
    public List<EvalCase> cases(String datasetId, long tenantId, long ownerUserId) { requireDataset(datasetId, tenantId, ownerUserId); return repository.listCases(datasetId, tenantId); }
    public EvalRun run(String datasetId, long tenantId, long ownerUserId, String appVersionId, EvalMetric metric, java.util.function.Function<EvalCase, String> executor) {
        requireDataset(datasetId, tenantId, ownerUserId);
        List<EvalResult> results = cases(datasetId, tenantId, ownerUserId).stream().map(test -> new DeterministicEvaluator(metric).evaluate(test, executor.apply(test))).toList();
        double passRate = results.isEmpty() ? 0D : results.stream().filter(EvalResult::passed).count() / (double) results.size();
        EvalRun value = new EvalRun(UUID.randomUUID().toString(), datasetId, tenantId, ownerUserId, appVersionId, metric, "COMPLETED", passRate, results, Instant.now(), Instant.now());
        repository.insertRun(value);
        return value;
    }
    public EvalRun requireRun(String id, long tenantId, long ownerUserId) { return repository.findRun(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("evaluation run not found")); }
    public List<EvalResult> results(String id, long tenantId, long ownerUserId) { return requireRun(id, tenantId, ownerUserId).results(); }
}
