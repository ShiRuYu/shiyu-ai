package com.shiyu.ai.agent.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.runtime.*;
import com.shiyu.ai.agent.runtime.AgentRuntime;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/agent")
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
        if (request.appVersionId != null && !request.appVersionId.isBlank()
                && !version.id().equals(request.appVersionId)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,
                    "only the currently published app version can execute");
        }
        Map<String, Object> config = version.configJson() == null || version.configJson().isBlank() ? Map.of() : JSONUtils.parseMap(version.configJson());
        String agentId = config.get("agentId") == null ? null : String.valueOf(config.get("agentId"));
        if (agentId == null || agentId.isBlank()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "published app does not declare agentId");
        Map<String, Object> input = new java.util.LinkedHashMap<>();
        if (request.input != null) input.putAll(request.input);
        input.put("prompt", request.prompt == null ? "" : request.prompt);
        input.put("__appId", id); input.put("__appVersionId", version.id());
        Execution execution = agents.execute(actor(), agentId,
                config.get("agentVersion") == null ? null : String.valueOf(config.get("agentVersion")), input);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("executionId", execution.getExecutionId()); result.put("status", execution.getStatus().name()); result.put("output", execution.getOutput());
        // Published App executions are admitted through Runtime before the
        // agent graph starts. Returning a successful execution without its
        // root run would violate the single-run audit contract.
        result.put("runtimeRunId", runtime.requireExecutionRun(execution.getExecutionId(), tenant(), user()).id());
        return Result.success(result);
    }
    @GetMapping("/runs/{id}")
    public Result<AiRun> run(@PathVariable String id) { return Result.success(runtime.requireRun(id, tenant(), user())); }
    @GetMapping("/runs")
    public Result<List<AiRun>> runs(@RequestParam(defaultValue = "50") int limit) { return Result.success(runtime.listRuns(tenant(), user(), limit)); }
    @GetMapping(value = "/generations/{generationId}/runtime-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> generationEvents(@PathVariable String generationId,
                                                                                              @RequestParam(defaultValue = "0") long afterSeq,
                                                                                              @RequestParam(defaultValue = "false") boolean follow,
                                                                                              @RequestParam(defaultValue = "30000") int waitMs,
                                                                                              @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        long cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank()) try { cursor = Math.max(cursor, Long.parseLong(lastEventId)); } catch (NumberFormatException ignored) { }
        AiRun run = runtime.requireGenerationRun(generationId, tenant(), user());
        return eventStream(run, cursor, follow, waitMs);
    }
    @GetMapping(value = "/runs/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> events(@PathVariable String id, @RequestParam(defaultValue = "0") long afterSeq, @RequestParam(defaultValue = "500") int limit, @RequestParam(defaultValue = "false") boolean follow, @RequestParam(defaultValue = "30000") int waitMs, @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        long cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank()) try { cursor = Math.max(cursor, Long.parseLong(lastEventId)); } catch (NumberFormatException ignored) { }
        AiRun run = runtime.requireRun(id, tenant(), user());
        return eventStream(run, cursor, follow, waitMs, limit);
    }
    @GetMapping(value = "/runs/{id}/event-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<List<AiRunEvent>> eventHistory(@PathVariable String id, @RequestParam(defaultValue = "0") long afterSeq, @RequestParam(defaultValue = "500") int limit) {
        return Result.success(runtime.events(id, tenant(), user(), afterSeq, limit));
    }
    @GetMapping("/runs/{id}/trajectory")
    public Result<List<AiRunEvent>> trajectory(@PathVariable String id) {
        return Result.success(runtime.events(id, tenant(), user(), 0, 5000));
    }
    @GetMapping("/runs/{id}/prompt-snapshot")
    public Result<Map<String, Object>> promptSnapshot(@PathVariable String id) {
        AiRun run = runtime.requireRun(id, tenant(), user());
        return Result.success(Map.of("runId", run.id(), "promptHash", run.promptHash() == null ? "" : run.promptHash(),
                "promptTokens", run.promptTokens(), "estimatedUsage", run.estimatedUsage(), "model", run.model() == null ? "" : run.model()));
    }
    @PostMapping("/runs/{id}/cancel")
    public Result<AiRun> cancel(@PathVariable String id) { return Result.success(runtime.finish(id, tenant(), user(), AiRunStatus.CANCELLED, "CLIENT_CANCELLED")); }

    private Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> eventStream(AiRun run, long afterSeq, boolean follow, int waitMs) {
        return eventStream(run, afterSeq, follow, waitMs, 1000);
    }

    private Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> eventStream(AiRun run, long afterSeq, boolean follow, int waitMs, int limit) {
        AtomicLong cursor = new AtomicLong(Math.max(0, afterSeq));
        Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> poll = Flux.interval(Duration.ZERO, Duration.ofMillis(500))
                .concatMap(tick -> Flux.defer(() -> {
                    List<AiRunEvent> events = runtime.events(run.id(), run.tenantId(), run.ownerUserId().value(), cursor.get(), limit);
                    if (events.isEmpty()) {
                        return Flux.just(org.springframework.http.codec.ServerSentEvent.<AiRunEvent>builder().comment("heartbeat").build());
                    }
                    return Flux.fromIterable(events).map(event -> {
                        cursor.accumulateAndGet(event.seq(), Math::max);
                        return org.springframework.http.codec.ServerSentEvent.<AiRunEvent>builder(event)
                                .id(Long.toString(event.seq())).event(event.type().name()).build();
                    });
                }));
        if (!follow) return Flux.fromIterable(runtime.events(run.id(), run.tenantId(), run.ownerUserId().value(), afterSeq, limit))
                .map(event -> org.springframework.http.codec.ServerSentEvent.<AiRunEvent>builder(event)
                        .id(Long.toString(event.seq())).event(event.type().name()).build());
        return poll.takeUntil(event -> event.data() != null && isTerminal(event.data().type()))
                .take(Duration.ofMillis(Math.max(1000, Math.min(waitMs, 120000))));
    }

    private boolean isTerminal(AiRunEventType type) {
        return type == AiRunEventType.RUN_COMPLETED || type == AiRunEventType.RUN_FAILED || type == AiRunEventType.RUN_CANCELLED;
    }

    private TenantId tenant() { return new TenantId(ActorContextHttpAdapter.tenantId()); }
    private long user() { return ActorContextHttpAdapter.userId(); }
    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }

    @Data public static class AppRequest { private String name; private String description; }
    @Data public static class VersionRequest { private String version; private String configJson = "{}"; }
    @Data public static class PreviewRequest { private String appVersionId; private String prompt; }
    @Data public static class RunRequest { private String appId; private String appVersionId; private AiRunSource sourceType = AiRunSource.API; private String sourceId; private String conversationId; private String generationId; private String executionId; private String traceId; private String model; private String prompt; private Map<String,String> attributes; }
    @Data public static class AppExecutionRequest { private String prompt; private String appVersionId; private Map<String,Object> input; }
}
