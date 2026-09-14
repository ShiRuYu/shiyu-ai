package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService.DocumentRelationRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService.DocumentRelationView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService.DocumentSummary;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
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
 * {@code KnowledgeDocumentRelationController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点文档关系")
@SaCheckPermission("knowledge:list")
public class KnowledgeDocumentRelationController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeDocumentRelationService service;

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pointId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
     * {@code replace} 执行当前类型定义的业务操作。
     *
     * @param pointId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code listPoints} 查询并返回当前操作所需的数据。
     *
     * @param documentId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
     * {@code replacePoints} 执行当前类型定义的业务操作。
     *
     * @param documentId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code replaceDocumentRelations} 执行当前类型定义的业务操作。
     *
     * @param documentId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code ReplaceRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param documentIds documentIds 属性，表示该记录组件承载的数据。
     * @param relationType relationType 属性，表示该记录组件承载的数据。
     */
    public record ReplaceRequest(@NotNull List<Long> documentIds, String relationType) {}

    /**
     * {@code ReplacePointsRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param pointIds pointIds 属性，表示该记录组件承载的数据。
     * @param relationType relationType 属性，表示该记录组件承载的数据。
     */
    public record ReplacePointsRequest(@NotNull List<Long> pointIds, String relationType) {}

    /**
     * {@code ReplaceDocumentRelationsRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param relations relations 属性，表示该记录组件承载的数据。
     */
    public record ReplaceDocumentRelationsRequest(
            @NotNull List<KnowledgeDocumentRelationService.DocumentRelationRequest> relations) {}

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
