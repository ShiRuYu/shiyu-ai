package com.shiyu.ai.web.conversation;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/generations")
public class GenerationController {
    private final GenerationRepository generations;
    private final GenerationAdmission admission;
    public GenerationController(GenerationRepository generations, GenerationAdmission admission) { this.generations = generations; this.admission = admission; }
    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<GenerationEvent>> stream(@PathVariable String id,
                                                          @RequestParam(defaultValue = "-1") int afterSeq,
                                                          @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        generations.find(id, tenant(), user()).orElseThrow(() -> new IllegalArgumentException("generation not found"));
        int cursor = afterSeq;
        if (lastEventId != null && !lastEventId.isBlank()) {
            try { cursor = Math.max(cursor, Integer.parseInt(lastEventId)); } catch (NumberFormatException ignored) { }
        }
        return Flux.fromIterable(generations.listEvents(id, cursor, 1000)).map(e -> ServerSentEvent.<GenerationEvent>builder().id(String.valueOf(e.sequence())).event(e.type().name()).data(e).build());
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
        generations.appendEvent(new GenerationEvent(g.id(), generations.nextEventSequence(g.id()), GenerationEventType.CANCELLED, "{}", java.time.Instant.now()), tenant());
        admission.release(tenant(), cancelled);
        return Result.success();
    }
    private long tenant(){Long id= UserContextHolder.getCurrentTenantId();if(id==null)throw new IllegalStateException("tenant context is required");return id;}
    private long user(){Long id=UserContextHolder.getUserId();if(id==null)throw new IllegalStateException("login is required");return id;}
}
