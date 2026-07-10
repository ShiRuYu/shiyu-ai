package com.shiyu.ai.agent.controller;

import com.shiyu.ai.agent.service.AiPlatformService;
import com.shiyu.ai.dal.bo.model.AiPlatformBO;
import com.shiyu.ai.model.vo.IdNameOptionVO;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * AI 平台管理 Controller
 */
@Slf4j
@Tag(name = "Ai Platform", description = "Ai Platform")
@RestController
@RequestMapping("/admin/platform")
public class AiPlatformController {

    private final AiPlatformService aiPlatformService;
    private final ModelManager modelManager;

    public AiPlatformController(AiPlatformService aiPlatformService, ModelManager modelManager) {
        this.aiPlatformService = aiPlatformService;
        this.modelManager = modelManager;
    }

    /**
     * 平台列表 - 分页
     */
    @Operation(summary = "Get Page")
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
    @Operation(summary = "Get All Enabled")
    @GetMapping("/enabled")
    public Result<List<AiPlatformBO>> getAllEnabled() {
        log.info("查询所有启用的平台");
        List<AiPlatformBO> list = aiPlatformService.getAllEnabled();
        return Result.success(list);
    }

    /**
     * 平台下拉选项（id + name）
     */
    @Operation(summary = "Get Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions() {
        log.info("查询平台下拉选项");
        List<IdNameOptionVO> list = aiPlatformService.getOptions();
        return Result.success(list);
    }

    /**
     * 根据 ID 查询平台详情
     */
    @Operation(summary = "Get by Id")
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
    @Operation(summary = "Get by Code")
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
    @Operation(summary = "Get Default")
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
    @Operation(summary = "Create")
    @PostMapping("/create")
    public Result<AiPlatformBO> create(@Valid @RequestBody AiPlatformBO bo) {
        log.info("新增平台：{}", bo.getName());
        try {
            AiPlatformBO created = aiPlatformService.create(bo);
            modelManager.markDirty();
            return Result.success(created);
        } catch (Exception e) {
            log.error("新增平台失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改平台
     */
    @Operation(summary = "Update")
    @PostMapping("/update")
    public Result<AiPlatformBO> update(@RequestParam Long id, @Valid @RequestBody AiPlatformBO bo) {
        log.info("修改平台，id: {}", id);
        try {
            bo.setId(id);
            AiPlatformBO updated = aiPlatformService.update(bo);
            modelManager.markDirty();
            return Result.success(updated);
        } catch (Exception e) {
            log.error("修改平台失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除平台
     */
    @Operation(summary = "Delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        log.info("删除平台，id: {}", id);
        try {
            aiPlatformService.deleteById(id);
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("删除平台失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * 设置为默认平台
     */
    @Operation(summary = "Set Default")
    @PostMapping("/set-default")
    public Result<AiPlatformBO> setDefault(@RequestParam Long id) {
        log.info("设置默认平台，id: {}", id);
        try {
            AiPlatformBO bo = aiPlatformService.setDefault(id);
            modelManager.markDirty();
            return Result.success(bo);
        } catch (Exception e) {
            log.error("设置默认平台失败", e);
            return Result.fail("设置失败");
        }
    }

    /**
     * 手动重新加载所有平台适配器（从数据库刷新）
     */
    @Operation(summary = "Reload")
    @PostMapping("/reload")
    public Result<Void> reload() {
        log.info("手动触发平台适配器重新加载");
        try {
            modelManager.markDirty();
            return Result.success();
        } catch (Exception e) {
            log.error("重新加载失败", e);
            return Result.fail("重新加载失败");
        }
    }
}
