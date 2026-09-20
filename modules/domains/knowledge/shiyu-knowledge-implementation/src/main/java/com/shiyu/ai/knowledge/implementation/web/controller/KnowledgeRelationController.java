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
 * 处理 知识 关系 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param relations 用于完成本次业务处理的 relations 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 创建或保存 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param relations 用于完成本次业务处理的 relations 参数。
     * @return 返回 知识 关系 相关操作生成的结果数据。
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
     * 删除或移除 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param targetId 用于定位target的标识。
     * @return 返回 知识 关系 相关操作生成的结果数据。
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
     * 封装 关系 相关的不可变数据及其字段约束。
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
