package com.shiyu.ai.knowledge.web;

import com.shiyu.ai.knowledge.web.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
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

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识检索")
@SaCheckPermission("knowledge:list")
public class KnowledgeSearchController {

    private final KnowledgeIndexService indexService;
    private final KnowledgeSpaceService spaceService;

    @PostMapping("/search")
    public Result<SearchResponse> search(@RequestBody @Valid SearchRequest request,
                                         @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                 defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        ActorContext actor = currentActor();
        spaceService.requireAccess(request.spaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        String mode = request.mode() == null || request.mode().isBlank()
                ? "HYBRID" : request.mode().trim().toUpperCase(java.util.Locale.ROOT);
        double threshold = request.threshold() == null ? 0D : request.threshold();
        List<KnowledgeIndexService.HybridHit> hits = indexService.hybridSearch(
                actor, request.spaceId(), request.query(), mode,
                request.topK() == null ? 5 : request.topK(), threshold,
                Boolean.TRUE.equals(request.rerank()));
        return Result.success(new SearchResponse(request.spaceId(), mode, hits));
    }

    @PostMapping("/index-jobs/rebuild")
    @SaCheckPermission("knowledge:index:rebuild")
    public Result<Long> rebuild(@RequestBody @Valid RebuildRequest request,
                                @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                        defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        ActorContext actor = currentActor();
        spaceService.requireAccess(request.spaceId(), KnowledgeSpaceService.SpaceRole.ADMIN, actor);
        return Result.success(indexService.rebuild(actor.tenantId(), request.spaceId()));
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }

    public record SearchRequest(@NotNull Long spaceId, @NotBlank String query,
                                String mode, @Min(1) @Max(100) Integer topK,
                                @jakarta.validation.constraints.DecimalMin("0.0")
                                @jakarta.validation.constraints.DecimalMax("1.0")
                                Double threshold, Boolean rerank) {
    }

    public record SearchResponse(Long spaceId, String mode,
                                 List<KnowledgeIndexService.HybridHit> hits) {
    }

    public record RebuildRequest(@NotNull Long spaceId) {
    }
}
