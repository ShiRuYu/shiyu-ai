package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.service.KnowledgeEvaluationService;
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

@RestController
@RequestMapping("/knowledge/evaluations")
@RequiredArgsConstructor
@Tag(name = "知识评测")
@SaCheckPermission("knowledge:list")
public class KnowledgeEvaluationController {

    private final KnowledgeEvaluationService service;

    @GetMapping
    public Result<PageData<KnowledgeEvaluationService.CaseView>> page(
            @RequestParam Long spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.page(pageNum, pageSize, spaceId));
    }

    @PostMapping
    @SaCheckPermission("knowledge:edit")
    public Result<KnowledgeEvaluationService.CaseView> create(
            @RequestBody @Valid KnowledgeEvaluationService.CreateCaseRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.create(request));
    }

    @PostMapping("/run")
    @SaCheckPermission("knowledge:list")
    public Result<KnowledgeEvaluationService.RunResult> run(
            @RequestBody @Valid KnowledgeEvaluationService.RunRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.run(request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.delete(id);
        return Result.success();
    }
}
