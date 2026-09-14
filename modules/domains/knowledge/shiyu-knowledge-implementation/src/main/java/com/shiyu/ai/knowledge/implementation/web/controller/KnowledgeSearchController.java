package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService.HybridHit;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@code KnowledgeSearchController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识检索")
@SaCheckPermission("knowledge:list")
public class KnowledgeSearchController {

    /**
     * 索引服务，表示当前对象中的对应属性。
     */
    private final KnowledgeIndexService indexService;
    /**
     * spaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceService spaceService;

    /**
     * {@code search} 查询并返回当前操作所需的数据。
     *
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/search")
    public Result<SearchResponse> search(
            @RequestBody @Valid SearchRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        ActorContext actor = currentActor();
        spaceService.requireAccess(
                request.spaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        String mode =
                request.mode() == null || request.mode().isBlank()
                        ? "HYBRID"
                        : request.mode().trim().toUpperCase(java.util.Locale.ROOT);
        double threshold = request.threshold() == null ? 0D : request.threshold();
        List<KnowledgeIndexService.HybridHit> hits =
                indexService.hybridSearch(
                        actor,
                        request.spaceId(),
                        request.query(),
                        mode,
                        request.topK() == null ? 5 : request.topK(),
                        threshold,
                        Boolean.TRUE.equals(request.rerank()));
        return Result.success(new SearchResponse(request.spaceId(), mode, hits));
    }

    /**
     * {@code rebuild} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/index-jobs/rebuild")
    @SaCheckPermission("knowledge:index:rebuild")
    public Result<Long> rebuild(
            @RequestBody @Valid RebuildRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        ActorContext actor = currentActor();
        spaceService.requireAccess(request.spaceId(), KnowledgeSpaceService.SpaceRole.ADMIN, actor);
        return Result.success(indexService.rebuild(actor.tenantId(), request.spaceId()));
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }

    /**
     * {@code SearchRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param query query 属性，表示该记录组件承载的数据。
     * @param mode mode 属性，表示该记录组件承载的数据。
     * @param topK topK 属性，表示该记录组件承载的数据。
     * @param threshold threshold 属性，表示该记录组件承载的数据。
     * @param rerank rerank 属性，表示该记录组件承载的数据。
     */
    public record SearchRequest(
            @NotNull Long spaceId,
            @NotBlank String query,
            String mode,
            @Min(1) @Max(100) Integer topK,
            @jakarta.validation.constraints.DecimalMin("0.0")
                    @jakarta.validation.constraints.DecimalMax("1.0")
                    Double threshold,
            Boolean rerank) {}

    /**
     * {@code SearchResponse} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param mode mode 属性，表示该记录组件承载的数据。
     * @param hits hits 属性，表示该记录组件承载的数据。
     */
    public record SearchResponse(
            Long spaceId, String mode, List<KnowledgeIndexService.HybridHit> hits) {}

    /**
     * {@code RebuildRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     */
    public record RebuildRequest(@NotNull Long spaceId) {}
}
