package com.shiyu.ai.agent.implementation.web;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppPreview;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@code AiRuntimeController} 是智能体模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/agent")
public class AiRuntimeController {
    /**
     * runtime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRuntimeService runtime;
    /**
     * agents 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentRuntime agents;

    /**
     * {@code AiRuntimeController} 创建并初始化当前类型实例。
     *
     * @param runtime 参数值，用于执行当前操作。
     * @param agents 参数值，用于执行当前操作。
     */
    public AiRuntimeController(AiRuntimeService runtime, AgentRuntime agents) {
        this.runtime = runtime;
        this.agents = agents;
    }

    /**
     * {@code createApp} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/apps")
    public Result<AiApp> createApp(@Valid @RequestBody AppRequest request) {
        return Result.success(
                runtime.createApp(tenant(), user(), request.name, request.description));
    }

    /**
     * {@code apps} 执行当前类型定义的业务操作。
     *
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/apps")
    public Result<List<AiApp>> apps(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(runtime.listApps(tenant(), user(), limit));
    }

    /**
     * {@code version} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/apps/{id}/versions")
    public Result<AiAppVersion> version(
            @PathVariable String id, @Valid @RequestBody VersionRequest request) {
        return Result.success(
                runtime.createVersion(id, tenant(), user(), request.version, request.configJson));
    }

    /**
     * {@code versions} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/apps/{id}/versions")
    public Result<List<AiAppVersion>> versions(@PathVariable String id) {
        return Result.success(runtime.versions(id, tenant(), user()));
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param id 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/apps/{id}/versions/{versionId}/publish")
    public Result<AiAppVersion> publish(@PathVariable String id, @PathVariable String versionId) {
        return Result.success(runtime.publish(id, versionId, tenant(), user()));
    }

    /**
     * {@code archive} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/apps/{id}/versions/{versionId}/archive")
    public Result<AiAppVersion> archive(@PathVariable String id, @PathVariable String versionId) {
        return Result.success(runtime.archive(id, versionId, tenant(), user()));
    }

    /**
     * {@code preview} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/apps/{id}/preview")
    public Result<AiAppPreview> preview(
            @PathVariable String id, @Valid @RequestBody PreviewRequest request) {
        return Result.success(
                runtime.preview(id, request.appVersionId, tenant(), user(), request.prompt));
    }

    /**
     * {@code startRun} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/runs")
    public Result<AiRun> startRun(@Valid @RequestBody RunRequest request) {
        AiRunContext context =
                new AiRunContext(
                        tenant(),
                        user(),
                        request.appId,
                        request.appVersionId,
                        request.conversationId,
                        request.generationId,
                        request.executionId,
                        request.traceId,
                        request.attributes);
        return Result.success(
                runtime.startRun(
                        context,
                        request.sourceType,
                        request.sourceId,
                        request.model,
                        request.prompt));
    }

    /**
     * {@code executeApp} 执行当前模块定义的业务流程。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/apps/{id}/execute")
    public Result<Map<String, Object>> executeApp(
            @PathVariable String id, @Valid @RequestBody AppExecutionRequest request) {
        AiAppVersion version = runtime.requirePublishedVersion(id, tenant(), user());
        if (request.appVersionId != null
                && !request.appVersionId.isBlank()
                && !version.id().equals(request.appVersionId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "only the currently published app version can execute");
        }
        Map<String, Object> config =
                version.configJson() == null || version.configJson().isBlank()
                        ? Map.of()
                        : JSONUtils.parseMap(version.configJson());
        String agentId =
                config.get("agentId") == null ? null : String.valueOf(config.get("agentId"));
        if (agentId == null || agentId.isBlank())
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "published app does not declare agentId");
        Map<String, Object> input = new java.util.LinkedHashMap<>();
        if (request.input != null) input.putAll(request.input);
        input.put("prompt", request.prompt == null ? "" : request.prompt);
        input.put("__appId", id);
        input.put("__appVersionId", version.id());
        Execution execution =
                agents.execute(
                        actor(),
                        agentId,
                        config.get("agentVersion") == null
                                ? null
                                : String.valueOf(config.get("agentVersion")),
                        input);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("executionId", execution.getExecutionId());
        result.put("status", execution.getStatus().name());
        result.put("output", execution.getOutput());
        result.put(
                "runtimeRunId",
                runtime.requireExecutionRun(execution.getExecutionId(), tenant(), user()).id());
        return Result.success(result);
    }

    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/runs/{id}")
    public Result<AiRun> run(@PathVariable String id) {
        return Result.success(runtime.requireRun(id, tenant(), user()));
    }

    /**
     * {@code runs} 执行当前模块定义的业务流程。
     *
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/runs")
    public Result<List<AiRun>> runs(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(runtime.listRuns(tenant(), user(), limit));
   }

   /**
    * {@code generationEvents} 执行当前类型定义的业务操作。
     *
     * @param generationId 参数值，用于执行当前操作。
     * @param afterSeq 参数值，用于执行当前操作。
     * @param follow 参数值，用于执行当前操作。
     * @param waitMs 参数值，用于执行当前操作。
     * @param lastEventId 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
    */
    @GetMapping(
            value = "/generations/{generationId}/runtime-events",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> generationEvents(
           @PathVariable String generationId,
            @RequestParam(defaultValue = "0") long afterSeq,
            @RequestParam(defaultValue = "false") boolean follow,
            @RequestParam(defaultValue = "30000") int waitMs,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        long cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank())
            try {
                cursor = Math.max(cursor, Long.parseLong(lastEventId));
            } catch (NumberFormatException ignored) {
            }
        AiRun run = runtime.requireGenerationRun(generationId, tenant(), user());
        return eventStream(run, cursor, follow, waitMs);
    }

    /**
     * {@code events} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param afterSeq 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     * @param follow 参数值，用于执行当前操作。
     * @param waitMs 参数值，用于执行当前操作。
     * @param lastEventId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping(value = "/runs/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> events(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") long afterSeq,
            @RequestParam(defaultValue = "500") int limit,
            @RequestParam(defaultValue = "false") boolean follow,
            @RequestParam(defaultValue = "30000") int waitMs,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        long cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank())
            try {
                cursor = Math.max(cursor, Long.parseLong(lastEventId));
            } catch (NumberFormatException ignored) {
            }
        AiRun run = runtime.requireRun(id, tenant(), user());
        return eventStream(run, cursor, follow, waitMs, limit);
    }

    /**
     * {@code eventHistory} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param afterSeq 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping(value = "/runs/{id}/event-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<List<AiRunEvent>> eventHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") long afterSeq,
            @RequestParam(defaultValue = "500") int limit) {
        return Result.success(runtime.events(id, tenant(), user(), afterSeq, limit));
    }

    /**
     * {@code trajectory} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/runs/{id}/trajectory")
    public Result<List<AiRunEvent>> trajectory(@PathVariable String id) {
        return Result.success(runtime.events(id, tenant(), user(), 0, 5000));
    }

    /**
     * {@code promptSnapshot} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/runs/{id}/prompt-snapshot")
    public Result<Map<String, Object>> promptSnapshot(@PathVariable String id) {
        AiRun run = runtime.requireRun(id, tenant(), user());
        return Result.success(
                Map.of(
                        "runId",
                        run.id(),
                        "promptHash",
                        run.promptHash() == null ? "" : run.promptHash(),
                        "promptTokens",
                        run.promptTokens(),
                        "estimatedUsage",
                        run.estimatedUsage(),
                        "model",
                        run.model() == null ? "" : run.model()));
    }

    /**
     * {@code cancel} 校验当前操作的输入或状态是否满足约束。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/runs/{id}/cancel")
    public Result<AiRun> cancel(@PathVariable String id) {
        return Result.success(
                runtime.finish(id, tenant(), user(), AiRunStatus.CANCELLED, "CLIENT_CANCELLED"));
    }

    private Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> eventStream(
            AiRun run, long afterSeq, boolean follow, int waitMs) {
        return eventStream(run, afterSeq, follow, waitMs, 1000);
    }

    private Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> eventStream(
            AiRun run, long afterSeq, boolean follow, int waitMs, int limit) {
        AtomicLong cursor = new AtomicLong(Math.max(0, afterSeq));
        Flux<org.springframework.http.codec.ServerSentEvent<AiRunEvent>> poll =
                Flux.interval(Duration.ZERO, Duration.ofMillis(500))
                        .concatMap(
                                tick ->
                                        Flux.defer(
                                                () -> {
                                                    List<AiRunEvent> events =
                                                            runtime.events(
                                                                    run.id(),
                                                                    run.tenantId(),
                                                                    run.ownerUserId().value(),
                                                                    cursor.get(),
                                                                    limit);
                                                    if (events.isEmpty()) {
                                                        return Flux.just(
                                                                org.springframework.http.codec
                                                                        .ServerSentEvent
                                                                        .<AiRunEvent>builder()
                                                                        .comment("heartbeat")
                                                                        .build());
                                                    }
                                                    return Flux.fromIterable(events)
                                                            .map(
                                                                    event -> {
                                                                        cursor.accumulateAndGet(
                                                                                event.seq(),
                                                                                Math::max);
                                                                        return org.springframework
                                                                                .http.codec
                                                                                .ServerSentEvent
                                                                                .<AiRunEvent>
                                                                                        builder(
                                                                                                event)
                                                                                .id(
                                                                                        Long
                                                                                                .toString(
                                                                                                        event
                                                                                                                .seq()))
                                                                                .event(
                                                                                        event.type()
                                                                                                .name())
                                                                                .build();
                                                                    });
                                                }));
        if (!follow)
            return Flux.fromIterable(
                            runtime.events(
                                    run.id(),
                                    run.tenantId(),
                                    run.ownerUserId().value(),
                                    afterSeq,
                                    limit))
                    .map(
                            event ->
                                    org.springframework.http.codec.ServerSentEvent
                                            .<AiRunEvent>builder(event)
                                            .id(Long.toString(event.seq()))
                                            .event(event.type().name())
                                            .build());
        return poll.takeUntil(event -> event.data() != null && isTerminal(event.data().type()))
                .take(Duration.ofMillis(Math.max(1000, Math.min(waitMs, 120000))));
    }

    private boolean isTerminal(AiRunEventType type) {
        return type == AiRunEventType.RUN_COMPLETED
                || type == AiRunEventType.RUN_FAILED
                || type == AiRunEventType.RUN_CANCELLED;
    }

    private TenantId tenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long user() {
        return ActorContextHttpAdapter.userId();
    }

    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }

    /**
     * {@code AppRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class AppRequest {
        private String name;
        /**
         * 描述，表示当前对象中的对应属性。
         */
        private String description;
    }

    /**
     * {@code VersionRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class VersionRequest {
        private String version;
        /**
         * configJson 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String configJson = "{}";
    }

    /**
     * {@code PreviewRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class PreviewRequest {
        private String appVersionId;
        /**
         * 提示词，表示当前对象中的对应属性。
         */
        private String prompt;
    }

    /**
     * {@code RunRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class RunRequest {
        private String appId;
        /**
         * appVersionId 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String appVersionId;
        /**
         * 来源类型，表示当前对象中的对应属性。
         */
        private AiRunSource sourceType = AiRunSource.API;
        /**
         * 来源标识，表示当前对象中的对应属性。
         */
        private String sourceId;
        /**
         * conversationId 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String conversationId;
        /**
         * 生成标识，表示当前对象中的对应属性。
         */
        private String generationId;
        /**
         * 执行标识，表示当前对象中的对应属性。
         */
        private String executionId;
        /**
         * traceId 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String traceId;
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model;
        /**
         * 提示词，表示当前对象中的对应属性。
         */
        private String prompt;
        /**
         * attributes 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Map<String, String> attributes;
    }

    /**
     * {@code AppExecutionRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class AppExecutionRequest {
        private String prompt;
        /**
         * appVersionId 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String appVersionId;
        /**
         * 输入，表示当前对象中的对应属性。
         */
        private Map<String, Object> input;
    }
}
