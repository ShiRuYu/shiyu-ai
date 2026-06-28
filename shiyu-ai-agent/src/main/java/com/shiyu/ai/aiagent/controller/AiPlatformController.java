package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.service.AiPlatformService;
import com.shiyu.ai.model.bo.AiPlatformBO;
import com.shiyu.ai.model.vo.IdNameOptionVO;
import com.shiyu.ai.core.langchain4j.Lc4jModelManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 平台管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/ai/platform")
public class AiPlatformController {

    private final AiPlatformService aiPlatformService;
    private final Lc4jModelManager lc4jModelManager;

    public AiPlatformController(AiPlatformService aiPlatformService, Lc4jModelManager lc4jModelManager) {
        this.aiPlatformService = aiPlatformService;
        this.lc4jModelManager = lc4jModelManager;
    }

    /**
     * 平台列表 - 分页
     */
    @GetMapping("/page")
    public Result<PageData<AiPlatformBO>> getPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("获取平台列表，name: {}, code: {}, pageNo: {}, pageSize: {}", name, code, pageNo, pageSize);
        Pair<Long, List<AiPlatformBO>> result = aiPlatformService.getPage(pageNo, pageSize, name, code);
        PageData<AiPlatformBO> pageData = new PageData<>(result.getRight(), result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 查询所有启用的平台
     */
    @GetMapping("/enabled")
    public Result<List<AiPlatformBO>> getAllEnabled() {
        log.info("查询所有启用的平台");
        List<AiPlatformBO> list = aiPlatformService.getAllEnabled();
        return Result.success(list);
    }

    /**
     * 平台下拉选项（id + name）
     */
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions() {
        log.info("查询平台下拉选项");
        List<IdNameOptionVO> list = aiPlatformService.getOptions();
        return Result.success(list);
    }

    /**
     * 根据 ID 查询平台详情
     */
    @GetMapping("/{id}")
    public Result<AiPlatformBO> getById(@PathVariable Long id) {
        log.info("查询平台详情，id: {}", id);
        AiPlatformBO bo = aiPlatformService.getById(id);
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("平台不存在");
    }

    /**
     * 根据编码查询平台
     */
    @GetMapping("/code/{code}")
    public Result<AiPlatformBO> getByCode(@PathVariable String code) {
        log.info("根据编码查询平台，code: {}", code);
        AiPlatformBO bo = aiPlatformService.getByCode(code);
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("平台不存在");
    }

    /**
     * 获取默认平台
     */
    @GetMapping("/default")
    public Result<AiPlatformBO> getDefault() {
        log.info("获取默认平台");
        AiPlatformBO bo = aiPlatformService.getDefault();
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("未配置默认平台");
    }

    /**
     * 新增平台
     */
    @PostMapping
    public Result<AiPlatformBO> create(@Valid @RequestBody AiPlatformBO bo) {
        log.info("新增平台：{}", bo.getName());
        try {
            AiPlatformBO created = aiPlatformService.create(bo);
            lc4jModelManager.markDirty();
            return Result.success(created);
        } catch (Exception e) {
            log.error("新增平台失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改平台
     */
    @PatchMapping("/{id}")
    public Result<AiPlatformBO> update(@PathVariable Long id, @Valid @RequestBody AiPlatformBO bo) {
        log.info("修改平台，id: {}", id);
        try {
            bo.setId(id);
            AiPlatformBO updated = aiPlatformService.update(bo);
            lc4jModelManager.markDirty();
            return Result.success(updated);
        } catch (Exception e) {
            log.error("修改平台失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除平台
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除平台，id: {}", id);
        try {
            aiPlatformService.deleteById(id);
            lc4jModelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("删除平台失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * 设置为默认平台
     */
    @PutMapping("/{id}/default")
    public Result<AiPlatformBO> setDefault(@PathVariable Long id) {
        log.info("设置默认平台，id: {}", id);
        try {
            AiPlatformBO bo = aiPlatformService.setDefault(id);
            lc4jModelManager.markDirty();
            return Result.success(bo);
        } catch (Exception e) {
            log.error("设置默认平台失败", e);
            return Result.fail("设置失败");
        }
    }

    /**
     * 手动重新加载所有平台适配器（从数据库刷新）
     */
    @PostMapping("/reload")
    public Result<Void> reload() {
        log.info("手动触发平台适配器重新加载");
        try {
            lc4jModelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("重新加载失败", e);
            return Result.fail("重新加载失败");
        }
    }
}
