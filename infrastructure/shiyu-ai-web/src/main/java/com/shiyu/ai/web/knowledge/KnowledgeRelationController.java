package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识关系")
@SaCheckPermission("knowledge:list")
public class KnowledgeRelationController {

    private final KnowledgeRelationService service;

    @GetMapping("/points/{pointId}/relations")
    public Result<List<KnowledgeRelationService.RelationView>> list(
            @PathVariable Long pointId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.list(pointId));
    }

    @PostMapping("/points/{pointId}/relations")
    @SaCheckPermission("knowledge:relation")
    public Result<Void> create(@PathVariable Long pointId,
                               @RequestBody RelationRequest request,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        if (!pointId.equals(request.sourceId())) {
            throw new ServiceException("sourceId 必须与路径中的 pointId 一致");
        }
        service.addRelation(request.sourceId(), request.targetId(),
                request.type(), request.weight());
        return Result.success();
    }

    @DeleteMapping("/points/{pointId}/relations/{targetId}")
    @SaCheckPermission("knowledge:relation")
    public Result<Void> delete(@PathVariable Long pointId,
                               @PathVariable Long targetId,
                               RelationType type,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.removeRelation(pointId, targetId, type);
        return Result.success();
    }

    public record RelationRequest(@NotNull Long sourceId,
                                  @NotNull Long targetId,
                                  @NotNull RelationType type,
                                  Double weight) {
    }
}
