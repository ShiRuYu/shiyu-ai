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
 * 处理 知识 Search 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 知识 Search 相关业务数据，并返回处理结果。
     *
     * @param search 用于完成本次业务处理的 search 参数。
     * @return 返回 知识 Search 相关操作生成的结果数据。
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
     * 执行 知识 Search 相关业务数据，并返回处理结果。
     *
     * @param rebuild 用于完成本次业务处理的 rebuild 参数。
     * @return 返回 知识 Search 相关操作生成的结果数据。
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
     * 封装 Search 相关的不可变数据及其字段约束。
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
     * 封装 Search 相关的不可变数据及其字段约束。
     */
    public record SearchResponse(
            Long spaceId, String mode, List<KnowledgeIndexService.HybridHit> hits) {}

    /**
     * 封装 Rebuild 相关的不可变数据及其字段约束。
     */
    public record RebuildRequest(@NotNull Long spaceId) {}
}
