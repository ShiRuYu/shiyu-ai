package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeRelationService.RelationView;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeRelationService;
import com.shiyu.ai.knowledge.implementation.domain.RelationType;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@code KnowledgeRelationController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识关系")
@SaCheckPermission("knowledge:list")
public class KnowledgeRelationController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeRelationService service;

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param pointId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/points/{pointId}/relations")
    public Result<List<KnowledgeRelationService.RelationView>> list(
            @PathVariable Long pointId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.list(currentActor(), pointId));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param pointId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/points/{pointId}/relations")
    @SaCheckPermission("knowledge:relation")
    public Result<Void> create(
            @PathVariable Long pointId,
            @RequestBody RelationRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        if (!pointId.equals(request.sourceId())) {
            throw new ServiceException("sourceId 必须与路径中的 pointId 一致");
        }
        service.addRelation(
                currentActor(),
                request.sourceId(),
                request.targetId(),
                request.type(),
                request.weight());
        return Result.success();
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param pointId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/points/{pointId}/relations/{targetId}")
    @SaCheckPermission("knowledge:relation")
    public Result<Void> delete(
            @PathVariable Long pointId,
            @PathVariable Long targetId,
            RelationType type,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.removeRelation(currentActor(), pointId, targetId, type);
        return Result.success();
    }

    /**
     * {@code RelationRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param sourceId sourceId 属性，表示该记录组件承载的数据。
     * @param targetId targetId 属性，表示该记录组件承载的数据。
     * @param type 类型，表示该记录组件承载的数据。
     * @param weight weight 属性，表示该记录组件承载的数据。
     */
    public record RelationRequest(
            @NotNull Long sourceId,
            @NotNull Long targetId,
            @NotNull RelationType type,
            Double weight) {}

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
