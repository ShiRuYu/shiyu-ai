package com.shiyu.ai.web.evaluation;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.evaluation.*;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/evaluations")
public class EvaluationController {
    private final EvaluationService evaluations;
    public EvaluationController(EvaluationService evaluations) { this.evaluations = evaluations; }
    @PostMapping("/datasets") public Result<EvalDataset> create(@Valid @RequestBody DatasetRequest request) { return Result.success(evaluations.createDataset(tenant(), user(), request.name, request.description)); }
    @PostMapping("/datasets/{id}/cases") public Result<EvalCase> addCase(@PathVariable String id, @Valid @RequestBody CaseRequest request) { return Result.success(evaluations.addCase(id, tenant(), user(), request.input, request.expected, request.metadata)); }
    @GetMapping("/datasets/{id}/cases") public Result<List<EvalCase>> cases(@PathVariable String id) { return Result.success(evaluations.cases(id, tenant(), user())); }
    @PostMapping("/runs") public Result<EvalRun> run(@Valid @RequestBody RunRequest request) { return Result.success(evaluations.run(request.datasetId, tenant(), user(), request.appVersionId, request.metric, c -> request.outputs == null ? "" : request.outputs.getOrDefault(c.id(), ""))); }
    @GetMapping("/runs/{id}") public Result<EvalRun> detail(@PathVariable String id) { return Result.success(evaluations.requireRun(id, tenant(), user())); }
    @GetMapping("/runs/{id}/results") public Result<List<EvalResult>> results(@PathVariable String id) { return Result.success(evaluations.results(id, tenant(), user())); }
    private long tenant() { Long id=UserContextHolder.getCurrentTenantId(); if(id==null) throw new IllegalStateException("tenant context is required"); return id; }
    private long user() { Long id=UserContextHolder.getUserId(); if(id==null) throw new IllegalStateException("login is required"); return id; }
    @Data public static class DatasetRequest { private String name; private String description; }
    @Data public static class CaseRequest { private String input; private String expected; private Map<String,Object> metadata; }
    @Data public static class RunRequest { private String datasetId; private String appVersionId; private EvalMetric metric = EvalMetric.EXACT_MATCH; private Map<String,String> outputs; }
}
