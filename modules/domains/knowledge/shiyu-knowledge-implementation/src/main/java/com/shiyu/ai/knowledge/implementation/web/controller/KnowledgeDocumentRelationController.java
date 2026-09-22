package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService.DocumentRelationRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService.DocumentRelationView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService.DocumentSummary;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService;

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

/**
 * 处理 知识 文档 关系 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点文档关系")
public class KnowledgeDocumentRelationController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeDocumentRelationService service;

    /**
     * 查询 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param documents 用于完成本次业务处理的 documents 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @SaCheckPermission("knowledge:list")
    @GetMapping("/points/{pointId}/documents")
    public Result<List<KnowledgeDocumentRelationService.DocumentSummary>> list(
            @PathVariable Long pointId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.listDocuments(currentActor(), pointId));
    }

    /**
     * 执行 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param documents 用于完成本次业务处理的 documents 参数。
     * @return 返回 知识 文档 关系 相关操作生成的结果数据。
     */
    @PutMapping("/points/{pointId}/documents")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replace(
            @PathVariable Long pointId,
            @RequestBody @Valid ReplaceRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replaceDocuments(
                currentActor(), pointId, request.documentIds(), request.relationType());
        return Result.success();
    }

    /**
     * 查询 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param points 用于完成本次业务处理的 points 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @SaCheckPermission("knowledge:list")
    @GetMapping("/documents/{documentId}/points")
    public Result<List<Long>> listPoints(
            @PathVariable Long documentId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.listPointIds(currentActor(), documentId));
    }

    /**
     * 执行 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param points 用于完成本次业务处理的 points 参数。
     * @return 返回 知识 文档 关系 相关操作生成的结果数据。
     */
    @PutMapping("/documents/{documentId}/points")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replacePoints(
            @PathVariable Long documentId,
            @RequestBody @Valid ReplacePointsRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replacePoints(
                currentActor(), documentId, request.pointIds(), request.relationType());
        return Result.success();
    }

    @SaCheckPermission("knowledge:list")
    @GetMapping("/documents/{documentId}/relations")
    public Result<List<KnowledgeDocumentRelationService.DocumentRelationView>>
            listDocumentRelations(
                    @PathVariable Long documentId,
                    @RequestHeader(
                                    value = KnowledgeApiVersion.HEADER,
                                    defaultValue = KnowledgeApiVersion.CURRENT)
                            String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.listDocumentRelations(currentActor(), documentId));
    }

    /**
     * 执行 知识 文档 关系 相关业务数据，并返回处理结果。
     *
     * @param relations 用于完成本次业务处理的 relations 参数。
     * @return 返回 知识 文档 关系 相关操作生成的结果数据。
     */
    @PutMapping("/documents/{documentId}/relations")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replaceDocumentRelations(
            @PathVariable Long documentId,
            @RequestBody @Valid ReplaceDocumentRelationsRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replaceDocumentRelations(currentActor(), documentId, request.relations());
        return Result.success();
    }

    /**
     * 封装 Replace 相关的不可变数据及其字段约束。
     */
    public record ReplaceRequest(@NotNull List<Long> documentIds, String relationType) {}

    /**
     * 封装 Replace Points 相关的不可变数据及其字段约束。
     */
    public record ReplacePointsRequest(@NotNull List<Long> pointIds, String relationType) {}

    /**
     * 封装 Replace 文档 Relations 相关的不可变数据及其字段约束。
     */
    public record ReplaceDocumentRelationsRequest(
            @NotNull List<KnowledgeDocumentRelationService.DocumentRelationRequest> relations) {}

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
