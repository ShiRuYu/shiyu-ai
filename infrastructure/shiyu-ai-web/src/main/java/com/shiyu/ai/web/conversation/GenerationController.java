package com.shiyu.ai.web.conversation;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import com.shiyu.ai.runtime.AiRun;
import com.shiyu.ai.runtime.AiRunEvent;
import com.shiyu.ai.runtime.AiRunEventType;
import com.shiyu.ai.runtime.AiRuntimeService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/generations")
public class GenerationController {
    private final GenerationRepository generations;
    private final GenerationAdmission admission;
    private final AiRuntimeService runtime;
    public GenerationController(GenerationRepository generations, GenerationAdmission admission) { this(generations, admission, null); }
    @Autowired public GenerationController(GenerationRepository generations, GenerationAdmission admission, AiRuntimeService runtime) { this.generations = generations; this.admission = admission; this.runtime = runtime; }
    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GenerationEvent>> stream(@PathVariable String id,
                                                          @RequestParam(defaultValue = "-1") int afterSeq,
                                                          @RequestParam(defaultValue = "false") boolean follow,
                                                          @RequestParam(defaultValue = "30000") int waitMs,
                                                          @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        generations.find(id, tenant(), user()).orElseThrow(() -> new IllegalArgumentException("generation not found"));
        int cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank()) {
            try { cursor = Math.max(cursor, Integer.parseInt(lastEventId)); } catch (NumberFormatException ignored) { }
        }
        if (runtime != null) {
            try {
                AiRun run = runtime.requireGenerationRun(id, tenant(), user());
                return runtimeEventStream(run, cursor, follow, waitMs);
            } catch (RuntimeException ignored) {
                // A legacy or stateless run may not have a runtime projection; use the durable generation view.
            }
        }
        return Flux.fromIterable(generations.listEvents(id, cursor, 1000)).map(e -> ServerSentEvent.<GenerationEvent>builder().id(String.valueOf(e.sequence())).event(e.type().name()).data(e).build());
    }

    private Flux<ServerSentEvent<GenerationEvent>> runtimeEventStream(AiRun run, long afterSeq, boolean follow, int waitMs) {
        if (!follow) {
            return Flux.fromIterable(runtime.events(run.id(), tenant(), user(), afterSeq, 1000))
                    .map(this::projectRuntimeEvent)
                    .map(this::sse);
        }
        AtomicLong cursor = new AtomicLong(Math.max(-1, afterSeq));
        return Flux.interval(Duration.ZERO, Duration.ofMillis(500))
                .concatMap(tick -> Flux.defer(() -> {
                    List<com.shiyu.ai.runtime.AiRunEvent> events = runtime.events(run.id(), tenant(), user(), cursor.get(), 1000);
                    if (events.isEmpty()) return Flux.just(ServerSentEvent.<GenerationEvent>builder().comment("heartbeat").build());
                    return Flux.fromIterable(events).map(event -> {
                        cursor.accumulateAndGet(event.seq(), Math::max);
                        return sse(projectRuntimeEvent(event));
                    });
                }))
                .takeUntil(event -> event.data() != null && isTerminal(event.data().type()))
                .take(Duration.ofMillis(Math.max(1000, Math.min(waitMs, 120000))));
    }

    private ServerSentEvent<GenerationEvent> sse(GenerationEvent event) {
        return ServerSentEvent.<GenerationEvent>builder().id(String.valueOf(event.sequence())).event(event.type().name()).data(event).build();
    }

    private boolean isTerminal(GenerationEventType type) {
        return type == GenerationEventType.COMPLETED || type == GenerationEventType.FAILED || type == GenerationEventType.CANCELLED;
    }
    @PostMapping("/{id}/cancel") public Result<Void> cancel(@PathVariable String id) {
        GenerationRun g = generations.find(id, tenant(), user()).orElseThrow(() -> new IllegalArgumentException("generation not found"));
        if (g.status() == GenerationStatus.COMPLETED || g.status() == GenerationStatus.CANCELLED || g.status() == GenerationStatus.FAILED) return Result.success();
        // The domain lifecycle is CREATED -> RUNNING -> terminal.  Do not
        // manufacture a CREATED -> CANCELLED transition during the tiny
        // admission/start race; the caller can retry once the run is RUNNING.
        if (g.status() != GenerationStatus.RUNNING) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "generation is not running");
        }
        GenerationRun cancelled = new GenerationRun(g.id(), g.conversationId(), g.inputMessageId(), g.assistantMessageId(), g.speakerId(), g.platform(), g.model(), GenerationStatus.CANCELLED, g.promptTokens(), g.completionTokens(), g.latencyMs(), g.errorCode(), g.lastEventSequence(), true, g.version() + 1, g.createdAt(), java.time.Instant.now());
        if (generations.update(cancelled, g.version()) != 1) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "generation was modified");
        // Runtime owns the durable event stream when installed; keep the
        // generation table as a projection only for runtime-less deployments.
        if (runtime == null) {
            generations.appendEvent(new GenerationEvent(g.id(), generations.nextEventSequence(g.id()), GenerationEventType.CANCELLED, "{}", java.time.Instant.now()), tenant());
        }
        if (runtime != null) {
            AiRun run = runtime.requireGenerationRun(id, tenant(), user());
            runtime.finish(run.id(), tenant(), user(), com.shiyu.ai.runtime.AiRunStatus.CANCELLED, "CLIENT_CANCELLED");
        }
        admission.release(tenant(), cancelled);
        return Result.success();
    }
    private long tenant(){Long id= UserContextHolder.getCurrentTenantId();if(id==null)throw new IllegalStateException("tenant context is required");return id;}
    private long user(){Long id=UserContextHolder.getUserId();if(id==null)throw new IllegalStateException("login is required");return id;}
    private GenerationEvent projectRuntimeEvent(AiRunEvent event) {
        GenerationEventType type = switch (event.type()) {
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
        // The projection is addressed by generation id.  Runtime events are
        // keyed by run id, but carry the generation id in their envelope;
        // returning the run id here breaks clients that resume a generation
        // stream and later compare event ownership.
        String generationId = event.generationId() == null || event.generationId().isBlank()
                ? event.runId() : event.generationId();
        return new GenerationEvent(generationId, (int) Math.min(Integer.MAX_VALUE, event.seq()), type, event.payload(), event.createdAt());
    }
}
