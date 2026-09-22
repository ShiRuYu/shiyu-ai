package com.shiyu.ai.model.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.model.implementation.application.service.AiModelService;
import com.shiyu.ai.model.implementation.infrastructure.service.ModelManager;
import com.shiyu.ai.model.implementation.web.request.AiModelRequest;
import com.shiyu.ai.model.implementation.web.response.AiModelResponse;
import com.shiyu.ai.model.implementation.web.response.AiModelVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 AI 模型 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@Tag(name = "Ai Model", description = "Ai Model")
@RestController
@RequestMapping("/api/model/model-configurations")
public class AiModelController {

    /**
     * aiModelService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiModelService aiModelService;
    /**
     * modelManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ModelManager modelManager;

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param aiModelService 用于完成本次业务处理的 aiModelService 参数。
     * @param modelManager 用于完成本次业务处理的 modelManager 参数。
     */
    public AiModelController(AiModelService aiModelService, ModelManager modelManager) {
        this.aiModelService = aiModelService;
        this.modelManager = modelManager;
    }

    /**
     * {@code getPage} 查询并返回当前操作所需的数据。
     *
     * @param platformId 参数值，用于执行当前操作。
     * @param pageNo 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Page")
    @SaCheckPermission("agent:model:list")
    @GetMapping
    public Result<PageData<AiModelVO>> getPage(
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        var result =
                aiModelService.pageResponse(
                        ActorContextHttpAdapter.currentActor(), platformId, pageNo, pageSize);
        var vos =
                com.shiyu.ai.common.foundation.utils.MapstructUtils.convert(
                        result.getRight(), AiModelVO.class);
        return Result.success(new PageData<>(vos, result.getLeft()));
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Id 用于定位目标业务对象的标识。
     */
    @Operation(summary = "Get by Platform Id")
    @SaCheckPermission("agent:model:list")
    @GetMapping("/platform")
    public Result<List<AiModelVO>> getByPlatformId(@RequestParam Long platformId) {
        var list =
                aiModelService.byPlatformResponse(
                        ActorContextHttpAdapter.currentActor(), platformId);
        return Result.success(
                com.shiyu.ai.common.foundation.utils.MapstructUtils.convert(list, AiModelVO.class));
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Code 用于定位或筛选目标业务对象的业务值。
     */
    @Operation(summary = "Get by Platform Code")
    @SaCheckPermission("agent:model:list")
    @GetMapping("/platform/by-code")
    public Result<List<AiModelResponse>> getByPlatformCode(@RequestParam String platformCode) {
        List<AiModelResponse> list =
                aiModelService.byPlatformCodeResponse(
                        ActorContextHttpAdapter.currentActor(), platformCode);
        return Result.success(list);
    }

    /**
     * {@code getOptions} 查询并返回当前操作所需的数据。
     *
     * @param platformId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Options")
    @SaCheckPermission("agent:model:list")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions(
            @RequestParam(required = false) Long platformId) {
        List<IdNameOptionVO> list =
                aiModelService.getOptions(ActorContextHttpAdapter.currentActor(), platformId);
        return Result.success(list);
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Id 用于定位目标业务对象的标识。
     */
    @Operation(summary = "Get by Id")
    @SaCheckPermission("agent:model:list")
    @GetMapping("/{id}")
    public Result<AiModelVO> getById(@PathVariable Long id) {
        AiModelResponse response =
                aiModelService.detailResponse(ActorContextHttpAdapter.currentActor(), id);
        if (response != null) {
            return Result.success(
                    com.shiyu.ai.common.foundation.utils.MapstructUtils.convert(
                            response, AiModelVO.class));
        }
        return Result.fail("模型不存在");
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Id 用于定位目标业务对象的标识。
     */
    @Operation(summary = "Get Default By Platform Id")
    @SaCheckPermission("agent:model:list")
    @GetMapping("/platform/default")
    public Result<AiModelVO> getDefaultByPlatformId(@RequestParam Long platformId) {
        AiModelResponse response =
                aiModelService.defaultResponse(ActorContextHttpAdapter.currentActor(), platformId);
        if (response != null) {
            return Result.success(
                    com.shiyu.ai.common.foundation.utils.MapstructUtils.convert(
                            response, AiModelVO.class));
        }
        return Result.fail("未配置默认模型");
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Create 用于完成本次业务处理的 Create 参数。
     */
    @Operation(summary = "Create")
    @SaCheckPermission("agent:model:create")
    @PostMapping
    public Result<AiModelVO> create(@Valid @RequestBody AiModelRequest request) {
        try {
            AiModelResponse created =
                    aiModelService.createResponse(ActorContextHttpAdapter.currentActor(), request);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.foundation.utils.MapstructUtils.convert(
                            created, AiModelVO.class));
        } catch (Exception e) {
            log.error("新增模型失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Update 用于完成本次业务处理的 Update 参数。
     */
    @Operation(summary = "Update")
    @SaCheckPermission("agent:model:edit")
    @PutMapping("/{id}")
    public Result<AiModelVO> update(
            @PathVariable Long id, @Valid @RequestBody AiModelRequest request) {
        try {
            AiModelResponse updated =
                    aiModelService.updateResponse(
                            ActorContextHttpAdapter.currentActor(), id, request);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.foundation.utils.MapstructUtils.convert(
                            updated, AiModelVO.class));
        } catch (Exception e) {
            log.error("修改模型失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Delete 用于完成本次业务处理的 Delete 参数。
     */
    @Operation(summary = "Delete")
    @SaCheckPermission("agent:model:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            aiModelService.deleteById(ActorContextHttpAdapter.currentActor(), id);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("删除模型失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Batch 用于完成本次业务处理的 Batch 参数。
     */
    @Operation(summary = "Delete Batch")
    @SaCheckPermission("agent:model:delete")
    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        try {
            aiModelService.deleteByIds(ActorContextHttpAdapter.currentActor(), ids);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除模型失败", e);
            return Result.fail("批量删除失败");
        }
    }

    /**
     * 执行 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Default 用于完成本次业务处理的 Default 参数。
     */
    @Operation(summary = "Set Default")
    @SaCheckPermission("agent:model:set-default")
    @PostMapping("/set-default")
    public Result<AiModelVO> setDefault(@RequestParam Long id) {
        try {
            AiModelResponse response =
                    aiModelService.setDefaultResponse(ActorContextHttpAdapter.currentActor(), id);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.foundation.utils.MapstructUtils.convert(
                            response, AiModelVO.class));
        } catch (Exception e) {
            log.error("设置默认模型失败", e);
            return Result.fail("设置失败");
        }
    }
}
