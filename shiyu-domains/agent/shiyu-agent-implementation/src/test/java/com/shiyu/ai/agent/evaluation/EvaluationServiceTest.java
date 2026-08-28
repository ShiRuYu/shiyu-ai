package com.shiyu.ai.agent.evaluation;

import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationServiceTest {
    @Test
    void evaluatesAllMetricsAndEnforcesTenantOwnership() {
        EvaluationService service = new EvaluationService();
        TenantId tenant = new TenantId(1);
        EvalDataset dataset = service.createDataset(tenant, 2, "demo", null);
        EvalCase exact = service.addCase(dataset.id(), tenant, 2, "input", "yes", Map.of());
        service.addCase(dataset.id(), tenant, 2, "json", "x", Map.of("requiredField", "x"));
        assertEquals(2, service.cases(dataset.id(), tenant, 2).size());
        assertThrows(IllegalArgumentException.class, () -> service.cases(dataset.id(), new TenantId(9), 2));
        EvalRun run = service.run(dataset.id(), tenant, 2, "v1", EvalMetric.EXACT_MATCH, value -> value.input().equals(exact.input()) ? "yes" : "no");
        assertEquals(0.5, run.passRate()); assertEquals(2, service.results(run.id(), tenant, 2).size()); assertSame(run, service.requireRun(run.id(), tenant, 2));
        assertThrows(IllegalArgumentException.class, () -> service.requireRun(run.id(), new TenantId(9), 2));
    }

    @Test
    void evaluationCommandsRequireTypedTenantIdentity() {
        EvaluationService service = new EvaluationService();
        assertThrows(NullPointerException.class, () -> service.createDataset(null, 2, "demo", null));
        assertThrows(NullPointerException.class, () -> service.cases("missing", null, 2));
    }

    @Test
    void deterministicEvaluatorCoversJsonToolAndBudgetBranches() {
        EvalCase base = new EvalCase("c", "d", 1, "input", "Expected", Map.of("budget", 2, "requiredField", "name"), null);
        assertTrue(new DeterministicEvaluator(null).evaluate(base, " Expected ").passed());
        assertTrue(new DeterministicEvaluator(EvalMetric.CONTAINS).evaluate(base, "expected value").passed());
        assertFalse(new DeterministicEvaluator(EvalMetric.CONTAINS).evaluate(base, "").passed());
        assertTrue(new DeterministicEvaluator(EvalMetric.JSON_SCHEMA).evaluate(base, "{\"name\":1}").passed());
        assertFalse(new DeterministicEvaluator(EvalMetric.JSON_SCHEMA).evaluate(base, "bad").passed());
        assertTrue(new DeterministicEvaluator(EvalMetric.TOOL_CALL_SCHEMA).evaluate(base, "{\"name\":1}").passed());
        assertFalse(new DeterministicEvaluator(EvalMetric.TOOL_CALL_SCHEMA).evaluate(base, "{\"other\":1}").passed());
        assertTrue(new DeterministicEvaluator(EvalMetric.TOKEN_BUDGET).evaluate(base, "tiny").passed());
        assertTrue(new DeterministicEvaluator(EvalMetric.COST_BUDGET).evaluate(base, "tiny").passed());
        assertTrue(new DeterministicEvaluator(EvalMetric.CITATION_COVERAGE).evaluate(base, "expected citation").passed());
        assertTrue(new DeterministicEvaluator(EvalMetric.RETRIEVAL_HIT).evaluate(base, "expected hit").passed());
    }

    @Test
    void recordInvariantsAndInMemoryRepositoryAreTenantScoped() {
        assertThrows(IllegalArgumentException.class, () -> new EvalDataset("", 1, 2, "x", null, null));
        assertThrows(IllegalArgumentException.class, () -> new EvalCase("c", "d", 0, "x", null, null, null));
        InMemoryEvaluationRepository repository = new InMemoryEvaluationRepository();
        EvalDataset dataset = new EvalDataset("d", 1, 2, "name", null, null); repository.insertDataset(dataset);
        assertThrows(IllegalStateException.class, () -> repository.insertDataset(dataset));
        EvalCase value = new EvalCase("c", "d", 1, "x", null, null, null); repository.insertCase(value);
        assertEquals(1, repository.listCases("d", new TenantId(1)).size()); assertTrue(repository.findDataset("d", new TenantId(9), 2).isEmpty());
        EvalRun run = new EvalRun("r", "d", 1, 2, null, null, null, 0, List.of(), null, null); repository.insertRun(run);
        assertEquals(run, repository.findRun("r", new TenantId(1), 2).orElseThrow());
    }
}
