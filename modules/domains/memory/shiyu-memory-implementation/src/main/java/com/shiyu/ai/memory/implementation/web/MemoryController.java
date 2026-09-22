package com.shiyu.ai.memory.implementation.web;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryRetrievalResult;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryRetrievalTrace;
import com.shiyu.ai.memory.implementation.domain.magma.port.MemorySemanticIndex;
import com.shiyu.ai.memory.implementation.domain.magma.service.MagmaMemoryService;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 处理 记忆 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Tag(name = "MAGMA Memory Platform")
@RestController
@RequestMapping("/api/memory")
public class MemoryController {
    /**
     * memory 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MagmaMemoryService memory;
    /**
     * 索引，表示当前对象中的对应属性。
     */
    private final MemorySemanticIndex index;

    /**
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param memory 用于完成本次业务处理的 memory 参数。
     * @param index 用于完成本次业务处理的 index 参数。
     */
    public MemoryController(MagmaMemoryService memory, MemorySemanticIndex index) {
        this.memory = memory;
        this.index = index;
    }

    /**
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param events 用于完成本次业务处理的 events 参数。
     */
    @PostMapping("/events")
    public Result<MemoryEvent> ingest(@Valid @RequestBody EventRequest request) {
        enforceSubject(request.subjectType, request.subjectId);
        return Result.success(
                memory.ingest(
                        new IngestMemoryCommand(
                                tenant(),
                                request.namespace,
                                request.subjectType,
                                request.subjectId,
                                request.eventType,
                                request.content,
                                request.occurredAt,
                                request.sourceType,
                                request.sourceId,
                                request.attributes,
                                request.confidence,
                                request.importance,
                                request.confirmationPolicy)));
    }

    /**
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param query 用于筛选目标数据的查询条件。
     */
    @PostMapping("/query")
    public Result<MemoryRetrievalResult> query(@Valid @RequestBody QueryRequest request) {
        enforceSubject(request.subjectType, request.subjectId);
        Set<GraphType> graphs =
                request.graphTypes == null || request.graphTypes.isEmpty()
                        ? Set.of(
                                GraphType.TEMPORAL,
                                GraphType.SEMANTIC,
                                GraphType.CAUSAL,
                                GraphType.ENTITY)
                        : request.graphTypes;
        return Result.success(
                memory.retrieveWithTrace(
                        new MemoryQuery(
                                tenant(),
                                request.namespace,
                                request.subjectType,
                                request.subjectId,
                                request.text,
                                graphs,
                                request.from,
                                request.to,
                                Math.min(Math.max(request.maxDepth, 0), 8),
                                Math.min(Math.max(request.maxNodes, 1), 500),
                                Math.min(Math.max(request.maxTokens, 1), 20_000),
                                request.intent)));
    }

    /**
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param confirm 用于完成本次业务处理的 confirm 参数。
     */
    @PostMapping("/events/{id}/confirm")
    public Result<Void> confirm(@PathVariable String id) {
        enforceEventSubject(memory.requireAccessibleEvent(tenant(), id));
        memory.confirm(tenant(), id);
        return Result.success();
    }

    /**
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param revoke 用于完成本次业务处理的 revoke 参数。
     */
    @PostMapping("/events/{id}/revoke")
    public Result<Void> revoke(@PathVariable String id) {
        enforceEventSubject(memory.requireAccessibleEvent(tenant(), id));
        memory.revoke(tenant(), id);
        return Result.success();
    }

    /**
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param supersede 用于完成本次业务处理的 supersede 参数。
     */
    @PostMapping("/events/{id}/supersede")
    public Result<MemoryEvent> supersede(
            @PathVariable String id, @Valid @RequestBody EventRequest request) {
        enforceEventSubject(memory.requireAccessibleEvent(tenant(), id));
        enforceSubject(request.subjectType, request.subjectId);
        return Result.success(
                memory.supersede(
                        tenant(),
                        id,
                        new IngestMemoryCommand(
                                tenant(),
                                request.namespace,
                                request.subjectType,
                                request.subjectId,
                                request.eventType,
                                request.content,
                                request.occurredAt,
                                request.sourceType,
                                request.sourceId,
                                request.attributes,
                                request.confidence,
                                request.importance,
                                request.confirmationPolicy)));
    }

    /**
     * 执行 记忆 相关业务数据，并返回处理结果。
     *
     * @param relations 用于完成本次业务处理的 relations 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @GetMapping("/events/{id}/relations")
    public Result<List<MemoryEdge>> relations(
            @PathVariable String id,
            @RequestParam GraphType graphType,
            @RequestParam(defaultValue = "50") int limit) {
        enforceEventSubject(memory.requireAccessibleEvent(tenant(), id));
        return Result.success(memory.relations(tenant(), id, graphType, limit));
    }

    /**
     * 查询 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param traceId 用于定位trace的标识。
     */
    @GetMapping("/retrieval-traces/{traceId}")
    public Result<MemoryRetrievalTrace> trace(@PathVariable String traceId) {
        long user = ActorContextHttpAdapter.userId();
        String subjectType = ActorContextHttpAdapter.platformAdmin() ? null : "USER";
        String subjectId = ActorContextHttpAdapter.platformAdmin() ? null : String.valueOf(user);
        return Result.success(memory.trace(tenant(), traceId, subjectType, subjectId));
    }

    /**
     * 执行 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param admin 用于完成本次业务处理的 admin 参数。
     */
    @SaCheckPermission("memory:admin")
    @PostMapping("/admin/indexes/rebuild")
    public Result<Void> rebuild(@RequestParam String namespace) {
        index.rebuild(tenant(), namespace);
        return Result.success();
    }

    private TenantId tenant() {
        return ActorContextHttpAdapter.currentActor().tenantId();
    }

    private void enforceSubject(String subjectType, String subjectId) {
        long user = ActorContextHttpAdapter.userId();
        if (!ActorContextHttpAdapter.platformAdmin()
                && (!("USER".equalsIgnoreCase(subjectType)
                                || "STUDENT".equalsIgnoreCase(subjectType))
                        || !String.valueOf(user).equals(subjectId))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "subject access denied");
        }
    }

    private void enforceEventSubject(MemoryEvent event) {
        enforceSubject(event.subjectType(), event.subjectId());
    }

    /**
     * 封装 事件 操作所需的请求条件和输入数据。
     */
    @Data
    public static class EventRequest {
        private String namespace;
        /**
         * 学科类型，表示当前对象中的对应属性。
         */
        private String subjectType;
        /**
         * 学科标识，表示当前对象中的对应属性。
         */
        private String subjectId;
        /**
         * eventType 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String eventType;
        /**
         * 内容，表示当前对象中的对应属性。
         */
        private String content;
        /**
         * occurredAt 属性，保存当前对象中的业务数据或协作依赖。
         */
        private java.time.Instant occurredAt;
        /**
         * 来源类型，表示当前对象中的对应属性。
         */
        private String sourceType;
        /**
         * 来源标识，表示当前对象中的对应属性。
         */
        private String sourceId;
        /**
         * attributes 属性，保存当前对象中的业务数据或协作依赖。
         */
        private java.util.Map<String, Object> attributes;
        /**
         * confidence 属性，保存当前对象中的业务数据或协作依赖。
         */
        private double confidence = 0.5;
        /**
         * importance 属性，保存当前对象中的业务数据或协作依赖。
         */
        private double importance = 0.5;
        /**
         * confirmationPolicy 属性，保存当前对象中的业务数据或协作依赖。
         */
        private ConfirmationPolicy confirmationPolicy = ConfirmationPolicy.REQUIRED;
    }

    /**
     * 封装 Query 操作所需的请求条件和输入数据。
     */
    @Data
    public static class QueryRequest {
        private String namespace;
        /**
         * 学科类型，表示当前对象中的对应属性。
         */
        private String subjectType;
        /**
         * 学科标识，表示当前对象中的对应属性。
         */
        private String subjectId;
        /**
         * text 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String text;
        /**
         * graphTypes 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Set<GraphType> graphTypes;
        /**
         * from 属性，保存当前对象中的业务数据或协作依赖。
         */
        private java.time.Instant from;
        /**
         * to 属性，保存当前对象中的业务数据或协作依赖。
         */
        private java.time.Instant to;
        /**
         * maxDepth 属性，保存当前对象中的业务数据或协作依赖。
         */
        private int maxDepth = 2;
        /**
         * maxNodes 属性，保存当前对象中的业务数据或协作依赖。
         */
        private int maxNodes = 20;
        /**
         * maxTokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        private int maxTokens = 2000;
        /**
         * intent 属性，保存当前对象中的业务数据或协作依赖。
         */
        private MemoryQueryIntent intent;
    }
}
