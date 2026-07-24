package com.shiyu.ai.web.agent.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.runtime.AgentRuntime;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agent 执行生命周期管理 Controller
 *
 * 职责：Agent 执行的唯一入口，提供执行、流式执行、暂停/恢复/取消、状态查询、历史记录。
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Execution", description = "Agent Execution")
@SaCheckPermission("agent:admin:list")
@RestController
@RequestMapping("/agent/execution")
public class ExecutionController {

    private final AgentRuntime agentRuntime;

    public ExecutionController(AgentRuntime agentRuntime) {
        this.agentRuntime = agentRuntime;
    }

    @Operation(summary = "Execute Agent")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/execute")
    public Result<Map<String, Object>> execute(
            @RequestParam String agentId,
            @RequestBody(required = false) Map<String, Object> input) {
        try {
            Map<String, Object> enrichedInput = new HashMap<>();
            if (input != null) enrichedInput.putAll(input);
            enrichedInput.put("agentId", agentId);
            enrichedInput.put("sessionId", UUID.randomUUID().toString());
            enrichedInput.put("userId", LoginContextHolder.getUserId());

            Execution execution = agentRuntime.execute(agentId, enrichedInput);

            Map<String, Object> result = new HashMap<>();
            result.put("executionId", execution.getExecutionId());
            result.put("status", execution.getStatus().name());
            result.put("output", execution.getOutput());
            result.put("durationMs", execution.getDurationMs());

            return Result.success(result);
        } catch (Exception e) {
            log.error("Agent 执行失败: agentId={}", agentId, e);
            return Result.fail("执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "Execute Agent Stream")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping(value = "/execute-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Result<Map<String, Object>>> executeStream(
            @RequestParam String agentId,
            @RequestBody(required = false) Map<String, Object> input) {
        Map<String, Object> enrichedInput = new HashMap<>();
        if (input != null) enrichedInput.putAll(input);
        enrichedInput.put("agentId", agentId);
        enrichedInput.put("sessionId", UUID.randomUUID().toString());
        enrichedInput.put("userId", LoginContextHolder.getUserId());

        return agentRuntime.executeStream(agentId, enrichedInput)
                .map(output -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("executionId", enrichedInput.get("sessionId"));
                    result.put("data", output);
                    return Result.success(result);
                })
                .onErrorResume(e -> {
                    log.error("Agent 流式执行失败: agentId={}", agentId, e);
                    return Flux.just(Result.fail("流式执行失败: " + e.getMessage()));
                });
    }

    @Operation(summary = "Pause Execution")
    @PostMapping("/pause")
    public Result<Void> pause(@RequestParam String executionId) {
        try {
            agentRuntime.pause(executionId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail("暂停失败: " + e.getMessage());
        }
    }

    @Operation(summary = "Resume Execution")
    @PostMapping("/resume")
    public Result<Map<String, Object>> resume(@RequestParam String executionId) {
        try {
            Execution execution = agentRuntime.resume(executionId);
            Map<String, Object> result = new HashMap<>();
            result.put("executionId", execution.getExecutionId());
            result.put("status", execution.getStatus().name());
            result.put("output", execution.getOutput());
            result.put("durationMs", execution.getDurationMs());
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("恢复执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "Cancel Execution")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestParam String executionId) {
        try {
            agentRuntime.cancel(executionId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail("取消失败: " + e.getMessage());
        }
    }

    @Operation(summary = "Get Execution Status")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(@RequestParam String executionId) {
        ExecutionStatus status = agentRuntime.getStatus(executionId);
        if (status == null) {
            return Result.fail("执行记录不存在");
        }
        return Result.success(Map.of("executionId", executionId, "status", status.name()));
    }

    @Operation(summary = "Get Execution Details")
    @GetMapping("/detail")
    public Result<Map<String, Object>> getExecution(@RequestParam String executionId) {
        Execution execution = agentRuntime.getExecution(executionId);
        if (execution == null) {
            return Result.fail("执行记录不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("executionId", execution.getExecutionId());
        result.put("agentId", execution.getAgentId());
        result.put("version", execution.getVersion());
        result.put("status", execution.getStatus().name());
        result.put("input", execution.getInput());
        result.put("output", execution.getOutput());
        result.put("errorMessage", execution.getErrorMessage());
        result.put("startTime", execution.getStartTime());
        result.put("endTime", execution.getEndTime());
        result.put("durationMs", execution.getDurationMs());
        return Result.success(result);
    }

    @Operation(summary = "Get Execution History")
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(
            @RequestParam String agentId,
            @RequestParam(defaultValue = "20") int limit) {
        List<Execution> executions = agentRuntime.getHistory(agentId, limit);
        List<Map<String, Object>> result = executions.stream().map(exec -> {
            Map<String, Object> item = new HashMap<>();
            item.put("executionId", exec.getExecutionId());
            item.put("status", exec.getStatus().name());
            item.put("durationMs", exec.getDurationMs());
            item.put("startTime", exec.getStartTime());
            item.put("errorMessage", exec.getErrorMessage());
            return item;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
}
