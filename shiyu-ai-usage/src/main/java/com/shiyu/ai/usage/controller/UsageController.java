package com.shiyu.ai.usage.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.dal.repository.agent.TokenUsageRepository;
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

    private final TokenUsageRepository usageRepository;

    public UsageController(TokenUsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Operation(summary = "Get Usage Overview")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            return Result.success(usageRepository.getOverview());
        } catch (Exception e) {
            log.error("获取用量概览失败", e);
            return Result.fail("获取用量概览失败");
        }
    }

    @Operation(summary = "Aggregate by Day")
    @GetMapping("/daily")
    public Result<List<Map<String, Object>>> aggregateByDay(
            @RequestParam(defaultValue = "7") int days) {
        try {
            return Result.success(usageRepository.aggregateByDay(days));
        } catch (Exception e) {
            log.error("按日聚合查询失败", e);
            return Result.fail("按日聚合查询失败");
        }
    }

    @Operation(summary = "Aggregate by Week")
    @GetMapping("/weekly")
    public Result<List<Map<String, Object>>> aggregateByWeek(
            @RequestParam(defaultValue = "4") int weeks) {
        try {
            return Result.success(usageRepository.aggregateByWeek(weeks));
        } catch (Exception e) {
            log.error("按周聚合查询失败", e);
            return Result.fail("按周聚合查询失败");
        }
    }

    @Operation(summary = "Aggregate by Month")
    @GetMapping("/monthly")
    public Result<List<Map<String, Object>>> aggregateByMonth(
            @RequestParam(defaultValue = "6") int months) {
        try {
            return Result.success(usageRepository.aggregateByMonth(months));
        } catch (Exception e) {
            log.error("按月聚合查询失败", e);
            return Result.fail("按月聚合查询失败");
        }
    }

    @Operation(summary = "Aggregate by Model")
    @GetMapping("/by-model")
    public Result<List<Map<String, Object>>> aggregateByModel() {
        try {
            return Result.success(usageRepository.aggregateByModel());
        } catch (Exception e) {
            log.error("按模型聚合查询失败", e);
            return Result.fail("按模型聚合查询失败");
        }
    }
}
