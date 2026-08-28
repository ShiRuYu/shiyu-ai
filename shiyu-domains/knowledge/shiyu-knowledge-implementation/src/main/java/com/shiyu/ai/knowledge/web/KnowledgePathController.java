package com.shiyu.ai.knowledge.web;

import com.shiyu.ai.knowledge.web.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.path.KnowledgePathService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识路径")
@SaCheckPermission("knowledge:list")
public class KnowledgePathController {

    private final KnowledgePathService service;

    @GetMapping("/points/{pointId}/path")
    public Result<List<Long>> path(@PathVariable Long pointId,
                                   @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                           defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.generatePath(currentActor(), pointId));
    }

    @GetMapping("/points/{pointId}/prerequisites")
    public Result<List<Long>> prerequisites(
            @PathVariable Long pointId,
            @RequestParam(required = false, defaultValue = "") Set<Long> masteredIds,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.findMissingPrerequisites(currentActor(), pointId, masteredIds));
    }

    @GetMapping("/points/path")
    public Result<List<Long>> findPath(
            @RequestParam Long fromId,
            @RequestParam Long toId,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.findPath(currentActor(), fromId, toId));
    }

    private ActorContext currentActor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
