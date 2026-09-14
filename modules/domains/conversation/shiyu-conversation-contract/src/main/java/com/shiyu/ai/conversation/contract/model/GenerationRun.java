package com.shiyu.ai.conversation.contract.model;

import java.time.Instant;

/**
 * 表示一次会话生成运行及其生命周期状态。
 * @param id 标识，表示该记录组件承载的数据。
 * @param conversationId conversationId 属性，表示该记录组件承载的数据。
 * @param inputMessageId inputMessageId 属性，表示该记录组件承载的数据。
 * @param assistantMessageId assistantMessageId 属性，表示该记录组件承载的数据。
 * @param speakerId speakerId 属性，表示该记录组件承载的数据。
 * @param platform platform 属性，表示该记录组件承载的数据。
 * @param model 模型，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param promptTokens promptTokens 属性，表示该记录组件承载的数据。
 * @param completionTokens completionTokens 属性，表示该记录组件承载的数据。
 * @param latencyMs latencyMs 属性，表示该记录组件承载的数据。
 * @param errorCode errorCode 属性，表示该记录组件承载的数据。
 * @param lastEventSequence lastEventSequence 属性，表示该记录组件承载的数据。
 * @param cancelRequested cancelRequested 属性，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
 * @param runtimeRunId runtimeRunId 属性，表示该记录组件承载的数据。
 */
public record GenerationRun(
        String id,
        String conversationId,
        String inputMessageId,
        String assistantMessageId,
        String speakerId,
        String platform,
        String model,
        GenerationStatus status,
        long promptTokens,
        long completionTokens,
        long latencyMs,
        String errorCode,
        int lastEventSequence,
        boolean cancelRequested,
        long version,
        Instant createdAt,
        Instant updatedAt,
        String runtimeRunId) {
    /**
     * 处理生成运行。
     *
     * @param id 目标对象标识。
     * @param conversationId conversationId 参数。
     * @param inputMessageId inputMessageId 参数。
     * @param assistantMessageId assistantMessageId 参数。
     * @param speakerId speakerId 参数。
     * @param platform platform 参数。
     * @param model model 参数。
     * @param status 状态。
     * @param promptTokens promptTokens 参数。
     * @param completionTokens completionTokens 参数。
     * @param latencyMs latencyMs 参数。
     * @param errorCode errorCode 参数。
     * @param lastEventSequence lastEventSequence 参数。
     * @param cancelRequested cancelRequested 参数。
     * @param version version 参数。
     * @param createdAt createdAt 参数。
     * @param updatedAt updatedAt 参数。
     *
     * @return 处理结果。
     */
    public GenerationRun(
            String id,
            String conversationId,
            String inputMessageId,
            String assistantMessageId,
            String speakerId,
            String platform,
            String model,
            GenerationStatus status,
            long promptTokens,
            long completionTokens,
            long latencyMs,
            String errorCode,
            int lastEventSequence,
            boolean cancelRequested,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        this(
                id,
                conversationId,
                inputMessageId,
                assistantMessageId,
                speakerId,
                platform,
                model,
                status,
                promptTokens,
                completionTokens,
                latencyMs,
                errorCode,
                lastEventSequence,
                cancelRequested,
                version,
                createdAt,
                updatedAt,
                null);
    }

    /**
     * 处理生成运行。
     *
     * @param id 目标对象标识。
     * @param conversationId conversationId 参数。
     * @param inputMessageId inputMessageId 参数。
     * @param assistantMessageId assistantMessageId 参数。
     * @param platform platform 参数。
     * @param model model 参数。
     * @param status 状态。
     * @param promptTokens promptTokens 参数。
     * @param completionTokens completionTokens 参数。
     * @param latencyMs latencyMs 参数。
     * @param errorCode errorCode 参数。
     * @param lastEventSequence lastEventSequence 参数。
     * @param cancelRequested cancelRequested 参数。
     * @param version version 参数。
     * @param createdAt createdAt 参数。
     * @param updatedAt updatedAt 参数。
     *
     * @return 处理结果。
     */
    public GenerationRun(
            String id,
            String conversationId,
            String inputMessageId,
            String assistantMessageId,
            String platform,
            String model,
            GenerationStatus status,
            long promptTokens,
            long completionTokens,
            long latencyMs,
            String errorCode,
            int lastEventSequence,
            boolean cancelRequested,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        this(
                id,
                conversationId,
                inputMessageId,
                assistantMessageId,
                null,
                platform,
                model,
                status,
                promptTokens,
                completionTokens,
                latencyMs,
                errorCode,
                lastEventSequence,
                cancelRequested,
                version,
                createdAt,
                updatedAt,
                null);
    }

    public GenerationRun {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("generation run id is required");
        if (conversationId == null || conversationId.isBlank())
            throw new IllegalArgumentException("conversation id is required");
        if (status == null) throw new IllegalArgumentException("generation status is required");
        if (lastEventSequence < -1)
            throw new IllegalArgumentException("last event sequence is invalid");
    }

    public GenerationRun transition(GenerationStatus next) {
        if (next == null) throw new IllegalArgumentException("next status is required");
        boolean valid =
                (status == GenerationStatus.CREATED && next == GenerationStatus.RUNNING)
                        || (status == GenerationStatus.RUNNING
                                && (next == GenerationStatus.COMPLETED
                                        || next == GenerationStatus.CANCELLED
                                        || next == GenerationStatus.FAILED));
        if (!valid)
            throw new IllegalStateException(
                    "invalid generation transition " + status + " -> " + next);
        return new GenerationRun(
                id,
                conversationId,
                inputMessageId,
                assistantMessageId,
                speakerId,
                platform,
                model,
                next,
                promptTokens,
                completionTokens,
                latencyMs,
                errorCode,
                lastEventSequence,
                cancelRequested,
                version + 1,
                createdAt,
                Instant.now(),
                runtimeRunId);
    }

    public GenerationRun withRuntimeRunId(String id) {
        return new GenerationRun(
                this.id,
                conversationId,
                inputMessageId,
                assistantMessageId,
                speakerId,
                platform,
                model,
                status,
                promptTokens,
                completionTokens,
                latencyMs,
                errorCode,
                lastEventSequence,
                cancelRequested,
                version,
                createdAt,
                updatedAt,
                id);
    }
}
