package com.shiyu.ai.web.agent.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.agent.service.IntentDefService;
import com.shiyu.ai.agent.vo.IntentDefVO;
import com.shiyu.ai.dal.agent.bo.IntentDefBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
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
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Intent Def", description = "Intent Def")
@SaCheckPermission("agent:intent:list")
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
    public Result<PageData<IntentDefVO>> getPage(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("获取意图定义列表，agentId: {}, name: {}, code: {}, category: {}, pageNo: {}, pageSize: {}",
                agentId, name, code, category, pageNo, pageSize);
        Pair<Long, List<IntentDefBO>> result = intentDefService.getPage(pageNo, pageSize, agentId, name, code, category);
        java.util.List<IntentDefVO> vos = com.shiyu.ai.agent.service.convert.IntentDefConverter.INSTANCE.toVOList(result.getRight());
        PageData<IntentDefVO> pageData = new PageData<>(vos, result.getLeft());
        return Result.success(pageData);
    }

    /**
     * 根据 ID 查询意图定义详情
     */
    @Operation(summary = "Get by Id")
    @GetMapping("/detail")
    public Result<IntentDefVO> getById(@RequestParam Long id) {
        log.info("查询意图定义详情，id: {}", id);
        IntentDefBO bo = intentDefService.getById(id);
        if (bo != null) {
            return Result.success(com.shiyu.ai.agent.service.convert.IntentDefConverter.INSTANCE.toVO(bo));
        }
        return Result.fail("意图定义不存在");
    }

    /**
     * 新增意图定义
     */
    @Operation(summary = "Create")
    @SaCheckPermission("agent:intent:create")
    @PostMapping("/create")
    public Result<IntentDefVO> create(@Valid @RequestBody IntentDefBO bo) {
        log.info("新增意图定义，code: {}", bo.getCode());
        try {
            IntentDefBO created = intentDefService.create(bo);
            return Result.success(com.shiyu.ai.agent.service.convert.IntentDefConverter.INSTANCE.toVO(created));
        } catch (Exception e) {
            log.error("新增意图定义失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改意图定义
     */
    @Operation(summary = "Update")
    @SaCheckPermission("agent:intent:create")
    @PostMapping("/update")
    public Result<IntentDefVO> update(@RequestParam Long id, @Valid @RequestBody IntentDefBO bo) {
        log.info("修改意图定义，id: {}", id);
        try {
            bo.setId(id);
            IntentDefBO updated = intentDefService.update(bo);
            return Result.success(com.shiyu.ai.agent.service.convert.IntentDefConverter.INSTANCE.toVO(updated));
        } catch (Exception e) {
            log.error("修改意图定义失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除意图定义
     */
    @Operation(summary = "Delete")
    @SaCheckPermission("agent:intent:delete")
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
    @SaCheckPermission("agent:intent:delete")
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
