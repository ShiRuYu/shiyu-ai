package com.shiyu.ai.usage.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.agent.repository.UsageRecordRepository;
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
@RequestMapping("/usage")
public class UsageController {

    private final UsageRecordRepository usageRecordRepository;

    public UsageController(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    @Operation(summary = "用量概览（所有类型）")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            return Result.success(usageRecordRepository.getOverview());
        } catch (Exception e) {
            log.error("获取用量概览失败", e);
            return Result.fail("获取用量概览失败");
        }
    }

    @Operation(summary = "按日聚合（所有类型，按 usage_type 分组）")
    @GetMapping("/daily")
    public Result<List<Map<String, Object>>> aggregateByDay(
            @RequestParam(defaultValue = "7") int days) {
        try {
            return Result.success(usageRecordRepository.aggregateByDay(days));
        } catch (Exception e) {
            log.error("按日聚合查询失败", e);
            return Result.fail("按日聚合查询失败");
        }
    }

    @Operation(summary = "按周聚合（所有类型，按 usage_type 分组）")
    @GetMapping("/weekly")
    public Result<List<Map<String, Object>>> aggregateByWeek(
            @RequestParam(defaultValue = "4") int weeks) {
        try {
            return Result.success(usageRecordRepository.aggregateByWeek(weeks));
        } catch (Exception e) {
            log.error("按周聚合查询失败", e);
            return Result.fail("按周聚合查询失败");
        }
    }

    @Operation(summary = "按月聚合（所有类型，按 usage_type 分组）")
    @GetMapping("/monthly")
    public Result<List<Map<String, Object>>> aggregateByMonth(
            @RequestParam(defaultValue = "6") int months) {
        try {
            return Result.success(usageRecordRepository.aggregateByMonth(months));
        } catch (Exception e) {
            log.error("按月聚合查询失败", e);
            return Result.fail("按月聚合查询失败");
        }
    }

    @Operation(summary = "LLM 按模型聚合")
    @GetMapping("/by-model")
    public Result<List<Map<String, Object>>> aggregateByModel() {
        try {
            return Result.success(usageRecordRepository.aggregateByModel());
        } catch (Exception e) {
            log.error("按模型聚合查询失败", e);
            return Result.fail("按模型聚合查询失败");
        }
    }

    @Operation(summary = "LLM 按日聚合（含 token/cost）")
    @GetMapping("/llm/daily")
    public Result<List<Map<String, Object>>> aggregateLlmByDay(
            @RequestParam(defaultValue = "7") int days) {
        try {
            return Result.success(usageRecordRepository.aggregateLlmByDay(days));
        } catch (Exception e) {
            log.error("LLM 按日聚合失败", e);
            return Result.fail("LLM 按日聚合失败");
        }
    }

    @Operation(summary = "LLM 按周聚合（含 token/cost）")
    @GetMapping("/llm/weekly")
    public Result<List<Map<String, Object>>> aggregateLlmByWeek(
            @RequestParam(defaultValue = "4") int weeks) {
        try {
            return Result.success(usageRecordRepository.aggregateLlmByWeek(weeks));
        } catch (Exception e) {
            log.error("LLM 按周聚合失败", e);
            return Result.fail("LLM 按周聚合失败");
        }
    }

    @Operation(summary = "LLM 按月聚合（含 token/cost）")
    @GetMapping("/llm/monthly")
    public Result<List<Map<String, Object>>> aggregateLlmByMonth(
            @RequestParam(defaultValue = "6") int months) {
        try {
            return Result.success(usageRecordRepository.aggregateLlmByMonth(months));
        } catch (Exception e) {
            log.error("LLM 按月聚合失败", e);
            return Result.fail("LLM 按月聚合失败");
        }
    }

    @Operation(summary = "Embedding 用量概览")
    @GetMapping("/embedding/overview")
    public Result<Map<String, Object>> getEmbeddingOverview() {
        try {
            return Result.success(usageRecordRepository.getEmbeddingOverview());
        } catch (Exception e) {
            log.error("获取 Embedding 用量概览失败", e);
            return Result.fail("获取 Embedding 用量概览失败");
        }
    }
}
