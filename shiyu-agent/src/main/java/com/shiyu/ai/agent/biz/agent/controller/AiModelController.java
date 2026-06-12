package com.shiyu.ai.agent.biz.agent.controller;

import com.shiyu.ai.agent.biz.agent.service.AiModelService;
import com.shiyu.ai.agent.domain.bo.AiModelBO;
import com.shiyu.ai.agent.domain.vo.IdNameOptionVO;
import com.shiyu.ai.agent.langchain4j.Lc4jModelManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 模型管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/ai/model")
public class AiModelController {

    private final AiModelService aiModelService;
    private final Lc4jModelManager lc4jModelManager;

    public AiModelController(AiModelService aiModelService, Lc4jModelManager lc4jModelManager) {
        this.aiModelService = aiModelService;
        this.lc4jModelManager = lc4jModelManager;
    }

    /**
     * 模型列表 - 分页（可按平台过滤）
     */
    @GetMapping("/page")
    public Result<PageData<AiModelBO>> getPage(
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("获取模型列表，platformId: {}, pageNo: {}, pageSize: {}", platformId, pageNo, pageSize);
        Pair<Long, List<AiModelBO>> result = aiModelService.getPage(platformId, pageNo, pageSize);
        PageData<AiModelBO> pageData = new PageData<>(result.getRight(), result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 查询指定平台下所有启用的模型
     */
    @GetMapping("/platform/{platformId}")
    public Result<List<AiModelBO>> getByPlatformId(@PathVariable Long platformId) {
        log.info("查询平台下的模型，platformId: {}", platformId);
        List<AiModelBO> list = aiModelService.getByPlatformId(platformId);
        return Result.success(list);
    }

    /**
     * 根据平台编码查询所有启用的模型（前端级联下拉使用）
     */
    @GetMapping("/platform/by-code/{platformCode}")
    public Result<List<AiModelBO>> getByPlatformCode(@PathVariable String platformCode) {
        log.info("查询平台编码下的模型，platformCode: {}", platformCode);
        List<AiModelBO> list = aiModelService.getByPlatformCode(platformCode);
        return Result.success(list);
    }

    /**
     * 模型下拉选项（id + name），可按平台过滤
     */
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
    @GetMapping("/{id}")
    public Result<AiModelBO> getById(@PathVariable Long id) {
        log.info("查询模型详情，id: {}", id);
        AiModelBO bo = aiModelService.getById(id);
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("模型不存在");
    }

    /**
     * 获取平台的默认模型
     */
    @GetMapping("/platform/{platformId}/default")
    public Result<AiModelBO> getDefaultByPlatformId(@PathVariable Long platformId) {
        log.info("获取平台默认模型，platformId: {}", platformId);
        AiModelBO bo = aiModelService.getDefaultByPlatformId(platformId);
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("未配置默认模型");
    }

    /**
     * 新增模型
     */
    @PostMapping
    public Result<AiModelBO> create(@RequestBody AiModelBO bo) {
        log.info("新增模型：{}", bo.getModelName());
        try {
            AiModelBO created = aiModelService.create(bo);
            lc4jModelManager.reloadFromDb();
            return Result.success(created);
        } catch (Exception e) {
            log.error("新增模型失败", e);
            return Result.fail("新增失败：" + e.getMessage());
        }
    }

    /**
     * 修改模型
     */
    @PatchMapping("/{id}")
    public Result<AiModelBO> update(@PathVariable Long id, @RequestBody AiModelBO bo) {
        log.info("修改模型，id: {}", id);
        try {
            bo.setId(id);
            AiModelBO updated = aiModelService.update(bo);
            lc4jModelManager.reloadFromDb();
            return Result.success(updated);
        } catch (Exception e) {
            log.error("修改模型失败", e);
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除模型
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除模型，id: {}", id);
        try {
            aiModelService.deleteById(id);
            lc4jModelManager.reloadFromDb();
            return Result.success();
        } catch (Exception e) {
            log.error("删除模型失败", e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除模型
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除模型，ids: {}", ids);
        try {
            aiModelService.deleteByIds(ids);
            lc4jModelManager.reloadFromDb();
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除模型失败", e);
            return Result.fail("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 设置为平台默认模型
     */
    @PutMapping("/{id}/default")
    public Result<AiModelBO> setDefault(@PathVariable Long id) {
        log.info("设置默认模型，id: {}", id);
        try {
            AiModelBO bo = aiModelService.setDefault(id);
            lc4jModelManager.reloadFromDb();
            return Result.success(bo);
        } catch (Exception e) {
            log.error("设置默认模型失败", e);
            return Result.fail("设置失败：" + e.getMessage());
        }
    }
}
