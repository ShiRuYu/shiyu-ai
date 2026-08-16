package com.shiyu.ai.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAiRunRepository implements AiRunRepository {
    private final Map<String, AiRun> runs = new ConcurrentHashMap<>();
    private final Map<String, List<AiRunEvent>> events = new ConcurrentHashMap<>();

    @Override public void insert(AiRun run) { if (runs.putIfAbsent(run.id(), run) != null) throw new IllegalStateException("run already exists"); }
    @Override public Optional<AiRun> find(String id, long tenantId, long ownerUserId) { return Optional.ofNullable(runs.get(id)).filter(r -> r.tenantId() == tenantId && r.ownerUserId() == ownerUserId); }
    @Override public Optional<AiRun> findByGeneration(String generationId, long tenantId, long ownerUserId) { return runs.values().stream().filter(r -> r.tenantId() == tenantId && r.ownerUserId() == ownerUserId && java.util.Objects.equals(r.generationId(), generationId)).findFirst(); }
    @Override public Optional<AiRun> findByExecution(String executionId, long tenantId, long ownerUserId) { return runs.values().stream().filter(r -> r.tenantId() == tenantId && r.ownerUserId() == ownerUserId && java.util.Objects.equals(r.executionId(), executionId)).findFirst(); }
    @Override public int update(AiRun run, long expectedVersion) { return runs.computeIfPresent(run.id(), (id, current) -> current.version() == expectedVersion ? run : current) == run ? 1 : 0; }
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
    @Override public List<AiRunEvent> events(String runId, long tenantId, long ownerUserId, long afterSeq, int limit) {
        find(runId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found"));
        return events.getOrDefault(runId, List.of()).stream().filter(e -> e.seq() > afterSeq).sorted(Comparator.comparingLong(AiRunEvent::seq)).limit(Math.max(1, Math.min(limit, 100_000))).toList();
    }
}
