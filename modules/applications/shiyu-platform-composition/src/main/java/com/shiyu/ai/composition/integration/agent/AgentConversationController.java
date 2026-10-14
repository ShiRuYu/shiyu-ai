package com.shiyu.ai.composition.integration.agent;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/** Starts an Agent app inside an owned conversation and streams persisted exchange IDs. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conversation/conversations/{conversationId}/agent-executions")
public class AgentConversationController {
    private final AgentConversationService service;

    /** Executes a published app for the signed-in conversation owner. */
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Map<String, Object>>> execute(
            @PathVariable String conversationId, @Valid @RequestBody Request request) {
        Flux<Map<String, Object>> execution = service.execute(ActorContextHttpAdapter.currentActor(), conversationId,
                request.appId(), request.appVersionId(), request.prompt(),
                request.sourceMessageId());
        Flux<Map<String, Object>> heartbeat = Flux.interval(Duration.ofSeconds(1))
                .map(ignored -> Map.of("status", "HEARTBEAT"));
        return Flux.merge(execution.subscribeOn(Schedulers.boundedElastic()), heartbeat)
                .takeUntil(event -> "COMPLETED".equals(event.get("status"))
                        || "FAILED".equals(event.get("status"))
                        || "PAUSED".equals(event.get("status"))
                        || "CANCELLED".equals(event.get("status")))
                .map(event -> ServerSentEvent.<Map<String, Object>>builder(event)
                        .event(String.valueOf(event.getOrDefault("status", "NODE")))
                        .build());
    }

    /** A stream that failed before its first event cannot use the JSON error converter. */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public void startFailure(RuntimeException error, HttpServletResponse response) {
        if (!response.isCommitted()) {
            response.setStatus(error instanceof IllegalArgumentException ? 422 : 409);
        }
    }

    /** Published app selection and user input for an Agent conversation turn. */
    public record Request(@NotBlank String appId, String appVersionId,
            @NotBlank String prompt, String sourceMessageId) {}
}
