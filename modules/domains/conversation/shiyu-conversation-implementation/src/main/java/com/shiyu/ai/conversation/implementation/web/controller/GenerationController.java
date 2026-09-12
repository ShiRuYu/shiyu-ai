package com.shiyu.ai.conversation.implementation.web.controller;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEvent;
import com.shiyu.ai.agent.contract.runtime.AiRuntimePort;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.conversation.contract.api.GenerationAdmission;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.contract.model.GenerationStatus;
import com.shiyu.ai.conversation.implementation.domain.model.*;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@code GenerationController} 是会话模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/conversation/generations")
public class GenerationController {
    /**
     * generations 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final GenerationRepository generations;
    /**
     * admission 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final GenerationAdmission admission;
    /**
     * runtime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRuntimePort runtime;

    /**
     * {@code GenerationController} 创建并初始化当前类型实例。
     *
     * @param generations 参数值，用于执行当前操作。
     * @param admission 参数值，用于执行当前操作。
     */
    public GenerationController(GenerationRepository generations, GenerationAdmission admission) {
        this(generations, admission, null);
    }

    /**
     * {@code GenerationController} 创建并初始化当前类型实例。
     *
     * @param generations 参数值，用于执行当前操作。
     * @param admission 参数值，用于执行当前操作。
     * @param runtime 参数值，用于执行当前操作。
     */
    @Autowired
    public GenerationController(
            GenerationRepository generations,
            GenerationAdmission admission,
            AiRuntimePort runtime) {
        this.generations = generations;
        this.admission = admission;
        this.runtime = runtime;
    }

    /**
     * {@code stream} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param afterSeq 参数值，用于执行当前操作。
     * @param follow 参数值，用于执行当前操作。
     * @param waitMs 参数值，用于执行当前操作。
     * @param lastEventId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GenerationEvent>> stream(
            @PathVariable String id,
            @RequestParam(defaultValue = "-1") int afterSeq,
            @RequestParam(defaultValue = "false") boolean follow,
            @RequestParam(defaultValue = "30000") int waitMs,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        generations
                .find(id, tenant(), user())
                .orElseThrow(() -> new IllegalArgumentException("generation not found"));
        int cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank()) {
            try {
                cursor = Math.max(cursor, Integer.parseInt(lastEventId));
            } catch (NumberFormatException ignored) {
            }
        }
        if (runtime != null) {
            AiRun run = runtime.requireGenerationRun(id, tenant(), user());
            return runtimeEventStream(run, cursor, follow, waitMs);
        }
        return Flux.fromIterable(generations.listEvents(id, tenant(), cursor, 1000))
                .map(
                        e ->
                                ServerSentEvent.<GenerationEvent>builder()
                                        .id(String.valueOf(e.sequence()))
                                        .event(e.type().name())
                                        .data(e)
                                        .build());
    }

    private Flux<ServerSentEvent<GenerationEvent>> runtimeEventStream(
            AiRun run, long afterSeq, boolean follow, int waitMs) {
        TenantId tenantId = tenant();
        long ownerUserId = user();
        if (!follow) {
            return Flux.fromIterable(
                            runtime.events(run.id(), tenantId, ownerUserId, afterSeq, 1000))
                    .map(this::projectRuntimeEvent)
                    .map(this::sse);
        }
        AtomicLong cursor = new AtomicLong(Math.max(-1, afterSeq));
        return Flux.interval(Duration.ZERO, Duration.ofMillis(500))
                .concatMap(
                        tick ->
                                Flux.defer(
                                        () -> {
                                            List<com.shiyu.ai.agent.contract.runtime.AiRunEvent>
                                                    events =
                                                            runtime.events(
                                                                    run.id(),
                                                                    tenantId,
                                                                    ownerUserId,
                                                                    cursor.get(),
                                                                    1000);
                                            if (events.isEmpty())
                                                return Flux.just(
                                                        ServerSentEvent.<GenerationEvent>builder()
                                                                .comment("heartbeat")
                                                                .build());
                                            return Flux.fromIterable(events)
                                                    .map(
                                                            event -> {
                                                                cursor.accumulateAndGet(
                                                                        event.seq(), Math::max);
                                                                return sse(
                                                                        projectRuntimeEvent(event));
                                                            });
                                        }))
                .takeUntil(event -> event.data() != null && isTerminal(event.data().type()))
                .take(Duration.ofMillis(Math.max(1000, Math.min(waitMs, 120000))));
    }

    private ServerSentEvent<GenerationEvent> sse(GenerationEvent event) {
        return ServerSentEvent.<GenerationEvent>builder()
                .id(String.valueOf(event.sequence()))
                .event(event.type().name())
                .data(event)
                .build();
    }

    private boolean isTerminal(GenerationEventType type) {
        return type == GenerationEventType.COMPLETED
                || type == GenerationEventType.FAILED
                || type == GenerationEventType.CANCELLED;
    }

    /**
     * {@code cancel} 校验当前操作的输入或状态是否满足约束。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable String id) {
        GenerationRun g =
                generations
                        .find(id, tenant(), user())
                        .orElseThrow(() -> new IllegalArgumentException("generation not found"));
        if (g.status() == GenerationStatus.COMPLETED
                || g.status() == GenerationStatus.CANCELLED
                || g.status() == GenerationStatus.FAILED) return Result.success();
        if (g.status() != GenerationStatus.RUNNING) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "generation is not running");
        }
        GenerationRun cancelled =
                new GenerationRun(
                        g.id(),
                        g.conversationId(),
                        g.inputMessageId(),
                        g.assistantMessageId(),
                        g.speakerId(),
                        g.platform(),
                        g.model(),
                        GenerationStatus.CANCELLED,
                        g.promptTokens(),
                        g.completionTokens(),
                        g.latencyMs(),
                        g.errorCode(),
                        g.lastEventSequence(),
                        true,
                        g.version() + 1,
                        g.createdAt(),
                        java.time.Instant.now());
        if (generations.update(cancelled, g.version()) != 1)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "generation was modified");
        if (runtime == null) {
            generations.appendEvent(
                    new GenerationEvent(
                            g.id(),
                            generations.nextEventSequence(g.id(), tenant()),
                            GenerationEventType.CANCELLED,
                            "{}",
                            java.time.Instant.now()),
                    tenant());
        }
        if (runtime != null) {
            AiRun run = runtime.requireGenerationRun(id, tenant(), user());
            runtime.finish(
                    run.id(),
                    tenant(),
                    user(),
                    com.shiyu.ai.agent.contract.runtime.AiRunStatus.CANCELLED,
                    "CLIENT_CANCELLED");
        }
        admission.release(actor(), cancelled);
        return Result.success();
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

    private GenerationEvent projectRuntimeEvent(AiRunEvent event) {
        GenerationEventType type =
                switch (event.type()) {
                    case RUN_STARTED -> GenerationEventType.STARTED;
                    case MODEL_BLOCK_STARTED -> GenerationEventType.BLOCK_STARTED;
                    case MODEL_REASONING_DELTA -> GenerationEventType.REASONING_DELTA;
                    case MODEL_TOOL_CALL_DELTA -> GenerationEventType.TOOL_CALL;
                    case MODEL_BLOCK_COMPLETED -> GenerationEventType.BLOCK_COMPLETED;
                    case MODEL_COMPLETED -> GenerationEventType.BLOCK_COMPLETED;
                    case MODEL_USAGE -> GenerationEventType.USAGE;
                    case RUN_COMPLETED -> GenerationEventType.COMPLETED;
                    case RUN_CANCELLED -> GenerationEventType.CANCELLED;
                    case RUN_FAILED -> GenerationEventType.FAILED;
                    default -> GenerationEventType.DELTA;
                };
        String generationId =
                event.generationId() == null || event.generationId().isBlank()
                        ? event.runId()
                        : event.generationId();
        return new GenerationEvent(
                generationId,
                (int) Math.min(Integer.MAX_VALUE, event.seq()),
                type,
                event.payload(),
                event.createdAt());
    }
}
