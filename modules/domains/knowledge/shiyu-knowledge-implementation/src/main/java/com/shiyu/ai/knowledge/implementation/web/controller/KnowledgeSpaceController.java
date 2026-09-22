package com.shiyu.ai.knowledge.implementation.web.controller;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.CreateSpaceRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.DifficultyScaleView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.MemberRequest;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.MemberView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.UpdateSpaceRequest;
import com.shiyu.ai.knowledge.implementation.web.api.KnowledgeApiVersion;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
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
 * 处理 知识 空间 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/knowledge/spaces")
@RequiredArgsConstructor
@Tag(name = "知识空间")
public class KnowledgeSpaceController {

    /**
     * 服务，表示当前对象中的对应属性。
     */
    private final KnowledgeSpaceService service;

    /**
     * 查询 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param domainCode 用于完成本次业务处理的 domainCode 参数。
     * @param version 用于完成本次业务处理的 version 参数。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:list")
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
     * 查询 知识 空间 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param options 用于完成本次业务处理的 options 参数。
     */
    @SaCheckPermission("knowledge:list")
    @GetMapping("/options")
    public Result<List<KnowledgeSpaceService.SpaceView>> options() {
        return Result.success(service.accessibleSpaces(currentActor()));
    }

    /**
     * 查询 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:list")
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
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param scale 用于完成本次业务处理的 scale 参数。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:list")
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
     * 创建或保存 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param create 用于完成本次业务处理的 create 参数。
     * @return 返回 知识 空间 相关操作生成的结果数据。
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
     * 更新或设置 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 空间 相关操作生成的结果数据。
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
     * 删除或移除 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 空间 相关操作生成的结果数据。
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
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param members 用于完成本次业务处理的 members 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @SaCheckPermission("knowledge:list")
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
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param members 用于完成本次业务处理的 members 参数。
     * @return 返回 知识 空间 相关操作生成的结果数据。
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
     * 执行 知识 空间 相关业务数据，并返回处理结果。
     *
     * @param default 用于完成本次业务处理的 default 参数。
     * @return 返回 知识 空间 相关操作生成的结果数据。
     */
    @SaCheckPermission("knowledge:create")
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
