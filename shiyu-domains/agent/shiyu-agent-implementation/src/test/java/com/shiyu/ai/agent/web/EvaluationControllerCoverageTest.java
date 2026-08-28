package com.shiyu.ai.agent.web;

import com.shiyu.ai.agent.evaluation.*;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EvaluationControllerCoverageTest {
    @Test
    void mapsDatasetCaseRunAndResultEndpointsIncludingMissingOutput() {
        EvaluationService service = mock(EvaluationService.class);
        EvaluationController controller = new EvaluationController(service);
        EvalDataset dataset = new EvalDataset("d1", 7, 9, "Demo", "desc", Instant.now());
        EvalCase testCase = new EvalCase("c1", "d1", 7, "input", "expected", Map.of(), Instant.now());
        EvalResult result = new EvalResult("c1", EvalMetric.EXACT_MATCH, 1D, true, "ok");
        EvalRun run = new EvalRun("r1", "d1", 7, 9, "v1", EvalMetric.EXACT_MATCH, "COMPLETED", 1D,
                List.of(result), Instant.now(), Instant.now());
        TenantId tenant = new TenantId(7);
        when(service.createDataset(eq(tenant), eq(9L), eq("Demo"), eq("desc"))).thenReturn(dataset);
        when(service.addCase(eq("d1"), eq(tenant), eq(9L), eq("input"), eq("expected"), anyMap())).thenReturn(testCase);
        when(service.cases(eq("d1"), eq(tenant), eq(9L))).thenReturn(List.of(testCase));
        when(service.run(eq("d1"), eq(tenant), eq(9L), eq("v1"), eq(EvalMetric.EXACT_MATCH), any())).thenReturn(run);
        when(service.requireRun(eq("r1"), eq(tenant), eq(9L))).thenReturn(run);
        when(service.results(eq("r1"), eq(tenant), eq(9L))).thenReturn(List.of(result));
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            EvaluationController.DatasetRequest datasetRequest = new EvaluationController.DatasetRequest();
            datasetRequest.setName("Demo"); datasetRequest.setDescription("desc");
            assertTrue(controller.create(datasetRequest).isSuccess());
            EvaluationController.CaseRequest caseRequest = new EvaluationController.CaseRequest();
            caseRequest.setInput("input"); caseRequest.setExpected("expected"); caseRequest.setMetadata(Map.of());
            assertTrue(controller.addCase("d1", caseRequest).isSuccess());
            assertTrue(controller.cases("d1").isSuccess());
            EvaluationController.RunRequest runRequest = new EvaluationController.RunRequest();
            runRequest.setDatasetId("d1"); runRequest.setAppVersionId("v1"); runRequest.setMetric(EvalMetric.EXACT_MATCH);
            assertTrue(controller.run(runRequest).isSuccess());
            assertTrue(controller.detail("r1").isSuccess());
            assertTrue(controller.results("r1").isSuccess());
        }
    }
}
