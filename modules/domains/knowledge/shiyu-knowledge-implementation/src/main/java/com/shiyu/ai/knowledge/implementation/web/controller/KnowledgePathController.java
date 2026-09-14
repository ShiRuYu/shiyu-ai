package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
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
 * {@code KnowledgePathController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
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
     * {@code path} 执行当前类型定义的业务操作。
     *
     * @param pointId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code prerequisites} 执行当前类型定义的业务操作。
     *
     * @param pointId 参数值，用于执行当前操作。
     * @param masteredIds 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code findPath} 查询并返回当前操作所需的数据。
     *
     * @param fromId 参数值，用于执行当前操作。
     * @param toId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
