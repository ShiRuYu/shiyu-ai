package com.shiyu.ai.model.controller;

import com.shiyu.ai.model.service.AiModelService;
import com.shiyu.ai.model.vo.AiModelVO;
import com.shiyu.ai.dal.model.bo.AiModelBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * AI 模型管理 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Ai Model", description = "Ai Model")
@RestController
@RequestMapping("/agent/model")
public class AiModelController {

    private final AiModelService aiModelService;
    private final ModelManager modelManager;

    public AiModelController(AiModelService aiModelService, ModelManager modelManager) {
        this.aiModelService = aiModelService;
        this.modelManager = modelManager;
    }

    @Operation(summary = "Get Page")
    @GetMapping("/page")
    public Result<PageData<AiModelVO>> getPage(
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        var result = aiModelService.getPage(platformId, pageNo, pageSize);
        var vos = com.shiyu.ai.common.core.utils.MapstructUtils.convert(result.getRight(), AiModelVO.class);
        return Result.success(new PageData<>(vos, result.getLeft()));
    }

    @Operation(summary = "Get by Platform Id")
    @GetMapping("/platform")
    public Result<List<AiModelVO>> getByPlatformId(@RequestParam Long platformId) {
        var list = aiModelService.getByPlatformId(platformId);
        return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(list, AiModelVO.class));
    }

    @Operation(summary = "Get by Platform Code")
    @GetMapping("/platform/by-code")
    public Result<List<AiModelBO>> getByPlatformCode(@RequestParam String platformCode) {
        List<AiModelBO> list = aiModelService.getByPlatformCode(platformCode);
        return Result.success(list);
    }

    @Operation(summary = "Get Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions(
            @RequestParam(required = false) Long platformId) {
        List<IdNameOptionVO> list = aiModelService.getOptions(platformId);
        return Result.success(list);
    }

    @Operation(summary = "Get by Id")
    @GetMapping("/detail")
    public Result<AiModelVO> getById(@RequestParam Long id) {
        AiModelBO bo = aiModelService.getById(id);
        if (bo != null) {
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(bo, AiModelVO.class));
        }
        return Result.fail("模型不存在");
    }

    @Operation(summary = "Get Default By Platform Id")
    @GetMapping("/platform/default")
    public Result<AiModelVO> getDefaultByPlatformId(@RequestParam Long platformId) {
        AiModelBO bo = aiModelService.getDefaultByPlatformId(platformId);
        if (bo != null) {
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(bo, AiModelVO.class));
        }
        return Result.fail("未配置默认模型");
    }

    @Operation(summary = "Create")
    @PostMapping("/create")
    public Result<AiModelVO> create(@Valid @RequestBody AiModelBO bo) {
        try {
            AiModelBO created = aiModelService.create(bo);
            modelManager.markDirty();
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(created, AiModelVO.class));
        } catch (Exception e) {
            log.error("新增模型失败", e);
            return Result.fail("新增失败");
        }
    }

    @Operation(summary = "Update")
    @PostMapping("/update")
    public Result<AiModelVO> update(@RequestParam Long id, @Valid @RequestBody AiModelBO bo) {
        try {
            bo.setId(id);
            AiModelBO updated = aiModelService.update(bo);
            modelManager.markDirty();
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(updated, AiModelVO.class));
        } catch (Exception e) {
            log.error("修改模型失败", e);
            return Result.fail("修改失败");
        }
    }

    @Operation(summary = "Delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        try {
            aiModelService.deleteById(id);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("删除模型失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Delete Batch")
    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        try {
            aiModelService.deleteByIds(ids);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除模型失败", e);
            return Result.fail("批量删除失败");
        }
    }

    @Operation(summary = "Set Default")
    @PostMapping("/set-default")
    public Result<AiModelVO> setDefault(@RequestParam Long id) {
        try {
            AiModelBO bo = aiModelService.setDefault(id);
            modelManager.markDirty();
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(bo, AiModelVO.class));
        } catch (Exception e) {
            log.error("设置默认模型失败", e);
            return Result.fail("设置失败");
        }
    }
}
