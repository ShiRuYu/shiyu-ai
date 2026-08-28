package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAiRunRepository implements AiRunRepository {
    private final Map<String, AiRun> runs = new ConcurrentHashMap<>();
    private final Map<String, List<AiRunEvent>> events = new ConcurrentHashMap<>();

    @Override public void insert(AiRun run) { if (runs.putIfAbsent(run.id(), run) != null) throw new IllegalStateException("run already exists"); }
    @Override public Optional<AiRun> find(String id, TenantId tenantId, long ownerUserId) { return Optional.ofNullable(runs.get(id)).filter(r -> r.tenantId().equals(requireTenant(tenantId)) && r.ownerUserId().value() == ownerUserId); }
    @Override public List<AiRun> list(TenantId tenantId, long ownerUserId, int limit) { return runs.values().stream().filter(r -> r.tenantId().equals(requireTenant(tenantId)) && r.ownerUserId().value() == ownerUserId).sorted(java.util.Comparator.comparing(AiRun::createdAt).reversed()).limit(Math.max(1, Math.min(limit, 500))).toList(); }
    @Override public Optional<AiRun> findByGeneration(String generationId, TenantId tenantId, long ownerUserId) { return runs.values().stream().filter(r -> r.tenantId().equals(requireTenant(tenantId)) && r.ownerUserId().value() == ownerUserId && java.util.Objects.equals(r.generationId(), generationId)).findFirst(); }
    @Override public int linkGeneration(String runId, TenantId tenantId, long ownerUserId, String generationId) {
        AiRun current = find(runId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found"));
        if (current.generationId() != null) return 0;
        AiRun linked = new AiRun(current.id(), current.tenantId(), current.ownerUserId(), current.appId(), current.appVersionId(), current.sourceType(), current.sourceId(), current.parentRunId(), current.traceId(), current.conversationId(), generationId, current.executionId(), current.model(), current.promptHash(), current.status(), current.promptTokens(), current.completionTokens(), current.estimatedUsage(), current.costSnapshot(), current.createdAt(), current.completedAt(), current.errorCode(), current.version() + 1, current.lastEventSeq());
        return update(linked, current.version());
    }
    @Override public Optional<AiRun> findByExecution(String executionId, TenantId tenantId, long ownerUserId) { return runs.values().stream().filter(r -> r.tenantId().equals(requireTenant(tenantId)) && r.ownerUserId().value() == ownerUserId && java.util.Objects.equals(r.executionId(), executionId)).findFirst(); }
    @Override public int update(AiRun run, long expectedVersion) {
        // The value returned by computeIfPresent is the existing value when
        // the compare-and-set predicate fails. Comparing that value with the
        // requested run by identity would therefore report a false success
        // when callers retry with the same object and a stale version.
        boolean[] replaced = {false};
        runs.computeIfPresent(run.id(), (id, current) -> {
            if (current.version() != expectedVersion) return current;
            replaced[0] = true;
            return run;
        });
        return replaced[0] ? 1 : 0;
    }
    @Override public synchronized AiRun updateTerminalAndAppend(AiRun run, long expectedVersion, AiRunEventType eventType,
                                                                 String payload, boolean redacted) {
        if (update(run, expectedVersion) != 1) throw new IllegalStateException("run was modified");
        long seq = appendNextEvent(run.id(), run.tenantId(), run.ownerUserId().value(), eventType, payload, redacted, Instant.now());
        return run.withLastEventSeq(seq);
    }
    @Override public long appendNextEvent(String runId, TenantId tenantId, long ownerUserId, AiRunEventType type,
                                          String payload, boolean redacted, Instant createdAt) {
        return appendNextEvent(runId, tenantId, ownerUserId, type, payload, redacted, createdAt, null, null, null);
    }
    @Override public long appendNextEvent(String runId, TenantId tenantId, long ownerUserId, AiRunEventType type,
                                          String payload, boolean redacted, Instant createdAt,
                                          String turnId, String stepId, String providerRequestId) {
        find(runId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found"));
        List<AiRunEvent> stream = events.computeIfAbsent(runId, ignored -> new ArrayList<>());
        synchronized (stream) {
            AiRunEvent terminal = stream.stream().filter(event -> event.type() == AiRunEventType.RUN_COMPLETED
                        || event.type() == AiRunEventType.RUN_FAILED || event.type() == AiRunEventType.RUN_CANCELLED).findFirst().orElse(null);
            if (terminal != null) {
                if (type == terminal.type()) {
                    if (terminal.type() != type || !java.util.Objects.equals(terminal.payload(), payload) || terminal.redacted() != redacted) {
                        throw new IllegalStateException("run already has a different terminal event");
                    }
                    return terminal.seq();
                }
                throw new IllegalStateException("run is already terminal");
            }
            long seq = stream.size() + 1L;
            AiRun run = find(runId, tenantId, ownerUserId).orElseThrow();
            AiRunEvent event = new AiRunEvent(runId, tenantId, seq, type, 1, turnId, stepId,
                    seq > 1 ? seq - 1 : null, run.conversationId(), run.generationId(), run.executionId(),
                    run.appId(), run.appVersionId(), providerRequestId, run.traceId(), payload, redacted, createdAt);
            stream.add(event);
            runs.computeIfPresent(runId, (id, current) -> new AiRun(current.id(), current.tenantId(), current.ownerUserId(), current.appId(), current.appVersionId(), current.sourceType(), current.sourceId(), current.parentRunId(), current.traceId(), current.conversationId(), current.generationId(), current.executionId(), current.model(), current.promptHash(), current.status(), current.promptTokens(), current.completionTokens(), current.estimatedUsage(), current.costSnapshot(), current.createdAt(), current.completedAt(), current.errorCode(), current.version(), seq));
            return seq;
        }
    }
    @Override public long appendEvent(AiRunEvent event) {
        List<AiRunEvent> stream = events.computeIfAbsent(event.runId(), ignored -> new ArrayList<>());
        synchronized (stream) {
            AiRunEvent existing = stream.stream().filter(e -> e.seq() == event.seq()).findFirst().orElse(null);
            if (existing != null) {
                if (existing.type() != event.type() || !java.util.Objects.equals(existing.payload(), event.payload()) || existing.redacted() != event.redacted()) {
                    throw new IllegalStateException("event sequence already contains a different payload");
                }
                return event.seq();
            }
            long expected = stream.size() + 1L;
            if (event.seq() != expected) throw new IllegalStateException("run event sequence must be contiguous");
            stream.add(event);
            return event.seq();
        }
    }
    @Override public List<AiRunEvent> events(String runId, TenantId tenantId, long ownerUserId, long afterSeq, int limit) {
        find(runId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found"));
        return events.getOrDefault(runId, List.of()).stream().filter(e -> e.seq() > afterSeq).sorted(Comparator.comparingLong(AiRunEvent::seq)).limit(Math.max(1, Math.min(limit, 100_000))).toList();
    }

    private static TenantId requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId;
    }
}
