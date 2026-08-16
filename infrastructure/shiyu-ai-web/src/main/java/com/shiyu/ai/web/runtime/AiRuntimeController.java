package com.shiyu.ai.web.runtime;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.runtime.*;
import com.shiyu.ai.agent.runtime.AgentRuntime;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class AiRuntimeController {
    private final AiRuntimeService runtime;
    private final AgentRuntime agents;
    public AiRuntimeController(AiRuntimeService runtime, AgentRuntime agents) { this.runtime = runtime; this.agents = agents; }

    @PostMapping("/apps")
    public Result<AiApp> createApp(@Valid @RequestBody AppRequest request) { return Result.success(runtime.createApp(tenant(), user(), request.name, request.description)); }
    @GetMapping("/apps")
    public Result<List<AiApp>> apps(@RequestParam(defaultValue = "50") int limit) { return Result.success(runtime.listApps(tenant(), user(), limit)); }
    @PostMapping("/apps/{id}/versions")
    public Result<AiAppVersion> version(@PathVariable String id, @Valid @RequestBody VersionRequest request) { return Result.success(runtime.createVersion(id, tenant(), user(), request.version, request.configJson)); }
    @GetMapping("/apps/{id}/versions")
    public Result<List<AiAppVersion>> versions(@PathVariable String id) { return Result.success(runtime.versions(id, tenant(), user())); }
    @PostMapping("/apps/{id}/versions/{versionId}/publish")
    public Result<AiAppVersion> publish(@PathVariable String id, @PathVariable String versionId) { return Result.success(runtime.publish(id, versionId, tenant(), user())); }
    @PostMapping("/apps/{id}/versions/{versionId}/archive")
    public Result<AiAppVersion> archive(@PathVariable String id, @PathVariable String versionId) { return Result.success(runtime.archive(id, versionId, tenant(), user())); }
    @PostMapping("/apps/{id}/preview")
    public Result<AiAppPreview> preview(@PathVariable String id, @Valid @RequestBody PreviewRequest request) { return Result.success(runtime.preview(id, request.appVersionId, tenant(), user(), request.prompt)); }

    @PostMapping("/runs")
    public Result<AiRun> startRun(@Valid @RequestBody RunRequest request) {
        AiRunContext context = new AiRunContext(tenant(), user(), request.appId, request.appVersionId, request.conversationId, request.generationId, request.executionId, request.traceId, request.attributes);
        return Result.success(runtime.startRun(context, request.sourceType, request.sourceId, request.model, request.prompt));
    }
    @PostMapping("/apps/{id}/execute")
    public Result<Map<String, Object>> executeApp(@PathVariable String id, @Valid @RequestBody AppExecutionRequest request) {
        AiAppVersion version = runtime.requirePublishedVersion(id, tenant(), user());
        Map<String, Object> config = version.configJson() == null || version.configJson().isBlank() ? Map.of() : JSONUtils.parseObject(version.configJson(), Map.class);
        String agentId = config.get("agentId") == null ? null : String.valueOf(config.get("agentId"));
        if (agentId == null || agentId.isBlank()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "published app does not declare agentId");
        Map<String, Object> input = new java.util.LinkedHashMap<>();
        if (request.input != null) input.putAll(request.input);
        input.put("prompt", request.prompt == null ? "" : request.prompt);
        input.put("tenantId", tenant()); input.put("userId", user());
        input.put("__appId", id); input.put("__appVersionId", version.id());
        Execution execution = agents.execute(agentId, config.get("agentVersion") == null ? null : String.valueOf(config.get("agentVersion")), input);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("executionId", execution.getExecutionId()); result.put("status", execution.getStatus().name()); result.put("output", execution.getOutput());
        try { result.put("runtimeRunId", runtime.requireExecutionRun(execution.getExecutionId(), tenant(), user()).id()); } catch (RuntimeException ignored) { }
        return Result.success(result);
    }
    @GetMapping("/runs/{id}")
    public Result<AiRun> run(@PathVariable String id) { return Result.success(runtime.requireRun(id, tenant(), user())); }
    @GetMapping(value = "/generations/{generationId}/runtime-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public List<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> generationEvents(@PathVariable String generationId,
                                                                                              @RequestParam(defaultValue = "0") long afterSeq,
                                                                                              @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        long cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank()) try { cursor = Math.max(cursor, Long.parseLong(lastEventId)); } catch (NumberFormatException ignored) { }
        AiRun run = runtime.requireGenerationRun(generationId, tenant(), user());
        return runtime.events(run.id(), tenant(), user(), cursor, 1000).stream().map(e -> org.springframework.http.codec.ServerSentEvent.<AiRunEvent>builder(e).id(Long.toString(e.seq())).event(e.type().name()).build()).toList();
    }
    @GetMapping(value = "/runs/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public List<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> events(@PathVariable String id, @RequestParam(defaultValue = "0") long afterSeq, @RequestParam(defaultValue = "500") int limit, @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        long cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank()) try { cursor = Math.max(cursor, Long.parseLong(lastEventId)); } catch (NumberFormatException ignored) { }
        return runtime.events(id, tenant(), user(), cursor, limit).stream().map(e -> org.springframework.http.codec.ServerSentEvent.<AiRunEvent>builder(e).id(Long.toString(e.seq())).event(e.type().name()).build()).toList();
    }
    @GetMapping(value = "/runs/{id}/event-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<List<AiRunEvent>> eventHistory(@PathVariable String id, @RequestParam(defaultValue = "0") long afterSeq, @RequestParam(defaultValue = "500") int limit) {
        return Result.success(runtime.events(id, tenant(), user(), afterSeq, limit));
    }
    @PostMapping("/runs/{id}/cancel")
    public Result<AiRun> cancel(@PathVariable String id) { return Result.success(runtime.finish(id, tenant(), user(), AiRunStatus.CANCELLED, "CLIENT_CANCELLED")); }

    private long tenant() { Long id = UserContextHolder.getCurrentTenantId(); if (id == null) throw new IllegalStateException("tenant context is required"); return id; }
    private long user() { Long id = UserContextHolder.getUserId(); if (id == null) throw new IllegalStateException("login is required"); return id; }

    @Data public static class AppRequest { private String name; private String description; }
    @Data public static class VersionRequest { private String version; private String configJson = "{}"; }
    @Data public static class PreviewRequest { private String appVersionId; private String prompt; }
    @Data public static class RunRequest { private String appId; private String appVersionId; private AiRunSource sourceType = AiRunSource.API; private String sourceId; private String conversationId; private String generationId; private String executionId; private String traceId; private String model; private String prompt; private Map<String,String> attributes; }
    @Data public static class AppExecutionRequest { private String prompt; private Map<String,Object> input; }
}
