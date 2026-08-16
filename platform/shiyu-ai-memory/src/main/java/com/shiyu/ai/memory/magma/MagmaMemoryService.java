package com.shiyu.ai.memory.magma;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagmaMemoryService implements MemoryIngestionPort, MemoryQueryPort, MemoryGovernancePort {
    private static final Logger log = LoggerFactory.getLogger(MagmaMemoryService.class);
    private final MagmaMemoryRepository repository;
    private final MemorySemanticIndex semanticIndex;

    public MagmaMemoryService(MagmaMemoryRepository repository, MemorySemanticIndex semanticIndex) {
        this.repository = repository;
        this.semanticIndex = semanticIndex;
    }

    @Override
    public MemoryEvent ingest(IngestMemoryCommand command) {
        MemoryEventStatus status = command.confirmationPolicy() == ConfirmationPolicy.REQUIRED
                ? MemoryEventStatus.CANDIDATE : MemoryEventStatus.ACTIVE;
        Instant now = Instant.now();
        MemoryEvent event = new MemoryEvent(
                UUID.randomUUID().toString(), command.tenantId(), command.namespace(),
                command.subjectType(), command.subjectId(), command.eventType(), command.content(),
                command.occurredAt(), command.sourceType(), command.sourceId(), command.attributes(),
                command.confidence(), command.importance(), status, command.confirmationPolicy(), now, now);
        MemoryEvent previous = repository.findLatestEvent(
                command.tenantId(), command.namespace(), command.subjectType(), command.subjectId()).orElse(null);
        repository.insertEvent(event);
        if (previous != null && !previous.id().equals(event.id())) {
            repository.insertEdge(new MemoryEdge(
                    UUID.randomUUID().toString(), command.tenantId(), previous.id(), event.id(),
                    GraphType.TEMPORAL, "after", true, 1.0d, 1.0d, EdgeOrigin.RULE,
                    event.sourceId(), true, now));
        }
        try { semanticIndex.upsert(event); } catch (RuntimeException ex) { log.warn("memory semantic index unavailable; event remains durable: {}", ex.getMessage()); }
        try { repository.enqueueConsolidation(command.tenantId(), event.id()); } catch (RuntimeException ex) { log.warn("memory consolidation enqueue failed: {}", ex.getMessage()); }
        return event;
    }

    public void confirm(long tenantId, String eventId) {
        requireEvent(tenantId, eventId);
        repository.updateEventStatus(tenantId, eventId, MemoryEventStatus.ACTIVE);
    }

    public void revoke(long tenantId, String eventId) {
        requireEvent(tenantId, eventId);
        repository.updateEventStatus(tenantId, eventId, MemoryEventStatus.REVOKED);
        repository.deactivateEdgesForNode(tenantId, eventId);
        try { semanticIndex.delete(eventId); } catch (RuntimeException ex) { log.warn("memory semantic delete failed: {}", ex.getMessage()); }
    }

    public List<MemoryEdge> relations(long tenantId, String eventId, GraphType graphType, int limit) {
        requireEvent(tenantId, eventId);
        return repository.findEdges(tenantId, eventId, graphType, Math.min(Math.max(limit, 1), 200));
    }
    public MemoryRetrievalTrace trace(long tenantId, String traceId) { return repository.findRetrievalTrace(tenantId, traceId).orElseThrow(() -> new IllegalArgumentException("retrieval trace not found")); }

    @Override
    public List<MemoryPath> retrieve(MemoryQuery query) {
        return retrieveWithTrace(query).paths();
    }

    public MemoryRetrievalResult retrieveWithTrace(MemoryQuery query) {
        List<MemoryPath> semantic = new ArrayList<>();
        try { semantic.addAll(semanticIndex.search(query, query.maxNodes())); } catch (RuntimeException ex) { log.warn("semantic recall failed; using durable anchors: {}", ex.getMessage()); }
        Map<String, Double> rrf = new HashMap<>();
        Map<String, MemoryEvent> events = new HashMap<>();
        int rank = 1;
        for (MemoryPath path : semantic) {
            if (!matches(query, path.event())) continue;
            rrf.merge(path.event().id(), 1d / (60 + rank++), Double::sum); events.put(path.event().id(), path.event());
        }
        String[] terms = query.text().toLowerCase().split("\\s+");
        for (MemoryEvent event : repository.findByNamespace(query.tenantId(), query.namespace(), 1000)) {
            if (!matches(query, event)) continue;
            long hits = java.util.Arrays.stream(terms).filter(t -> event.content().toLowerCase().contains(t)).count();
            if (hits > 0) { rrf.merge(event.id(), (double) hits / (60 + rank++), Double::sum); events.put(event.id(), event); }
        }
        List<MemoryPath> anchors = events.entrySet().stream().sorted(Comparator.<Map.Entry<String, MemoryEvent>>comparingDouble(e -> rrf.getOrDefault(e.getKey(), 0d)).reversed())
                .limit(query.maxNodes()).map(e -> new MemoryPath(e.getValue(), e.getValue() == null ? 0 : rrf.get(e.getKey()), List.of())).toList();
        List<MemoryPath> result = beam(anchors, query);
        String traceId = UUID.randomUUID().toString();
        repository.recordRetrievalTrace(new MemoryRetrievalTrace(traceId, query.tenantId(), query.namespace(), query.text(), result.stream().map(path -> path.event().id()).toList(), Instant.now()));
        return new MemoryRetrievalResult(result, traceId);
    }

    private boolean matches(MemoryQuery query, MemoryEvent event) {
        if (event.status() != MemoryEventStatus.ACTIVE || !query.namespace().equals(event.namespace())) return false;
        if (query.subjectType() != null && !query.subjectType().isBlank() && !query.subjectType().equals(event.subjectType())) return false;
        if (query.subjectId() != null && !query.subjectId().isBlank() && !query.subjectId().equals(event.subjectId())) return false;
        return (query.from() == null || !event.occurredAt().isBefore(query.from())) && (query.to() == null || !event.occurredAt().isAfter(query.to()));
    }

    private List<MemoryPath> beam(List<MemoryPath> anchors, MemoryQuery query) {
        Map<String, MemoryPath> best = new HashMap<>();
        List<MemoryPath> frontier = new ArrayList<>(anchors);
        for (MemoryPath path : frontier) best.put(path.event().id(), path);
        int tokenBudget = query.maxTokens();
        int[] usedTokens = {anchors.stream().mapToInt(path -> tokenCount(path.event().content())).sum()};
        for (int depth = 0; depth < query.maxDepth() && !frontier.isEmpty() && best.size() < query.maxNodes(); depth++) {
            List<MemoryPath> next = new ArrayList<>();
            for (MemoryPath path : frontier) for (GraphType graph : query.graphTypes()) for (MemoryEdge edge : repository.findEdges(query.tenantId(), path.event().id(), graph, 20)) {
                String nodeId = edge.sourceNodeId().equals(path.event().id()) ? edge.targetNodeId() : edge.sourceNodeId();
                repository.findEvent(query.tenantId(), nodeId).filter(e -> matches(query, e)).ifPresent(event -> {
                    if (!best.containsKey(event.id())) {
                        int candidateTokens = tokenCount(event.content());
                        if (usedTokens[0] + candidateTokens <= tokenBudget || best.isEmpty()) {
                            double graphWeight = graphWeight(query.intent(), graph);
                            MemoryPath candidate = new MemoryPath(event, path.score() * graphWeight * Math.max(0.1, edge.weight()) * 0.7, concat(path.edges(), edge));
                            best.put(event.id(), candidate); next.add(candidate); usedTokens[0] += candidateTokens;
                        }
                    }
                });
            }
            next.sort(Comparator.comparingDouble(MemoryPath::score).reversed());
            frontier = next.stream().limit(Math.max(1, Math.min(20, query.maxNodes()))).toList();
        }
        return ranked(best, query.maxNodes());
    }

    private List<MemoryPath> ranked(Map<String, MemoryPath> paths, int maxNodes) {
        return paths.values().stream().sorted(Comparator.comparingDouble(MemoryPath::score).reversed()).limit(maxNodes).toList();
    }

    private double graphWeight(MemoryQueryIntent intent, GraphType graph) {
        if (intent == MemoryQueryIntent.HYBRID) return 1.0;
        if ((intent == MemoryQueryIntent.SEMANTIC && graph == GraphType.SEMANTIC)
                || (intent == MemoryQueryIntent.TEMPORAL && graph == GraphType.TEMPORAL)
                || (intent == MemoryQueryIntent.CAUSAL && graph == GraphType.CAUSAL)
                || (intent == MemoryQueryIntent.ENTITY && graph == GraphType.ENTITY)) return 1.35;
        return 0.85;
    }

    private int tokenCount(String text) { return Math.max(1, (text == null ? 0 : text.length()) / 4); }

    private List<MemoryEdge> concat(List<MemoryEdge> current, MemoryEdge next) { List<MemoryEdge> result = new ArrayList<>(current); result.add(next); return result; }

    @Override
    public MemoryEvent supersede(long tenantId, String oldEventId, IngestMemoryCommand replacement) {
        requireEvent(tenantId, oldEventId);
        repository.updateEventStatus(tenantId, oldEventId, MemoryEventStatus.SUPERSEDED);
        repository.deactivateEdgesForNode(tenantId, oldEventId);
        MemoryEvent next = ingest(replacement);
        try { semanticIndex.delete(oldEventId); } catch (RuntimeException ignored) { /* index is rebuildable from H2 */ }
        return next;
    }

    private void requireEvent(long tenantId, String eventId) {
        if (repository.findEvent(tenantId, eventId).isEmpty()) {
            throw new IllegalArgumentException("memory event not found");
        }
    }
}
