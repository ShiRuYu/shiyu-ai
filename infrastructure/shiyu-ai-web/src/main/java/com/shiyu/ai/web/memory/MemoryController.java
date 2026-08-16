package com.shiyu.ai.web.memory;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.memory.magma.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "MAGMA Memory Platform")
@RestController
@RequestMapping("/memory")
public class MemoryController {
    private final MagmaMemoryService memory;
    private final MemorySemanticIndex index;
    public MemoryController(MagmaMemoryService memory, MemorySemanticIndex index) { this.memory = memory; this.index = index; }

    @PostMapping("/events")
    public Result<MemoryEvent> ingest(@Valid @RequestBody EventRequest request) {
        enforceSubject(request.subjectType, request.subjectId);
        return Result.success(memory.ingest(new IngestMemoryCommand(tenant(), request.namespace, request.subjectType, request.subjectId, request.eventType, request.content, request.occurredAt, request.sourceType, request.sourceId, request.attributes, request.confidence, request.importance, request.confirmationPolicy)));
    }
    @PostMapping("/query")
    public Result<MemoryRetrievalResult> query(@Valid @RequestBody QueryRequest request) {
        enforceSubject(request.subjectType, request.subjectId);
        Set<GraphType> graphs = request.graphTypes == null || request.graphTypes.isEmpty() ? Set.of(GraphType.TEMPORAL, GraphType.SEMANTIC, GraphType.CAUSAL, GraphType.ENTITY) : request.graphTypes;
        return Result.success(memory.retrieveWithTrace(new MemoryQuery(tenant(), request.namespace, request.subjectType, request.subjectId, request.text, graphs, request.from, request.to,
                Math.min(Math.max(request.maxDepth, 0), 8), Math.min(Math.max(request.maxNodes, 1), 500), Math.min(Math.max(request.maxTokens, 1), 20_000), request.intent)));
    }
    @PostMapping("/events/{id}/confirm") public Result<Void> confirm(@PathVariable String id) { memory.confirm(tenant(), id); return Result.success(); }
    @PostMapping("/events/{id}/revoke") public Result<Void> revoke(@PathVariable String id) { memory.revoke(tenant(), id); return Result.success(); }
    @PostMapping("/events/{id}/supersede") public Result<MemoryEvent> supersede(@PathVariable String id, @Valid @RequestBody EventRequest request) {
        enforceSubject(request.subjectType, request.subjectId);
        return Result.success(memory.supersede(tenant(), id, new IngestMemoryCommand(tenant(), request.namespace, request.subjectType, request.subjectId, request.eventType, request.content, request.occurredAt, request.sourceType, request.sourceId, request.attributes, request.confidence, request.importance, request.confirmationPolicy)));
    }
    @GetMapping("/events/{id}/relations") public Result<List<MemoryEdge>> relations(@PathVariable String id, @RequestParam GraphType graphType, @RequestParam(defaultValue = "50") int limit) { return Result.success(memory.relations(tenant(), id, graphType, limit)); }
    @GetMapping("/retrieval-traces/{traceId}") public Result<MemoryRetrievalTrace> trace(@PathVariable String traceId) { return Result.success(memory.trace(tenant(), traceId)); }
    @SaCheckPermission("memory:admin")
    @PostMapping("/admin/indexes/rebuild") public Result<Void> rebuild(@RequestParam String namespace) { index.rebuild(namespace); return Result.success(); }

    private long tenant() { Long id = UserContextHolder.getCurrentTenantId(); if (id == null) throw new IllegalStateException("tenant context is required"); return id; }
    private void enforceSubject(String subjectType, String subjectId) {
        Long user = UserContextHolder.getUserId();
        if (!UserContextHolder.isSuperAdmin() && (user == null || !("USER".equalsIgnoreCase(subjectType) || "STUDENT".equalsIgnoreCase(subjectType)) || !String.valueOf(user).equals(subjectId))) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "subject access denied");
        }
    }
    @Data public static class EventRequest { private String namespace; private String subjectType; private String subjectId; private String eventType; private String content; private java.time.Instant occurredAt; private String sourceType; private String sourceId; private java.util.Map<String,Object> attributes; private double confidence = 0.5; private double importance = 0.5; private ConfirmationPolicy confirmationPolicy = ConfirmationPolicy.REQUIRED; }
    @Data public static class QueryRequest { private String namespace; private String subjectType; private String subjectId; private String text; private Set<GraphType> graphTypes; private java.time.Instant from; private java.time.Instant to; private int maxDepth = 2; private int maxNodes = 20; private int maxTokens = 2000; private MemoryQueryIntent intent; }
}
