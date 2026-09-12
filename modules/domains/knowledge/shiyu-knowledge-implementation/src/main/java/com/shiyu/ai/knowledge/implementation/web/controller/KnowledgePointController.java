package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService.CreatePointRequest;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService.PointView;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService.UpdatePointRequest;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeGraphResponse;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code KnowledgePointController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点")
@SaCheckPermission("knowledge:list")
public class KnowledgePointController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgePointService service;

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param spaceId 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param category 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/spaces/{spaceId}/points")
    public Result<PageData<KnowledgePointService.PointView>> page(
            @PathVariable Long spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(
                service.page(currentActor(), spaceId, pageNum, pageSize, keyword, category));
    }

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/points/{id}")
    public Result<KnowledgePointService.PointView> get(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.get(currentActor(), id));
    }

    /**
     * {@code graph} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/points/{id}/graph")
    public Result<com.shiyu.ai.knowledge.implementation.web.response.KnowledgeGraphResponse> graph(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.graph(currentActor(), id));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param spaceId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/spaces/{spaceId}/points")
    @SaCheckPermission("knowledge:create")
    public Result<KnowledgePointService.PointView> create(
            @PathVariable Long spaceId,
            @RequestBody @Valid KnowledgePointService.CreatePointRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.create(currentActor(), spaceId, request));
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PutMapping("/points/{id}")
    @SaCheckPermission("knowledge:edit")
    public Result<KnowledgePointService.PointView> update(
            @PathVariable Long id,
            @RequestBody @Valid KnowledgePointService.UpdatePointRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.update(currentActor(), id, request));
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/points/{id}")
    @SaCheckPermission("knowledge:delete")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.delete(currentActor(), id);
        return Result.success();
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
