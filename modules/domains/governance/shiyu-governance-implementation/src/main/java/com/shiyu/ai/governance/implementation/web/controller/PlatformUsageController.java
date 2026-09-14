package com.shiyu.ai.governance.implementation.web.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.governance.implementation.usage.service.PlatformUsageService;
import com.shiyu.ai.iam.contract.PlatformUsageAccess;
import com.shiyu.ai.kernel.error.DomainAccessDeniedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 默认租户超级管理员使用的平台全租户用量统计入口。
 */
@Tag(name = "Platform Usage", description = "Platform-wide usage statistics")
@RestController
@RequestMapping("/api/governance/platform/usage")
public final class PlatformUsageController {

    private final PlatformUsageService service;
    private final PlatformUsageAccess access;

    /**
     * 创建平台用量统计控制器。
     *
     * @param service 平台统计服务。
     * @param access IAM 平台统计授权契约。
     */
    public PlatformUsageController(PlatformUsageService service, PlatformUsageAccess access) {
        this.service = service;
        this.access = access;
    }

    @Operation(summary = "平台用量概览")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        authorize();
        return Result.success(service.overview());
    }

    @Operation(summary = "平台按日用量")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/daily")
    public Result<List<Map<String, Object>>> daily(
            @RequestParam(defaultValue = "7") int days) {
        authorize();
        return Result.success(service.byDay(days));
    }

    @Operation(summary = "平台按周用量")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/weekly")
    public Result<List<Map<String, Object>>> weekly(
            @RequestParam(defaultValue = "4") int weeks) {
        authorize();
        return Result.success(service.byWeek(weeks));
    }

    @Operation(summary = "平台按月用量")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/monthly")
    public Result<List<Map<String, Object>>> monthly(
            @RequestParam(defaultValue = "6") int months) {
        authorize();
        return Result.success(service.byMonth(months));
    }

    @Operation(summary = "平台按模型用量")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/by-model")
    public Result<List<Map<String, Object>>> byModel() {
        authorize();
        return Result.success(service.byModel());
    }

    @Operation(summary = "平台 LLM 按日用量")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/llm/daily")
    public Result<List<Map<String, Object>>> llmDaily(
            @RequestParam(defaultValue = "7") int days) {
        authorize();
        return Result.success(service.llmByDay(days));
    }

    @Operation(summary = "平台 LLM 按周用量")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/llm/weekly")
    public Result<List<Map<String, Object>>> llmWeekly(
            @RequestParam(defaultValue = "4") int weeks) {
        authorize();
        return Result.success(service.llmByWeek(weeks));
    }

    @Operation(summary = "平台 LLM 按月用量")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/llm/monthly")
    public Result<List<Map<String, Object>>> llmMonthly(
            @RequestParam(defaultValue = "6") int months) {
        authorize();
        return Result.success(service.llmByMonth(months));
    }

    @Operation(summary = "平台 Embedding 用量概览")
    @SaCheckPermission("platform:usage:read")
    @GetMapping("/embedding/overview")
    public Result<Map<String, Object>> embeddingOverview() {
        authorize();
        return Result.success(service.embeddingOverview());
    }

    private void authorize() {
        if (!access.canReadPlatformUsage(ActorContextHttpAdapter.currentActor())) {
            throw new DomainAccessDeniedException(
                    "PLATFORM_USAGE_DENIED", "platform usage permission is required");
        }
    }
}
