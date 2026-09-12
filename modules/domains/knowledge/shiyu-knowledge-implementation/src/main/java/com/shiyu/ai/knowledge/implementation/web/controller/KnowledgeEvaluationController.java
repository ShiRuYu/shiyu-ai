package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.CaseView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.CreateCaseRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.RunRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.RunResult;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code KnowledgeEvaluationController} 是知识模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge/evaluations")
@RequiredArgsConstructor
@Tag(name = "知识评测")
@SaCheckPermission("knowledge:list")
public class KnowledgeEvaluationController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeEvaluationService service;

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param spaceId 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping
    public Result<PageData<KnowledgeEvaluationService.CaseView>> page(
            @RequestParam Long spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.page(currentActor(), pageNum, pageSize, spaceId));
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
    @SaCheckPermission("knowledge:edit")
    public Result<KnowledgeEvaluationService.CaseView> create(
            @RequestBody @Valid KnowledgeEvaluationService.CreateCaseRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.create(currentActor(), request));
    }

    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param request 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/run")
    @SaCheckPermission("knowledge:list")
    public Result<KnowledgeEvaluationService.RunResult> run(
            @RequestBody @Valid KnowledgeEvaluationService.RunRequest request,
            @RequestHeader(
                            value = KnowledgeApiVersion.HEADER,
                            defaultValue = KnowledgeApiVersion.CURRENT)
                    String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.run(currentActor(), request));
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
    @SaCheckPermission("knowledge:edit")
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
