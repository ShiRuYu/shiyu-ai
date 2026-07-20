package com.shiyu.ai.model.controller;

import com.shiyu.ai.model.service.AiModelService;
import com.shiyu.ai.model.vo.AiModelVO;
import com.shiyu.ai.dal.model.bo.AiModelBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * AI 模型管理 Controller
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

    /**
     * 模型列表 - 分页（可按平台过滤）
     */
    @Operation(summary = "Get Page")
    @GetMapping("/page")
    public Result<PageData<AiModelVO>> getPage(
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("获取模型列表，platformId: {}, pageNo: {}, pageSize: {}", platformId, pageNo, pageSize);
        var result = aiModelService.getPage(platformId, pageNo, pageSize);
        var vos = com.shiyu.ai.common.core.utils.MapstructUtils.convert(result.getRight(), AiModelVO.class);
        PageData<AiModelVO> pageData = new PageData<>(vos, result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 查询指定平台下所有启用的模型
     */
    @Operation(summary = "Get by Platform Id")
    @GetMapping("/platform/{platformId}")
    public Result<List<AiModelVO>> getByPlatformId(@PathVariable Long platformId) {
        log.info("查询平台下的模型，platformId: {}", platformId);
        var list = aiModelService.getByPlatformId(platformId);
        return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(list, AiModelVO.class));
    }

    /**
     * 根据平台编码查询所有启用的模型（前端级联下拉使用）
     */
    @Operation(summary = "Get by Platform Code")
    @GetMapping("/platform/by-code/{platformCode}")
    public Result<List<AiModelBO>> getByPlatformCode(@PathVariable String platformCode) {
        log.info("查询平台编码下的模型，platformCode: {}", platformCode);
        List<AiModelBO> list = aiModelService.getByPlatformCode(platformCode);
        return Result.success(list);
    }

    /**
     * 模型下拉选项（id + name），可按平台过滤
     */
    @Operation(summary = "Get Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions(
            @RequestParam(required = false) Long platformId) {
        log.info("查询模型下拉选项，platformId: {}", platformId);
        List<IdNameOptionVO> list = aiModelService.getOptions(platformId);
        return Result.success(list);
    }

    /**
     * 根据 ID 查询模型详情
     */
    @Operation(summary = "Get by Id")
    @GetMapping("/{id}")
    public Result<AiModelVO> getById(@PathVariable Long id) {
        log.info("查询模型详情，id: {}", id);
        AiModelBO bo = aiModelService.getById(id);
        if (bo != null) {
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(bo, AiModelVO.class));
        }
        return Result.fail("模型不存在");
    }

    /**
     * 获取平台的默认模型
     */
    @Operation(summary = "Get Default By Platform Id")
    @GetMapping("/platform/{platformId}/default")
    public Result<AiModelVO> getDefaultByPlatformId(@PathVariable Long platformId) {
        log.info("获取平台默认模型，platformId: {}", platformId);
        AiModelBO bo = aiModelService.getDefaultByPlatformId(platformId);
        if (bo != null) {
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(bo, AiModelVO.class));
        }
        return Result.fail("未配置默认模型");
    }

    /**
     * 新增模型
     */
    @Operation(summary = "Create")
    @PostMapping("/create")
    public Result<AiModelVO> create(@Valid @RequestBody AiModelBO bo) {
        log.info("新增模型：{}", bo.getModelName());
        try {
            AiModelBO created = aiModelService.create(bo);
            modelManager.markDirty();
            return Result.success(com.shiyu.ai.common.core.utils.MapstructUtils.convert(created, AiModelVO.class));
        } catch (Exception e) {
            log.error("新增模型失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改模型
     */
    @Operation(summary = "Update")
    @PostMapping("/update")
    public Result<AiModelVO> update(@RequestParam Long id, @Valid @RequestBody AiModelBO bo) {
        log.info("修改模型，id: {}", id);
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

    /**
     * 删除模型
     */
    @Operation(summary = "Delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        log.info("删除模型，id: {}", id);
        try {
            aiModelService.deleteById(id);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("删除模型失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * 批量删除模型
     */
    @Operation(summary = "Delete Batch")
    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除模型，ids: {}", ids);
        try {
            aiModelService.deleteByIds(ids);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除模型失败", e);
            return Result.fail("批量删除失败");
        }
    }

    /**
     * 设置为平台默认模型
     */
    @Operation(summary = "Set Default")
    @PostMapping("/set-default")
    public Result<AiModelVO> setDefault(@RequestParam Long id) {
        log.info("设置默认模型，id: {}", id);
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
