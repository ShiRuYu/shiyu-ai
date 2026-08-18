package com.shiyu.ai.web.knowledge;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.knowledge.security.KnowledgeAccessContext;
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

@RestController
@RequestMapping("/v1/knowledge/spaces")
@RequiredArgsConstructor
@Tag(name = "知识空间")
@SaCheckPermission("knowledge:list")
public class KnowledgeSpaceController {

    private final KnowledgeSpaceService service;

    @GetMapping
    public Result<PageData<KnowledgeSpaceService.SpaceView>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String domainCode,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.page(pageNum, Math.min(pageSize, 100), keyword, domainCode));
    }

    @GetMapping("/options")
    public Result<List<KnowledgeSpaceService.SpaceView>> options() {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        if (tenantId == null) {
            return Result.success(List.of());
        }
        KnowledgeAccessContext context = new KnowledgeAccessContext(
                tenantId, UserContextHolder.getUserId(), UserContextHolder.getCurrentRoleId(),
                UserContextHolder.isSuperAdmin());
        return Result.success(service.accessibleSpaces(context));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeSpaceService.SpaceView> get(@PathVariable Long id,
                                                        @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.get(id));
    }

    @GetMapping("/{id}/difficulty-scale")
    public Result<KnowledgeSpaceService.DifficultyScaleView> difficultyScale(
            @PathVariable Long id,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.difficultyScale(id));
    }

    @PostMapping
    @SaCheckPermission("knowledge:create")
    public Result<KnowledgeSpaceService.SpaceView> create(
            @RequestBody @Valid KnowledgeSpaceService.CreateSpaceRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("knowledge:edit")
    public Result<KnowledgeSpaceService.SpaceView> update(
            @PathVariable Long id,
            @RequestBody @Valid KnowledgeSpaceService.UpdateSpaceRequest request,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("knowledge:delete")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                       defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/members")
    public Result<List<KnowledgeSpaceService.MemberView>> members(@PathVariable Long id,
                                                                   @RequestHeader(value = KnowledgeApiVersion.HEADER,
                                                                           defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.members(id));
    }

    @PutMapping("/{id}/members")
    @SaCheckPermission("knowledge:edit")
    public Result<Void> replaceMembers(
            @PathVariable Long id,
            @RequestBody @Valid List<KnowledgeSpaceService.MemberRequest> members,
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        service.replaceMembers(id, members);
        return Result.success();
    }

    @PostMapping("/default")
    public Result<KnowledgeSpaceService.SpaceView> ensureDefault(
            @RequestHeader(value = KnowledgeApiVersion.HEADER,
                    defaultValue = KnowledgeApiVersion.CURRENT) String version) {
        KnowledgeApiVersion.requireCurrent(version);
        return Result.success(service.ensureDefaultSpace());
    }
}
