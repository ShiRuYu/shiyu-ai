package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/knowledge/v2/spaces")
@RequiredArgsConstructor
@Tag(name = "知识空间 V2")
@SaCheckPermission("knowledge:list")
public class KnowledgeSpaceV2Controller {

    private final KnowledgeSpaceService service;

    @GetMapping
    public Result<PageData<KnowledgeSpaceService.SpaceView>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(service.page(pageNum, Math.min(pageSize, 100), keyword));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeSpaceService.SpaceView> get(@PathVariable Long id) {
        return Result.success(service.get(id));
    }

    @PostMapping
    @SaCheckPermission("knowledge:create")
    public Result<KnowledgeSpaceService.SpaceView> create(
            @RequestBody @Valid KnowledgeSpaceService.CreateSpaceRequest request) {
        return Result.success(service.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("knowledge:edit")
    public Result<KnowledgeSpaceService.SpaceView> update(
            @PathVariable Long id,
            @RequestBody @Valid KnowledgeSpaceService.UpdateSpaceRequest request) {
        return Result.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("knowledge:delete")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/members")
    public Result<List<KnowledgeSpaceService.MemberView>> members(@PathVariable Long id) {
        return Result.success(service.members(id));
    }

    @PutMapping("/{id}/members")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replaceMembers(
            @PathVariable Long id,
            @RequestBody @Valid List<KnowledgeSpaceService.MemberRequest> members) {
        service.replaceMembers(id, members);
        return Result.success();
    }

    @PostMapping("/default")
    public Result<KnowledgeSpaceService.SpaceView> ensureDefault() {
        return Result.success(service.ensureDefaultSpace());
    }
}
