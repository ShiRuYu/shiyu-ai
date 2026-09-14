package com.shiyu.ai.agent.implementation.runtime.adapter.inmemory;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code InMemoryAiRunRepository} 定义智能体模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
public class InMemoryAiRunRepository implements AiRunRepository {
    private final Map<String, AiRun> runs = new ConcurrentHashMap<>();
    private final Map<String, List<AiRunEvent>> events = new ConcurrentHashMap<>();
    private static final Comparator<AiRun> NEWEST_FIRST =
            Comparator.comparing(AiRun::createdAt).reversed().thenComparing(AiRun::id);

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param run 参数值，用于执行当前操作。
     */
    @Override
    public void insert(AiRun run) {
        if (runs.putIfAbsent(run.id(), run) != null)
            throw new IllegalStateException("run already exists");
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiRun> find(String id, TenantId tenantId, long ownerUserId) {
        return Optional.ofNullable(runs.get(id))
                .filter(
                        r ->
                                r.tenantId().equals(requireTenant(tenantId))
                                        && r.ownerUserId().value() == ownerUserId);
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiRun> list(TenantId tenantId, long ownerUserId, int limit) {
        return runs.values().stream()
                .filter(
                        r ->
                                r.tenantId().equals(requireTenant(tenantId))
                                        && r.ownerUserId().value() == ownerUserId)
                .sorted(NEWEST_FIRST)
                .limit(Math.max(1, Math.min(limit, 500)))
                .toList();
    }

    /**
     * {@code findByGeneration} 查询并返回当前操作所需的数据。
     *
     * @param generationId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiRun> findByGeneration(
            String generationId, TenantId tenantId, long ownerUserId) {
        return runs.values().stream()
                .filter(
                        r ->
                                r.tenantId().equals(requireTenant(tenantId))
                                        && r.ownerUserId().value() == ownerUserId
                                        && java.util.Objects.equals(r.generationId(), generationId))
                .sorted(NEWEST_FIRST)
                .findFirst();
    }

    /**
     * {@code linkGeneration} 执行当前类型定义的业务操作。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param generationId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int linkGeneration(
            String runId, TenantId tenantId, long ownerUserId, String generationId) {
        AiRun current =
                find(runId, tenantId, ownerUserId)
                        .orElseThrow(() -> new IllegalArgumentException("run not found"));
        if (current.generationId() != null) return 0;
        AiRun linked =
                new AiRun(
                        current.id(),
                        current.tenantId(),
                        current.ownerUserId(),
                        current.appId(),
                        current.appVersionId(),
                        current.sourceType(),
                        current.sourceId(),
                        current.parentRunId(),
                        current.traceId(),
                        current.conversationId(),
                        generationId,
                        current.executionId(),
                        current.model(),
                        current.promptHash(),
                        current.status(),
                        current.promptTokens(),
                        current.completionTokens(),
                        current.estimatedUsage(),
                        current.costSnapshot(),
                        current.createdAt(),
                        current.completedAt(),
                        current.errorCode(),
                        current.version() + 1,
                        current.lastEventSeq());
        return update(linked, current.version());
    }

    /**
     * {@code findByExecution} 查询并返回当前操作所需的数据。
     *
     * @param executionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<AiRun> findByExecution(
            String executionId, TenantId tenantId, long ownerUserId) {
        return runs.values().stream()
                .filter(
                        r ->
                                r.tenantId().equals(requireTenant(tenantId))
                                        && r.ownerUserId().value() == ownerUserId
                                        && java.util.Objects.equals(r.executionId(), executionId))
                .sorted(NEWEST_FIRST)
                .findFirst();
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param run 参数值，用于执行当前操作。
     * @param expectedVersion 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int update(AiRun run, long expectedVersion) {
        boolean[] replaced = {false};
        runs.computeIfPresent(
                run.id(),
                (id, current) -> {
                    if (current.version() != expectedVersion) return current;
                    replaced[0] = true;
                    return run;
                });
        return replaced[0] ? 1 : 0;
    }

    /**
     * {@code updateTerminalAndAppend} 写入或更新当前模块中的业务数据。
     *
     * @param run 参数值，用于执行当前操作。
     * @param expectedVersion 参数值，用于执行当前操作。
     * @param eventType 参数值，用于执行当前操作。
     * @param payload 参数值，用于执行当前操作。
     * @param redacted 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public synchronized AiRun updateTerminalAndAppend(
            AiRun run,
            long expectedVersion,
            AiRunEventType eventType,
            String payload,
            boolean redacted) {
        AiRun previous =
                find(run.id(), run.tenantId(), run.ownerUserId().value())
                        .orElseThrow(() -> new IllegalArgumentException("run not found"));
        List<AiRunEvent> stream = events.computeIfAbsent(run.id(), ignored -> new ArrayList<>());
        synchronized (stream) {
            List<AiRunEvent> previousEvents = new ArrayList<>(stream);
            if (update(run, expectedVersion) != 1)
                throw new IllegalStateException("run was modified");
            try {
                long seq =
                        appendNextEvent(
                                run.id(),
                                run.tenantId(),
                                run.ownerUserId().value(),
                                eventType,
                                payload,
                                redacted,
                                Instant.now());
                return run.withLastEventSeq(seq);
            } catch (RuntimeException failure) {
                runs.put(run.id(), previous);
                stream.clear();
                stream.addAll(previousEvents);
                if (previousEvents.isEmpty()) events.remove(run.id(), stream);
                throw failure;
            }
        }
    }

    /**
     * {@code appendNextEvent} 执行当前类型定义的业务操作。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     * @param payload 参数值，用于执行当前操作。
     * @param redacted 参数值，用于执行当前操作。
     * @param createdAt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long appendNextEvent(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            AiRunEventType type,
            String payload,
            boolean redacted,
            Instant createdAt) {
        return appendNextEvent(
                runId, tenantId, ownerUserId, type, payload, redacted, createdAt, null, null, null);
    }

    /**
     * {@code appendNextEvent} 执行当前类型定义的业务操作。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     * @param payload 参数值，用于执行当前操作。
     * @param redacted 参数值，用于执行当前操作。
     * @param createdAt 参数值，用于执行当前操作。
     * @param turnId 参数值，用于执行当前操作。
     * @param stepId 参数值，用于执行当前操作。
     * @param providerRequestId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long appendNextEvent(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            AiRunEventType type,
            String payload,
            boolean redacted,
            Instant createdAt,
            String turnId,
            String stepId,
            String providerRequestId) {
        find(runId, tenantId, ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("run not found"));
        String normalizedPayload = payload == null ? "{}" : payload;
        List<AiRunEvent> stream = events.computeIfAbsent(runId, ignored -> new ArrayList<>());
        synchronized (stream) {
            AiRunEvent terminal =
                    stream.stream()
                            .filter(
                                    event ->
                                            event.type() == AiRunEventType.RUN_COMPLETED
                                                    || event.type() == AiRunEventType.RUN_FAILED
                                                    || event.type() == AiRunEventType.RUN_CANCELLED)
                            .findFirst()
                            .orElse(null);
            if (terminal != null) {
                if (type == terminal.type()) {
                    if (!java.util.Objects.equals(terminal.payload(), normalizedPayload)
                            || terminal.redacted() != redacted) {
                        throw new IllegalStateException(
                                "run already has a different terminal event");
                    }
                    return terminal.seq();
                }
                throw new IllegalStateException("run is already terminal");
            }
            long seq = stream.size() + 1L;
            AiRun run = find(runId, tenantId, ownerUserId).orElseThrow();
            AiRunEvent event =
                    new AiRunEvent(
                            runId,
                            tenantId,
                            seq,
                            type,
                            1,
                            turnId,
                            stepId,
                            seq > 1 ? seq - 1 : null,
                            run.conversationId(),
                            run.generationId(),
                            run.executionId(),
                            run.appId(),
                            run.appVersionId(),
                            providerRequestId,
                            run.traceId(),
                            normalizedPayload,
                            redacted,
                            createdAt);
            stream.add(event);
            runs.computeIfPresent(
                    runId,
                    (id, current) ->
                            new AiRun(
                                    current.id(),
                                    current.tenantId(),
                                    current.ownerUserId(),
                                    current.appId(),
                                    current.appVersionId(),
                                    current.sourceType(),
                                    current.sourceId(),
                                    current.parentRunId(),
                                    current.traceId(),
                                    current.conversationId(),
                                    current.generationId(),
                                    current.executionId(),
                                    current.model(),
                                    current.promptHash(),
                                    current.status(),
                                    current.promptTokens(),
                                    current.completionTokens(),
                                    current.estimatedUsage(),
                                    current.costSnapshot(),
                                    current.createdAt(),
                                    current.completedAt(),
                                    current.errorCode(),
                                    current.version(),
                                    seq));
            return seq;
        }
    }

    /**
     * {@code appendEvent} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long appendEvent(AiRunEvent event) {
        AiRun run = runs.get(event.runId());
        if (run == null || !run.tenantId().equals(requireTenant(event.tenantId()))) {
            throw new IllegalArgumentException("run not found");
        }
        List<AiRunEvent> stream =
                events.computeIfAbsent(event.runId(), ignored -> new ArrayList<>());
        synchronized (stream) {
            AiRunEvent existing =
                    stream.stream().filter(e -> e.seq() == event.seq()).findFirst().orElse(null);
            if (existing != null) {
                if (existing.type() != event.type()
                        || !java.util.Objects.equals(existing.payload(), event.payload())
                        || existing.redacted() != event.redacted()) {
                    throw new IllegalStateException(
                            "event sequence already contains a different payload");
                }
                return event.seq();
            }
            if (stream.stream()
                    .anyMatch(
                            e ->
                                    e.type() == AiRunEventType.RUN_COMPLETED
                                            || e.type() == AiRunEventType.RUN_FAILED
                                            || e.type() == AiRunEventType.RUN_CANCELLED)) {
                throw new IllegalStateException("run is already terminal");
            }
            long expected = stream.size() + 1L;
            if (event.seq() != expected)
                throw new IllegalStateException("run event sequence must be contiguous");
            stream.add(event);
            runs.computeIfPresent(
                    event.runId(), (id, current) -> current.withLastEventSeq(event.seq()));
            return event.seq();
        }
    }

    /**
     * {@code events} 执行当前类型定义的业务操作。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param afterSeq 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AiRunEvent> events(
            String runId, TenantId tenantId, long ownerUserId, long afterSeq, int limit) {
        find(runId, tenantId, ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("run not found"));
        List<AiRunEvent> stream = events.get(runId);
        List<AiRunEvent> snapshot;
        if (stream == null) {
            snapshot = List.of();
        } else {
            synchronized (stream) {
                snapshot = new ArrayList<>(stream);
            }
        }
        return snapshot.stream()
                .filter(e -> e.seq() > afterSeq)
                .sorted(Comparator.comparingLong(AiRunEvent::seq))
                .limit(Math.max(1, Math.min(limit, 100_000)))
                .toList();
    }

    private static TenantId requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId;
    }
}
