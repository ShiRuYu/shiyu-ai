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
 * 负责 In 记忆 AI 运行 的持久化查询、保存和删除，并维护数据访问边界。
 */
public class InMemoryAiRunRepository implements AiRunRepository {
    private final Map<String, AiRun> runs = new ConcurrentHashMap<>();
    private final Map<String, List<AiRunEvent>> events = new ConcurrentHashMap<>();
    private static final Comparator<AiRun> NEWEST_FIRST =
            Comparator.comparing(AiRun::createdAt).reversed().thenComparing(AiRun::id);

    /**
     * 创建或保存 In 记忆 AI 运行 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     */
    @Override
    public void insert(AiRun run) {
        if (runs.putIfAbsent(run.id(), run) != null)
            throw new IllegalStateException("run already exists");
    }

    /**
     * 查询 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param generationId 用于定位generation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 执行 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param generationId 用于定位generation的标识。
     * @return 返回 In 记忆 AI 运行 相关操作生成的结果数据。
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
     * 查询 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param executionId 用于定位execution的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 更新或设置 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @param expectedVersion 用于完成本次业务处理的 expectedVersion 参数。
     * @return 返回 In 记忆 AI 运行 相关操作生成的结果数据。
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
     * 更新或设置 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @param expectedVersion 用于完成本次业务处理的 expectedVersion 参数。
     * @param eventType 用于完成本次业务处理的 eventType 参数。
     * @param payload 本次流程携带的事件或业务数据。
     * @param redacted 用于完成本次业务处理的 redacted 参数。
     * @return 返回 In 记忆 AI 运行 相关操作生成的结果数据。
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
     * 执行 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param payload 本次流程携带的事件或业务数据。
     * @param redacted 用于完成本次业务处理的 redacted 参数。
     * @param createdAt 用于完成本次业务处理的 createdAt 参数。
     * @return 返回 In 记忆 AI 运行 相关操作生成的结果数据。
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
     * 执行 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param payload 本次流程携带的事件或业务数据。
     * @param redacted 用于完成本次业务处理的 redacted 参数。
     * @param createdAt 用于完成本次业务处理的 createdAt 参数。
     * @param turnId 用于定位turn的标识。
     * @param stepId 用于定位step的标识。
     * @param providerRequestId 用于定位provider的标识。
     * @return 返回 In 记忆 AI 运行 相关操作生成的结果数据。
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
     * 执行 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param event 本次流程携带的事件或业务数据。
     * @return 返回 In 记忆 AI 运行 相关操作生成的结果数据。
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
     * 执行 In 记忆 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param afterSeq 用于完成本次业务处理的 afterSeq 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
