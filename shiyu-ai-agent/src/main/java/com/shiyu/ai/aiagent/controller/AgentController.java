package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.langgraph4j.AgentDefinition;
import com.shiyu.ai.aiagent.langgraph4j.AgentVersion;
import com.shiyu.ai.model.ChatType;
import com.shiyu.ai.aiagent.langgraph4j.graph.Graph;
import com.shiyu.ai.aiagent.langgraph4j.node.NodeFields;
import com.shiyu.ai.aiagent.service.AgentService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Agent Controller
 * 提供 Agent 管理、执行和版本控制的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * 注册 Agent
     * @param request 注册请求（包含 agentId、name、description、graph）
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> registerAgent(@RequestBody RegisterAgentRequest request) {
        log.info("收到 Agent 注册请求：agentId={}, name={}", request.getAgentId(), request.getName());
        
        try {
            // 构建 AgentDefinition
            AgentDefinition definition = AgentDefinition.builder()
                    .agentId(request.getAgentId())
                    .name(request.getName())
                    .description(request.getDescription())
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .build();
            
            // 构建 AgentVersion
            if (request.getGraph() != null) {
                AgentVersion version = AgentVersion.builder()
                        .versionNumber(request.getVersionNumber() != null ? request.getVersionNumber() : "v1.0.0")
                        .description(request.getVersionDescription())
                        .graph(request.getGraph())
                        .createdAt(System.currentTimeMillis())
                        .build();
                definition.addVersion(version);
                definition.setCurrentVersion(version.getVersionNumber());
            }
            
            // 注册 Agent
            agentService.registerAgent(definition);
            
            return Result.success(Map.of(
                    "agentId", request.getAgentId()
            ));
            
        } catch (Exception e) {
            log.error("Agent 注册失败：agentId={}", request.getAgentId(), e);
            return Result.fail("Agent 注册失败");
        }
    }

    /**
     * 获取 Agent 定义
     * @param agentId Agent ID
     * @return AgentDefinition
     */
    @GetMapping("/{agentId}")
    public Result<AgentDefinition> getAgent(@PathVariable String agentId) {
        log.info("收到 Agent 查询请求：agentId={}", agentId);
        
        AgentDefinition definition = agentService.getAgent(agentId);
        
        if (definition == null) {
            return Result.fail("Agent 不存在");
        }
        
        return Result.success(definition);
    }

    /**
     * 删除 Agent
     * @param agentId Agent ID
     * @return 删除结果
     */
    @PostMapping("/{agentId}")
    public Result<Void> deleteAgent(@PathVariable String agentId) {
        log.info("收到 Agent 删除请求：agentId={}", agentId);
        
        boolean success = agentService.unregisterAgent(agentId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("Agent 不存在，删除失败");
        }
    }

    /**
     * 执行 Agent（同步）
     * @param agentId Agent ID
     * @param body 输入数据（POST 请求体）
     * @param params 查询参数（GET 请求）
     * @return 执行结果
     */
    @RequestMapping(value = "/{agentId}/execute", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Map<String, Object>> executeAgent(
            @PathVariable String agentId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Map<String, String> params) {
        log.info("收到 Agent 执行请求：agentId={}", agentId);
        
        try {
            Map<String, Object> input = new HashMap<>();
            if (body != null) {
                input.putAll(body);
            }
            if (params != null) {
                input.putAll(params);
            }
            input.put(NodeFields.FieldKey.CHAT_TYPE.key(), ChatType.SYNC.name());
            input.put(NodeFields.FieldKey.AGENT_ID.key(), agentId);
            input.put(NodeFields.FieldKey.SESSION_ID.key(), UUID.randomUUID().toString());
            input.put(NodeFields.FieldKey.USER_ID.key(), LoginContextHolder.getUserId());
            
            Map<String, Object> result = agentService.execute(agentId, input);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("Agent 执行失败：agentId={}", agentId, e);
            return Result.fail("Agent 执行失败");
        }
    }

    /**
     * 执行 Agent（流式）
     * @param agentId Agent ID
     * @param body 输入数据（POST 请求体）
     * @param params 查询参数（GET 请求）
     * @return 流式执行结果
     */
    @RequestMapping(value = "/{agentId}/executeStream", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Result<Map<String, Object>>> executeStreamAgent(
            @PathVariable String agentId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Map<String, String> params) {
        log.info("收到 Agent 流式执行请求：agentId={}", agentId);
        
        try {
            Map<String, Object> input = new HashMap<>();
            if (body != null) {
                input.putAll(body);
            }
            if (params != null) {
                input.putAll(params);
            }
            input.put(NodeFields.FieldKey.CHAT_TYPE.key(), ChatType.STREAM.name());
            input.put(NodeFields.FieldKey.AGENT_ID.key(), agentId);
            input.put(NodeFields.FieldKey.SESSION_ID.key(), UUID.randomUUID().toString());
            input.put(NodeFields.FieldKey.USER_ID.key(), LoginContextHolder.getUserId());
            
            return agentService.executeStream(agentId, input)
                    .map(Result::success)
                    .onErrorResume(e -> {
                        log.error("Agent 流式执行失败：agentId={}", agentId, e);
                        return Flux.just(Result.fail("Agent 流式执行失败"));
                    });
            
        } catch (Exception e) {
            log.error("Agent 流式执行失败：agentId={}", agentId, e);
            return Flux.just(Result.fail("Agent 流式执行失败"));
        }
    }

    /**
     * 切换 Agent 版本
     * @param agentId Agent ID
     * @param version 版本号
     * @return 切换结果
     */
    @PostMapping("/{agentId}/version/switch")
    public Result<Void> switchVersion(
            @PathVariable String agentId,
            @RequestParam String version) {
        log.info("收到 Agent 版本切换请求：agentId={}, version={}", agentId, version);
        
        boolean success = agentService.switchVersion(agentId, version);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("版本切换失败，版本不存在");
        }
    }

    /**
     * 获取所有已注册的 Agent
     * @return Agent 列表
     */
    @GetMapping("/list")
    public Result<List<AgentDefinition>> listAgents() {
        log.info("收到 Agent 列表查询请求");
        
        List<AgentDefinition> agents = agentService.listAgents();
        
        return Result.success(agents);
    }

    /**
     * 注册 Agent 请求参数
     */
    @lombok.Data
    public static class RegisterAgentRequest {
        /**
         * Agent ID（唯一标识）
         */
        private String agentId;
        
        /**
         * Agent 名称
         */
        private String name;
        
        /**
         * Agent 描述
         */
        private String description;
        
        /**
         * 版本号（可选，默认 v1.0.0）
         */
        private String versionNumber;
        
        /**
         * 版本描述
         */
        private String versionDescription;
        
        /**
         * Graph 定义
         */
        private Graph graph;
    }
}
