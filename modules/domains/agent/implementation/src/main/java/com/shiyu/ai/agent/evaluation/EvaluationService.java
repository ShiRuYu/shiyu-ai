package com.shiyu.ai.agent.evaluation;

import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class EvaluationService {
    private final EvaluationRepository repository;

    public EvaluationService() { this(new InMemoryEvaluationRepository()); }
    @Autowired public EvaluationService(EvaluationRepository repository) { this.repository = repository; }

    public EvalDataset createDataset(TenantId tenantId, long ownerUserId, String name, String description) {
        tenantId = requireTenant(tenantId);
        EvalDataset dataset = new EvalDataset(UUID.randomUUID().toString(), tenantId.value(), ownerUserId, name, description, Instant.now());
        repository.insertDataset(dataset);
        return dataset;
    }
    public EvalDataset requireDataset(String id, TenantId tenantId, long ownerUserId) { return repository.findDataset(id, requireTenant(tenantId), ownerUserId).orElseThrow(() -> new IllegalArgumentException("dataset not found")); }
    public EvalCase addCase(String datasetId, TenantId tenantId, long ownerUserId, String input, String expected, Map<String, Object> metadata) {
        tenantId = requireTenant(tenantId);
        requireDataset(datasetId, tenantId, ownerUserId);
        EvalCase value = new EvalCase(UUID.randomUUID().toString(), datasetId, tenantId.value(), input, expected, metadata, Instant.now());
        repository.insertCase(value);
        return value;
    }
    public List<EvalCase> cases(String datasetId, TenantId tenantId, long ownerUserId) { tenantId = requireTenant(tenantId); requireDataset(datasetId, tenantId, ownerUserId); return repository.listCases(datasetId, tenantId); }
    public EvalRun run(String datasetId, TenantId tenantId, long ownerUserId, String appVersionId, EvalMetric metric, java.util.function.Function<EvalCase, String> executor) {
        tenantId = requireTenant(tenantId);
        requireDataset(datasetId, tenantId, ownerUserId);
        List<EvalResult> results = cases(datasetId, tenantId, ownerUserId).stream().map(test -> new DeterministicEvaluator(metric).evaluate(test, executor.apply(test))).toList();
        double passRate = results.isEmpty() ? 0D : results.stream().filter(EvalResult::passed).count() / (double) results.size();
        EvalRun value = new EvalRun(UUID.randomUUID().toString(), datasetId, tenantId.value(), ownerUserId, appVersionId, metric, "COMPLETED", passRate, results, Instant.now(), Instant.now());
        repository.insertRun(value);
        return value;
    }
    public EvalRun requireRun(String id, TenantId tenantId, long ownerUserId) { return repository.findRun(id, requireTenant(tenantId), ownerUserId).orElseThrow(() -> new IllegalArgumentException("evaluation run not found")); }
    public List<EvalResult> results(String id, TenantId tenantId, long ownerUserId) { return requireRun(id, requireTenant(tenantId), ownerUserId).results(); }

    private static TenantId requireTenant(TenantId tenantId) { return Objects.requireNonNull(tenantId, "tenantId must not be null"); }
}
