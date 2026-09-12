package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.CreateSpaceRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.DifficultyScaleView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.MemberRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.MemberView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.UpdateSpaceRequest;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;

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

import java.util.List;

/**
 * {@code KnowledgeSpaceController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge/spaces")
@RequiredArgsConstructor
@Tag(name = "知识空间")
@SaCheckPermission("knowledge:list")
public class KnowledgeSpaceController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeSpaceService service;

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param domainCode 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping
    public Result<PageData<KnowledgeSpaceService.SpaceView>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String domainCode,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(
                service.page(
                        currentActor(), pageNum, Math.min(pageSize, 100), keyword, domainCode));
    }

    /**
     * {@code options} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/options")
    public Result<List<KnowledgeSpaceService.SpaceView>> options() {
        return Result.success(service.accessibleSpaces(currentActor()));
    }

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/{id}")
    public Result<KnowledgeSpaceService.SpaceView> get(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.get(currentActor(), id));
    }

    /**
     * {@code difficultyScale} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/{id}/difficulty-scale")
    public Result<KnowledgeSpaceService.DifficultyScaleView> difficultyScale(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.difficultyScale(currentActor(), id));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping
    @SaCheckPermission("knowledge:create")
    public Result<KnowledgeSpaceService.SpaceView> create(
            @RequestBody @Valid KnowledgeSpaceService.CreateSpaceRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.create(currentActor(), request));
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
    @PutMapping("/{id}")
    @SaCheckPermission("knowledge:edit")
    public Result<KnowledgeSpaceService.SpaceView> update(
            @PathVariable Long id,
            @RequestBody @Valid KnowledgeSpaceService.UpdateSpaceRequest request,
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
    @DeleteMapping("/{id}")
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

    /**
     * {@code members} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/{id}/members")
    public Result<List<KnowledgeSpaceService.MemberView>> members(
            @PathVariable Long id,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.members(currentActor(), id));
    }

    /**
     * {@code replaceMembers} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param members 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PutMapping("/{id}/members")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replaceMembers(
            @PathVariable Long id,
            @RequestBody @Valid List<KnowledgeSpaceService.MemberRequest> members,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replaceMembers(currentActor(), id, members);
        return Result.success();
    }

    /**
     * {@code ensureDefault} 执行当前类型定义的业务操作。
     *
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/default")
    public Result<KnowledgeSpaceService.SpaceView> ensureDefault(
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.ensureDefaultSpace(currentActor()));
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
