package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.service.KnowledgeJobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/knowledge/ingestion-jobs")
@RequiredArgsConstructor
@Tag(name = "知识任务")
@SaCheckPermission("knowledge:document:list")
public class KnowledgeJobController {

    private final KnowledgeJobService service;

    @GetMapping
    public Result<PageData<KnowledgeJobService.JobView>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Long spaceId,
            @RequestParam(required = false) String status,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.page(pageNum, Math.min(pageSize, 100), spaceId, status));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeJobService.JobView> get(@PathVariable Long id,
                                                   @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                           defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.get(id));
    }

    @PostMapping("/{id}/cancel")
    @SaCheckPermission("knowledge:index:rebuild")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.cancel(id);
        return Result.success();
    }

    @PostMapping("/{id}/retry")
    @SaCheckPermission("knowledge:index:rebuild")
    public Result<Void> retry(@PathVariable Long id,
                              @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                      defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.retry(id);
        return Result.success();
    }
}
