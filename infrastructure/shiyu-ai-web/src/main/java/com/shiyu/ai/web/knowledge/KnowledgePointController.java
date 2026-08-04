package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
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

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识点")
@SaCheckPermission("knowledge:list")
public class KnowledgePointController {

    private final KnowledgePointService service;

    @GetMapping("/spaces/{spaceId}/points")
    public Result<PageData<KnowledgePointService.PointView>> page(
            @PathVariable Long spaceId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.page(spaceId, pageNum, pageSize, keyword, category));
    }

    @GetMapping("/points/{id}")
    public Result<KnowledgePointService.PointView> get(@PathVariable Long id,
                                                        @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.get(id));
    }

    @GetMapping("/points/{id}/graph")
    public Result<com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse> graph(
            @PathVariable Long id,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.graph(id));
    }

    @PostMapping("/spaces/{spaceId}/points")
    @SaCheckPermission("knowledge:create")
    public Result<KnowledgePointService.PointView> create(
            @PathVariable Long spaceId,
            @RequestBody @Valid KnowledgePointService.CreatePointRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.create(spaceId, request));
    }

    @PutMapping("/points/{id}")
    @SaCheckPermission("knowledge:edit")
    public Result<KnowledgePointService.PointView> update(
            @PathVariable Long id,
            @RequestBody @Valid KnowledgePointService.UpdatePointRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.update(id, request));
    }

    @DeleteMapping("/points/{id}")
    @SaCheckPermission("knowledge:delete")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.delete(id);
        return Result.success();
    }
}
