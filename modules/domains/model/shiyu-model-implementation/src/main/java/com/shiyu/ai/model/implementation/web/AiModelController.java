package com.shiyu.ai.model.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.model.implementation.application.service.AiModelService;
import com.shiyu.ai.model.implementation.infrastructure.adapter.ModelManager;
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
 * AI 模型管理 Controller
 *
 * <p>注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Ai Model", description = "Ai Model")
@SaCheckPermission("agent:model:list")
@RestController
@RequestMapping("/api/model/models")
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
     * {@code AiModelController} 创建并初始化当前类型实例。
     *
     * @param aiModelService 参数值，用于执行当前操作。
     * @param modelManager 参数值，用于执行当前操作。
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
    @GetMapping("/page")
    public Result<PageData<AiModelVO>> getPage(
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        var result =
                aiModelService.pageResponse(
                        ActorContextHttpAdapter.currentActor(), platformId, pageNo, pageSize);
        var vos =
                com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                        result.getRight(), AiModelVO.class);
        return Result.success(new PageData<>(vos, result.getLeft()));
    }

    /**
     * {@code getByPlatformId} 查询并返回当前操作所需的数据。
     *
     * @param platformId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get by Platform Id")
    @GetMapping("/platform")
    public Result<List<AiModelVO>> getByPlatformId(@RequestParam Long platformId) {
        var list =
                aiModelService.byPlatformResponse(
                        ActorContextHttpAdapter.currentActor(), platformId);
        return Result.success(
                com.shiyu.ai.common.core.utils.MapstructUtils.convert(list, AiModelVO.class));
    }

    /**
     * {@code getByPlatformCode} 查询并返回当前操作所需的数据。
     *
     * @param platformCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get by Platform Code")
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
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions(
            @RequestParam(required = false) Long platformId) {
        List<IdNameOptionVO> list =
                aiModelService.getOptions(ActorContextHttpAdapter.currentActor(), platformId);
        return Result.success(list);
    }

    /**
     * {@code getById} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get by Id")
    @GetMapping("/detail")
    public Result<AiModelVO> getById(@RequestParam Long id) {
        AiModelResponse response =
                aiModelService.detailResponse(ActorContextHttpAdapter.currentActor(), id);
        if (response != null) {
            return Result.success(
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            response, AiModelVO.class));
        }
        return Result.fail("模型不存在");
    }

    /**
     * {@code getDefaultByPlatformId} 查询并返回当前操作所需的数据。
     *
     * @param platformId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Default By Platform Id")
    @GetMapping("/platform/default")
    public Result<AiModelVO> getDefaultByPlatformId(@RequestParam Long platformId) {
        AiModelResponse response =
                aiModelService.defaultResponse(ActorContextHttpAdapter.currentActor(), platformId);
        if (response != null) {
            return Result.success(
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            response, AiModelVO.class));
        }
        return Result.fail("未配置默认模型");
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Create")
    @SaCheckPermission("agent:model:create")
    @PostMapping("/create")
    public Result<AiModelVO> create(@Valid @RequestBody AiModelRequest request) {
        try {
            AiModelResponse created =
                    aiModelService.createResponse(ActorContextHttpAdapter.currentActor(), request);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            created, AiModelVO.class));
        } catch (Exception e) {
            log.error("新增模型失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Update")
    @SaCheckPermission("agent:model:edit")
    @PostMapping("/update")
    public Result<AiModelVO> update(
            @RequestParam Long id, @Valid @RequestBody AiModelRequest request) {
        try {
            AiModelResponse updated =
                    aiModelService.updateResponse(
                            ActorContextHttpAdapter.currentActor(), id, request);
            modelManager.markDirty();
            return Result.success(
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            updated, AiModelVO.class));
        } catch (Exception e) {
            log.error("修改模型失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Delete")
    @SaCheckPermission("agent:model:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
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
     * {@code deleteBatch} 释放或移除当前操作涉及的资源。
     *
     * @param ids 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code setDefault} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
                    com.shiyu.ai.common.core.utils.MapstructUtils.convert(
                            response, AiModelVO.class));
        } catch (Exception e) {
            log.error("设置默认模型失败", e);
            return Result.fail("设置失败");
        }
    }
}
