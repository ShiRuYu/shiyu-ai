package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeJobService.JobView;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeJobService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理 知识 Job 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge/ingestion-jobs")
@RequiredArgsConstructor
@Tag(name = "知识任务")
@SaCheckPermission("knowledge:document:list")
public class KnowledgeJobController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeJobService service;

    /**
     * 查询 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @param version 用于完成本次业务处理的 version 参数。
     * @return 返回 知识 Job 相关操作生成的结果数据。
     */
    @GetMapping
    public Result<PageData<KnowledgeJobService.JobView>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long spaceId,
            @RequestParam(required = false) String status,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(
                service.page(currentActor(), pageNum, Math.min(pageSize, 100), spaceId, status));
    }

    /**
     * 查询 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Job 相关操作生成的结果数据。
     */
    @GetMapping("/{id}")
    public Result<KnowledgeJobService.JobView> get(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.get(currentActor(), id));
    }

    /**
     * 校验或判断 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param cancel 用于完成本次业务处理的 cancel 参数。
     * @return 返回 知识 Job 相关操作生成的结果数据。
     */
    @PostMapping("/{id}/cancel")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> cancel(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.cancel(currentActor(), id);
        return Result.success();
    }

    /**
     * 执行 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param retry 用于完成本次业务处理的 retry 参数。
     * @return 返回 知识 Job 相关操作生成的结果数据。
     */
    @PostMapping("/{id}/retry")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> retry(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.retry(currentActor(), id);
        return Result.success();
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
