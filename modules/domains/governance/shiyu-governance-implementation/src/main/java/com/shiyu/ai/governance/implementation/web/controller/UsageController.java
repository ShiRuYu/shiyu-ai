package com.shiyu.ai.governance.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.governance.implementation.usage.service.UsageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 用量统计 Controller */
@Slf4j
@Tag(name = "Usage", description = "Usage Stats")
@RestController
@RequestMapping("/api/governance/usage")
public class UsageController {

    /**
     * 用量服务，表示当前对象中的对应属性。
     */
    private final UsageService usageService;

    /**
     * {@code UsageController} 创建并初始化当前类型实例。
     *
     * @param usageService 参数值，用于执行当前操作。
     */
    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    /**
     * {@code getOverview} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code aggregateByDay} 执行当前类型定义的业务操作。
     *
     * @param days 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code aggregateByWeek} 执行当前类型定义的业务操作。
     *
     * @param weeks 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code aggregateByMonth} 执行当前类型定义的业务操作。
     *
     * @param months 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code aggregateByModel} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code aggregateLlmByDay} 执行当前类型定义的业务操作。
     *
     * @param days 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code aggregateLlmByWeek} 执行当前类型定义的业务操作。
     *
     * @param weeks 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code aggregateLlmByMonth} 执行当前类型定义的业务操作。
     *
     * @param months 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code getEmbeddingOverview} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
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
