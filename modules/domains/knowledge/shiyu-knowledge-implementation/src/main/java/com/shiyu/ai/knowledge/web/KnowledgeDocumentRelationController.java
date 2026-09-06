package com.shiyu.ai.knowledge.web;

import com.shiyu.ai.knowledge.web.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点文档关系")
@SaCheckPermission("knowledge:list")
public class KnowledgeDocumentRelationController {

    private final KnowledgeDocumentRelationService service;

    @GetMapping("/points/{pointId}/documents")
    public Result<List<KnowledgeDocumentRelationService.DocumentSummary>> list(
            @PathVariable Long pointId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.listDocuments(currentActor(), pointId));
    }

    @PutMapping("/points/{pointId}/documents")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replace(@PathVariable Long pointId,
                                @RequestBody @Valid ReplaceRequest request,
                                @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                        defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replaceDocuments(currentActor(), pointId, request.documentIds(), request.relationType());
        return Result.success();
    }

    @GetMapping("/documents/{documentId}/points")
    public Result<List<Long>> listPoints(@PathVariable Long documentId,
                                         @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                 defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.listPointIds(currentActor(), documentId));
    }

    @PutMapping("/documents/{documentId}/points")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replacePoints(@PathVariable Long documentId,
                                      @RequestBody @Valid ReplacePointsRequest request,
                                      @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                              defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replacePoints(currentActor(), documentId, request.pointIds(), request.relationType());
        return Result.success();
    }

    @GetMapping("/documents/{documentId}/relations")
    public Result<List<KnowledgeDocumentRelationService.DocumentRelationView>> listDocumentRelations(
            @PathVariable Long documentId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.listDocumentRelations(currentActor(), documentId));
    }

    @PutMapping("/documents/{documentId}/relations")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replaceDocumentRelations(@PathVariable Long documentId,
                                                  @RequestBody @Valid ReplaceDocumentRelationsRequest request,
                                                  @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                          defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replaceDocumentRelations(currentActor(), documentId, request.relations());
        return Result.success();
    }

    public record ReplaceRequest(@NotNull List<Long> documentIds, String relationType) {
    }

    public record ReplacePointsRequest(@NotNull List<Long> pointIds, String relationType) {
    }

    public record ReplaceDocumentRelationsRequest(
            @NotNull List<KnowledgeDocumentRelationService.DocumentRelationRequest> relations) {
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
