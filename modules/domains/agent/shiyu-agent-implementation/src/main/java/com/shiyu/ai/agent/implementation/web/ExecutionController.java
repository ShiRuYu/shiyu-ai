package com.shiyu.ai.agent.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;

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
 * <p>职责：Agent 执行的唯一入口，提供执行、流式执行、暂停/恢复/取消、状态查询、历史记录。 注意：所有参数均通过 @RequestParam 或 @RequestBody
 * 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Execution", description = "Agent Execution")
@SaCheckPermission("agent:admin:list")
@RestController
@RequestMapping("/api/agent/executions")
public class ExecutionController {

    /**
     * agentRuntime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentRuntime agentRuntime;

    /**
     * {@code ExecutionController} 创建并初始化当前类型实例。
     *
     * @param agentRuntime 参数值，用于执行当前操作。
     */
    public ExecutionController(AgentRuntime agentRuntime) {
        this.agentRuntime = agentRuntime;
    }

    /**
     * {@code execute} 执行当前模块定义的业务流程。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Execute Agent")
    @SaCheckPermission("agent:execute")
    @PostMapping("/execute")
    public Result<Map<String, Object>> execute(
            @RequestParam String agentId,
            @RequestBody(required = false) Map<String, Object> input) {
        try {
            Map<String, Object> enrichedInput = new HashMap<>();
            if (input != null) enrichedInput.putAll(input);
            enrichedInput.put("agentId", agentId);
            enrichedInput.put("sessionId", UUID.randomUUID().toString());
            enrichedInput.put("__knowledgeAccessContext", actor());

            Execution execution = agentRuntime.execute(actor(), agentId, enrichedInput);

            Map<String, Object> result = new HashMap<>();
            result.put("executionId", execution.getExecutionId());
            result.put("status", execution.getStatus().name());
            result.put("output", execution.getOutput());
            result.put("durationMs", execution.getDurationMs());

            return Result.success(result);
        } catch (Exception e) {
            log.error(
                    "Agent 执行失败: agentIdPresent={}, errorType={}, errorMessageLength={}",
                    agentId != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return failure("执行", agentId, e);
        }
    }

    /**
     * {@code executeStream} 执行当前模块定义的业务流程。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Execute Agent Stream")
    @SaCheckPermission("agent:execute")
    @PostMapping(value = "/execute-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Result<Map<String, Object>>> executeStream(
            @RequestParam String agentId,
            @RequestBody(required = false) Map<String, Object> input) {
        Map<String, Object> enrichedInput = new HashMap<>();
        if (input != null) enrichedInput.putAll(input);
        enrichedInput.put("agentId", agentId);
        enrichedInput.put("sessionId", UUID.randomUUID().toString());
        enrichedInput.put("__knowledgeAccessContext", actor());

        return agentRuntime
                .executeStream(actor(), agentId, enrichedInput)
                .map(
                        output -> {
                            Map<String, Object> result = new HashMap<>();
                            result.put("executionId", output.get("executionId"));
                            result.put("data", output);
                            return Result.success(result);
                        })
                .onErrorResume(
                        e -> {
                            log.error(
                                    "Agent 流式执行失败: agentIdPresent={}, errorType={},"
                                            + " errorMessageLength={}",
                                    agentId != null,
                                    e.getClass().getSimpleName(),
                                    e.getMessage() == null ? 0 : e.getMessage().length());
                            return Flux.just(failure("流式执行", agentId, e));
                        });
    }

    /**
     * {@code pause} 执行当前类型定义的业务操作。
     *
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Pause Execution")
    @SaCheckPermission("agent:execute")
    @PostMapping("/pause")
    public Result<Void> pause(@RequestParam String executionId) {
        try {
            agentRuntime.pause(actor(), executionId);
            return Result.success();
        } catch (Exception e) {
            return failure("暂停", executionId, e);
        }
    }

    /**
     * {@code resume} 执行当前类型定义的业务操作。
     *
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Resume Execution")
    @SaCheckPermission("agent:execute")
    @PostMapping("/resume")
    public Result<Map<String, Object>> resume(@RequestParam String executionId) {
        try {
            Execution execution = agentRuntime.resume(actor(), executionId);
            Map<String, Object> result = new HashMap<>();
            result.put("executionId", execution.getExecutionId());
            result.put("status", execution.getStatus().name());
            result.put("output", execution.getOutput());
            result.put("durationMs", execution.getDurationMs());
            return Result.success(result);
        } catch (Exception e) {
            return failure("恢复执行", executionId, e);
        }
    }

    /**
     * {@code cancel} 校验当前操作的输入或状态是否满足约束。
     *
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Cancel Execution")
    @SaCheckPermission("agent:execute")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestParam String executionId) {
        try {
            agentRuntime.cancel(actor(), executionId);
            return Result.success();
        } catch (Exception e) {
            return failure("取消", executionId, e);
        }
    }

    /**
     * {@code getStatus} 查询并返回当前操作所需的数据。
     *
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Execution Status")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(@RequestParam String executionId) {
        ExecutionStatus status = agentRuntime.getStatus(actor(), executionId);
        if (status == null) {
            return Result.fail(BizResultCode.NOT_FOUND, "执行记录不存在");
        }
        return Result.success(Map.of("executionId", executionId, "status", status.name()));
    }

    /**
     * {@code getExecution} 查询并返回当前操作所需的数据。
     *
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Execution Details")
    @GetMapping("/detail")
    public Result<Map<String, Object>> getExecution(@RequestParam String executionId) {
        Execution execution = agentRuntime.getExecution(actor(), executionId);
        if (execution == null) {
            return Result.fail(BizResultCode.NOT_FOUND, "执行记录不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("executionId", execution.getExecutionId());
        result.put("agentId", execution.getAgentId());
        result.put("version", execution.getVersion());
        result.put("status", execution.getStatus().name());
        result.put("input", execution.getInput());
        result.put("output", execution.getOutput());
        result.put("errorMessage", publicErrorMessage(execution));
        result.put("startTime", execution.getStartTime());
        result.put("endTime", execution.getEndTime());
        result.put("durationMs", execution.getDurationMs());
        return Result.success(result);
    }

    /**
     * {@code getHistory} 查询并返回当前操作所需的数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Execution History")
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(
            @RequestParam String agentId, @RequestParam(defaultValue = "20") int limit) {
        List<Execution> executions =
                agentRuntime.getHistory(actor(), agentId, Math.max(1, Math.min(limit, 100)));
        List<Map<String, Object>> result =
                executions.stream()
                        .map(
                                exec -> {
                                    Map<String, Object> item = new HashMap<>();
                                    item.put("executionId", exec.getExecutionId());
                                    item.put("status", exec.getStatus().name());
                                    item.put("durationMs", exec.getDurationMs());
                                    item.put("startTime", exec.getStartTime());
                                    item.put("errorMessage", publicErrorMessage(exec));
                                    return item;
                                })
                        .collect(Collectors.toList());
        return Result.success(result);
    }

    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }

    private String publicErrorMessage(Execution execution) {
        return execution.getStatus() == ExecutionStatus.FAILED ? "执行失败，请稍后重试" : null;
    }

    private <T> Result<T> failure(String operation, String resourceId, Throwable exception) {
        log.error(
                "Agent{}失败: resourceIdPresent={}, errorType={}, errorMessageLength={}",
                operation,
                resourceId != null,
                exception.getClass().getSimpleName(),
                exception.getMessage() == null ? 0 : exception.getMessage().length());
        return Result.fail(operation + "失败，请稍后重试");
    }
}
