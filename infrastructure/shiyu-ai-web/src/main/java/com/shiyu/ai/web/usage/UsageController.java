package com.shiyu.ai.web.usage;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.usage.service.UsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用量统计 Controller
 */
@Slf4j
@Tag(name = "Usage", description = "Usage Stats")
@RestController
@RequestMapping("/v1/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @Operation(summary = "用量概览（所有类型）")
    @SaCheckPermission("usage:overview")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            return Result.success(usageService.overview());
        } catch (Exception e) {
            log.error("获取用量概览失败", e);
            return Result.fail("获取用量概览失败");
        }
    }

    @Operation(summary = "按日聚合（所有类型，按 usage_type 分组）")
    @SaCheckPermission("usage:daily")
    @GetMapping("/daily")
    public Result<List<Map<String, Object>>> aggregateByDay(
            @RequestParam(defaultValue = "7") int days) {
        try {
            return Result.success(usageService.byDay(days));
        } catch (Exception e) {
            log.error("按日聚合查询失败", e);
            return Result.fail("按日聚合查询失败");
        }
    }

    @Operation(summary = "按周聚合（所有类型，按 usage_type 分组）")
    @SaCheckPermission("usage:weekly")
    @GetMapping("/weekly")
    public Result<List<Map<String, Object>>> aggregateByWeek(
            @RequestParam(defaultValue = "4") int weeks) {
        try {
            return Result.success(usageService.byWeek(weeks));
        } catch (Exception e) {
            log.error("按周聚合查询失败", e);
            return Result.fail("按周聚合查询失败");
        }
    }

    @Operation(summary = "按月聚合（所有类型，按 usage_type 分组）")
    @SaCheckPermission("usage:monthly")
    @GetMapping("/monthly")
    public Result<List<Map<String, Object>>> aggregateByMonth(
            @RequestParam(defaultValue = "6") int months) {
        try {
            return Result.success(usageService.byMonth(months));
        } catch (Exception e) {
            log.error("按月聚合查询失败", e);
            return Result.fail("按月聚合查询失败");
        }
    }

    @Operation(summary = "LLM 按模型聚合")
    @SaCheckPermission("usage:model")
    @GetMapping("/by-model")
    public Result<List<Map<String, Object>>> aggregateByModel() {
        try {
            return Result.success(usageService.byModel());
        } catch (Exception e) {
            log.error("按模型聚合查询失败", e);
            return Result.fail("按模型聚合查询失败");
        }
    }

    @Operation(summary = "LLM 按日聚合（含 token/cost）")
    @SaCheckPermission("usage:llm")
    @GetMapping("/llm/daily")
    public Result<List<Map<String, Object>>> aggregateLlmByDay(
            @RequestParam(defaultValue = "7") int days) {
        try {
            return Result.success(usageService.llmByDay(days));
        } catch (Exception e) {
            log.error("LLM 按日聚合失败", e);
            return Result.fail("LLM 按日聚合失败");
        }
    }

    @Operation(summary = "LLM 按周聚合（含 token/cost）")
    @SaCheckPermission("usage:llm")
    @GetMapping("/llm/weekly")
    public Result<List<Map<String, Object>>> aggregateLlmByWeek(
            @RequestParam(defaultValue = "4") int weeks) {
        try {
            return Result.success(usageService.llmByWeek(weeks));
        } catch (Exception e) {
            log.error("LLM 按周聚合失败", e);
            return Result.fail("LLM 按周聚合失败");
        }
    }

    @Operation(summary = "LLM 按月聚合（含 token/cost）")
    @SaCheckPermission("usage:llm")
    @GetMapping("/llm/monthly")
    public Result<List<Map<String, Object>>> aggregateLlmByMonth(
            @RequestParam(defaultValue = "6") int months) {
        try {
            return Result.success(usageService.llmByMonth(months));
        } catch (Exception e) {
            log.error("LLM 按月聚合失败", e);
            return Result.fail("LLM 按月聚合失败");
        }
    }

    @Operation(summary = "Embedding 用量概览")
    @SaCheckPermission("usage:embedding")
    @GetMapping("/embedding/overview")
    public Result<Map<String, Object>> getEmbeddingOverview() {
        try {
            return Result.success(usageService.embeddingOverview());
        } catch (Exception e) {
            log.error("获取 Embedding 用量概览失败", e);
            return Result.fail("获取 Embedding 用量概览失败");
        }
    }
}
