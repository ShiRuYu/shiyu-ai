package com.shiyu.ai.conversation.domain;

import java.time.Instant;

public record GenerationRun(String id, String conversationId, String inputMessageId,
                            String assistantMessageId, String speakerId, String platform, String model,
                            GenerationStatus status, long promptTokens, long completionTokens,
                            long latencyMs, String errorCode, int lastEventSequence,
                            boolean cancelRequested, long version, Instant createdAt, Instant updatedAt,
                            String runtimeRunId) {
    /** Source-compatible constructor for non-group generations. */
    public GenerationRun(String id, String conversationId, String inputMessageId,
                         String assistantMessageId, String speakerId, String platform, String model,
                         GenerationStatus status, long promptTokens, long completionTokens,
                         long latencyMs, String errorCode, int lastEventSequence,
                         boolean cancelRequested, long version, Instant createdAt, Instant updatedAt) {
        this(id, conversationId, inputMessageId, assistantMessageId, speakerId, platform, model, status,
                promptTokens, completionTokens, latencyMs, errorCode, lastEventSequence,
                cancelRequested, version, createdAt, updatedAt, null);
    }

    /** Source-compatible constructor for non-group generations. */
    public GenerationRun(String id, String conversationId, String inputMessageId,
                         String assistantMessageId, String platform, String model,
                         GenerationStatus status, long promptTokens, long completionTokens,
                         long latencyMs, String errorCode, int lastEventSequence,
                         boolean cancelRequested, long version, Instant createdAt, Instant updatedAt) {
        this(id, conversationId, inputMessageId, assistantMessageId, null, platform, model, status,
                promptTokens, completionTokens, latencyMs, errorCode, lastEventSequence,
                cancelRequested, version, createdAt, updatedAt, null);
    }

    public GenerationRun {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("generation run id is required");
        if (conversationId == null || conversationId.isBlank()) throw new IllegalArgumentException("conversation id is required");
        if (status == null) throw new IllegalArgumentException("generation status is required");
        if (lastEventSequence < -1) throw new IllegalArgumentException("last event sequence is invalid");
    }

    public GenerationRun transition(GenerationStatus next) {
        if (next == null) throw new IllegalArgumentException("next status is required");
        boolean valid = (status == GenerationStatus.CREATED && next == GenerationStatus.RUNNING)
                || (status == GenerationStatus.RUNNING && (next == GenerationStatus.COMPLETED
                || next == GenerationStatus.CANCELLED || next == GenerationStatus.FAILED));
        if (!valid) throw new IllegalStateException("invalid generation transition " + status + " -> " + next);
        return new GenerationRun(id, conversationId, inputMessageId, assistantMessageId, speakerId, platform, model,
                next, promptTokens, completionTokens, latencyMs, errorCode, lastEventSequence,
                cancelRequested, version + 1, createdAt, Instant.now(), runtimeRunId);
    }

    public GenerationRun withRuntimeRunId(String id) {
        return new GenerationRun(this.id, conversationId, inputMessageId, assistantMessageId, speakerId, platform, model,
                status, promptTokens, completionTokens, latencyMs, errorCode, lastEventSequence, cancelRequested,
                version, createdAt, updatedAt, id);
    }
}
