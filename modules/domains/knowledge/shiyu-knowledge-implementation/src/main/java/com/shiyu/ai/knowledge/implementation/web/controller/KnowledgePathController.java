package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.infrastructure.path.KnowledgePathService;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 处理 知识 Path 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识路径")
@SaCheckPermission("knowledge:list")
public class KnowledgePathController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgePathService service;

    /**
     * 执行 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param path 用于完成本次业务处理的 path 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @GetMapping("/points/{pointId}/path")
    public Result<List<Long>> path(
            @PathVariable Long pointId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.generatePath(currentActor(), pointId));
    }

    /**
     * 执行 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param prerequisites 用于完成本次业务处理的 prerequisites 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @GetMapping("/points/{pointId}/prerequisites")
    public Result<List<Long>> prerequisites(
            @PathVariable Long pointId,
            @RequestParam(required = false, defaultValue = "") Set<Long> masteredIds,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(
                service.findMissingPrerequisites(currentActor(), pointId, masteredIds));
    }

    /**
     * 查询 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param path 用于完成本次业务处理的 path 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @GetMapping("/points/path")
    public Result<List<Long>> findPath(
            @RequestParam Long fromId,
            @RequestParam Long toId,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.findPath(currentActor(), fromId, toId));
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
