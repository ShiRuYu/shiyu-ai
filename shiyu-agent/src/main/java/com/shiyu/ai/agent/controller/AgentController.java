package com.shiyu.ai.agent.controller;

import com.shiyu.ai.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.domain.AgentVersion;
import com.shiyu.ai.agent.domain.ChatType;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.*;

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
    public ResponseEntity<Map<String, Object>> registerAgent(@RequestBody RegisterAgentRequest request) {
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Agent 注册成功");
            response.put("agentId", request.getAgentId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Agent 注册失败：agentId={}", request.getAgentId(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Agent 注册失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 获取 Agent 定义
     * @param agentId Agent ID
     * @return AgentDefinition
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<Map<String, Object>> getAgent(@PathVariable String agentId) {
        log.info("收到 Agent 查询请求：agentId={}", agentId);
        
        AgentDefinition definition = agentService.getAgent(agentId);
        
        if (definition == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Agent 不存在"
            ));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", definition);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除 Agent
     * @param agentId Agent ID
     * @return 删除结果
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<Map<String, Object>> deleteAgent(@PathVariable String agentId) {
        log.info("收到 Agent 删除请求：agentId={}", agentId);
        
        boolean success = agentService.unregisterAgent(agentId);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "Agent 删除成功");
        } else {
            response.put("success", false);
            response.put("message", "Agent 不存在，删除失败");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 执行 Agent（同步）
     * @param agentId Agent ID
     * @param input 输入数据
     * @return 执行结果
     */
    @PostMapping("/{agentId}/execute")
    public ResponseEntity<Map<String, Object>> executeAgent(
            @PathVariable String agentId,
            @RequestBody Map<String, Object> input) {
        log.info("收到 Agent 执行请求：agentId={}", agentId);
        
        try {
            // 设置 chatType 为 SYNC
            input.put("chatType", ChatType.SYNC.name());
            
            Map<String, Object> result = agentService.execute(agentId, input);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("chatType", ChatType.SYNC.name());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Agent 执行失败：agentId={}", agentId, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Agent 执行失败：" + e.getMessage()
            ));
        }
    }

    /**
     * 执行 Agent（流式）
     * @param agentId Agent ID
     * @param input 输入数据
     * @return 流式执行结果
     */
    @PostMapping(value = "/{agentId}/executeStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> executeStreamAgent(
            @PathVariable String agentId,
            @RequestBody Map<String, Object> input) {
        log.info("收到 Agent 流式执行请求：agentId={}", agentId);
        
        try {
            // 设置 chatType 为 STREAM
            input.put("chatType", ChatType.STREAM.name());
            
            return agentService.executeStream(agentId, input)
                    .map(result -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("data", result);
                        response.put("chatType", ChatType.STREAM.name());
                        return response;
                    })
                    .onErrorResume(e -> {
                        log.error("Agent 流式执行失败：agentId={}", agentId, e);
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Agent 流式执行失败：" + e.getMessage());
                        errorResponse.put("chatType", ChatType.STREAM.name());
                        return Flux.just(errorResponse);
                    });
            
        } catch (Exception e) {
            log.error("Agent 流式执行失败：agentId={}", agentId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Agent 流式执行失败：" + e.getMessage());
            errorResponse.put("chatType", ChatType.STREAM.name());
            return Flux.just(errorResponse);
        }
    }

    /**
     * 切换 Agent 版本
     * @param agentId Agent ID
     * @param version 版本号
     * @return 切换结果
     */
    @PostMapping("/{agentId}/version/switch")
    public ResponseEntity<Map<String, Object>> switchVersion(
            @PathVariable String agentId,
            @RequestParam String version) {
        log.info("收到 Agent 版本切换请求：agentId={}, version={}", agentId, version);
        
        boolean success = agentService.switchVersion(agentId, version);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "版本切换成功");
        } else {
            response.put("success", false);
            response.put("message", "版本切换失败，版本不存在");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有已注册的 Agent
     * @return Agent 列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listAgents() {
        log.info("收到 Agent 列表查询请求");
        
        List<AgentDefinition> agents = agentService.listAgents();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", agents);
        response.put("total", agents.size());
        
        return ResponseEntity.ok(response);
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
