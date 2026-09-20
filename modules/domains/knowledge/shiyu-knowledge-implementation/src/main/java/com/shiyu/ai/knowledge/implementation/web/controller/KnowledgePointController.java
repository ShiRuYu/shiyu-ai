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
 * 处理 知识 Point 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 查询 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param points 用于完成本次业务处理的 points 参数。
     * @return 返回 知识 Point 相关操作生成的结果数据。
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
     * 查询 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Point 相关操作生成的结果数据。
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
     * 执行 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param graph 用于完成本次业务处理的 graph 参数。
     * @return 返回 知识 Point 相关操作生成的结果数据。
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
     * 创建或保存 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param points 用于完成本次业务处理的 points 参数。
     * @return 返回 知识 Point 相关操作生成的结果数据。
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
     * 更新或设置 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Point 相关操作生成的结果数据。
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
     * 删除或移除 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Point 相关操作生成的结果数据。
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
