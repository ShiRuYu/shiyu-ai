package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.CaseView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.CreateCaseRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.RunRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeEvaluationService.RunResult;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 知识 Evaluation 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge/evaluations")
@RequiredArgsConstructor
@Tag(name = "知识评测")
public class KnowledgeEvaluationController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeEvaluationService service;

    /**
     * 查询 知识 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param spaceId 用于定位space的标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param version 用于完成本次业务处理的 version 参数。
     * @return 返回 知识 Evaluation 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:list")
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
     * 创建或保存 知识 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param edit 用于完成本次业务处理的 edit 参数。
     * @return 返回 知识 Evaluation 相关操作生成的结果数据。
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
     * 执行 知识 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @return 返回 知识 Evaluation 相关操作生成的结果数据。
     */
    @PostMapping("/run")
    @SaCheckPermission("knowledge:edit")
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
     * 删除或移除 知识 Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Evaluation 相关操作生成的结果数据。
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
