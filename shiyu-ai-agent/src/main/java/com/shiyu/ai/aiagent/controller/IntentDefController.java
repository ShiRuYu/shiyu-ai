package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.service.IntentDefService;
import com.shiyu.ai.aiagent.bo.IntentDefBO;
import com.shiyu.ai.model.vo.IdNameOptionVO;
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
 * 意图定义管理 Controller
 */
@Slf4j
@Tag(name = "Intent Def", description = "Intent Def")
@RestController
@RequestMapping("/admin/intent")
public class IntentDefController {

    private final IntentDefService intentDefService;

    public IntentDefController(IntentDefService intentDefService) {
        this.intentDefService = intentDefService;
    }

    /**
     * 意图定义列表 - 分页（可按 agentId 或 category 过滤）
     */
    @Operation(summary = "Get Page")
    @GetMapping("/page")
    public Result<PageData<IntentDefBO>> getPage(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("获取意图定义列表，agentId: {}, name: {}, code: {}, category: {}, pageNo: {}, pageSize: {}",
                agentId, name, code, category, pageNo, pageSize);
        Pair<Long, List<IntentDefBO>> result = intentDefService.getPage(pageNo, pageSize, agentId, name, code, category);
        PageData<IntentDefBO> pageData = new PageData<>(result.getRight(), result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 根据 ID 查询意图定义详情
     */
    @Operation(summary = "Get by Id")
    @GetMapping("/{id}")
    public Result<IntentDefBO> getById(@PathVariable Long id) {
        log.info("查询意图定义详情，id: {}", id);
        IntentDefBO bo = intentDefService.getById(id);
        if (bo != null) {
            return Result.success(bo);
        }
        return Result.fail("意图定义不存在");
    }

    /**
     * 新增意图定义
     */
    @Operation(summary = "Create")
    @PostMapping("/create")
    public Result<IntentDefBO> create(@Valid @RequestBody IntentDefBO bo) {
        log.info("新增意图定义，code: {}", bo.getCode());
        try {
            IntentDefBO created = intentDefService.create(bo);
            return Result.success(created);
        } catch (Exception e) {
            log.error("新增意图定义失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改意图定义
     */
    @Operation(summary = "Update")
    @PostMapping("/update")
    public Result<IntentDefBO> update(@RequestParam Long id, @Valid @RequestBody IntentDefBO bo) {
        log.info("修改意图定义，id: {}", id);
        try {
            bo.setId(id);
            IntentDefBO updated = intentDefService.update(bo);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("修改意图定义失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除意图定义
     */
    @Operation(summary = "Delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        log.info("删除意图定义，id: {}", id);
        try {
            intentDefService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除意图定义失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * 批量删除意图定义
     */
    @Operation(summary = "Delete Batch")
    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除意图定义，ids: {}", ids);
        try {
            intentDefService.deleteByIds(ids);
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除意图定义失败", e);
            return Result.fail("批量删除失败");
        }
    }

    /**
     * 获取所有意图定义选项（下拉选择用）
     */
    @Operation(summary = "Get Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> getOptions() {
        return Result.success(intentDefService.listAllOptions());
    }
}
