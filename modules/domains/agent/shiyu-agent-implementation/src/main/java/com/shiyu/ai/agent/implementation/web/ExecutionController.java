package com.shiyu.ai.agent.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.foundation.enums.BizResultCode;
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
 * 处理 Execution 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@Tag(name = "Execution", description = "Agent Execution")
@RestController
@RequestMapping("/api/agent/executions")
public class ExecutionController {

    /**
     * agentRuntime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentRuntime agentRuntime;

    /**
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentRuntime 用于完成本次业务处理的 agentRuntime 参数。
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
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Execution 用于完成本次业务处理的 Execution 参数。
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
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Execution 用于完成本次业务处理的 Execution 参数。
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
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Execution 用于完成本次业务处理的 Execution 参数。
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
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Status 用于完成本次业务处理的 Status 参数。
     */
    @Operation(summary = "Get Execution Status")
    @SaCheckPermission("agent:execute")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(@RequestParam String executionId) {
        ExecutionStatus status = agentRuntime.getStatus(actor(), executionId);
        if (status == null) {
            return Result.fail(BizResultCode.NOT_FOUND, "执行记录不存在");
        }
        return Result.success(Map.of("executionId", executionId, "status", status.name()));
    }

    /**
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param Details 用于完成本次业务处理的 Details 参数。
     */
    @Operation(summary = "Get Execution Details")
    @SaCheckPermission("agent:execute")
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
    @SaCheckPermission("agent:execute")
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
