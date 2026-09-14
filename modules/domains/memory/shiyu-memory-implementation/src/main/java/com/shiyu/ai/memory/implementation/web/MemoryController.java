package com.shiyu.ai.memory.implementation.web;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryRetrievalResult;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryRetrievalTrace;
import com.shiyu.ai.memory.implementation.domain.magma.port.MemorySemanticIndex;
import com.shiyu.ai.memory.implementation.domain.magma.service.MagmaMemoryService;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
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
 * {@code MemoryController} 是Web模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
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
     * {@code MemoryController} 创建并初始化当前类型实例。
     *
     * @param memory 参数值，用于执行当前操作。
     * @param index 参数值，用于执行当前操作。
     */
    public MemoryController(MagmaMemoryService memory, MemorySemanticIndex index) {
        this.memory = memory;
        this.index = index;
    }

    /**
     * {@code ingest} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code query} 查询并返回当前操作所需的数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code confirm} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/events/{id}/confirm")
    public Result<Void> confirm(@PathVariable String id) {
        enforceEventSubject(memory.requireAccessibleEvent(tenant(), id));
        memory.confirm(tenant(), id);
        return Result.success();
    }

    /**
     * {@code revoke} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/events/{id}/revoke")
    public Result<Void> revoke(@PathVariable String id) {
        enforceEventSubject(memory.requireAccessibleEvent(tenant(), id));
        memory.revoke(tenant(), id);
        return Result.success();
    }

    /**
     * {@code supersede} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code relations} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param graphType 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code trace} 执行当前类型定义的业务操作。
     *
     * @param traceId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/retrieval-traces/{traceId}")
    public Result<MemoryRetrievalTrace> trace(@PathVariable String traceId) {
        long user = ActorContextHttpAdapter.userId();
        String subjectType = ActorContextHttpAdapter.platformAdmin() ? null : "USER";
        String subjectId = ActorContextHttpAdapter.platformAdmin() ? null : String.valueOf(user);
        return Result.success(memory.trace(tenant(), traceId, subjectType, subjectId));
    }

    /**
     * {@code rebuild} 执行当前类型定义的业务操作。
     *
     * @param namespace 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code EventRequest} 表示Web模块的请求参数，承载调用方提交的输入数据。
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
     * {@code QueryRequest} 表示Web模块的请求参数，承载调用方提交的输入数据。
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
