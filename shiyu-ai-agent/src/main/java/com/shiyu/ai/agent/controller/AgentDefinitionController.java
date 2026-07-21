package com.shiyu.ai.agent.controller;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.service.AgentAdminService;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.agent.request.AgentRequest;
import com.shiyu.ai.agent.vo.AgentDetailVO;
import com.shiyu.ai.agent.vo.AgentVO;
import com.shiyu.ai.agent.vo.NodeTypeMetaVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 定义管理 Controller
 *
 * 职责：Agent 定义的全生命周期管理（CRUD）、节点类型元数据。
 * 合并来源：AgentAdminController + AgentController（非执行部分）+ NodeTypeController
 *
 * 注意：Agent 执行统一走 ExecutionController (/agent/execution)
 */
@Slf4j
@Tag(name = "Agent Definition", description = "Agent Definition")
@RestController
@RequestMapping("/agent/definition")
public class AgentDefinitionController {

    private final AgentAdminService agentAdminService;
    private final AgentService agentService;

    public AgentDefinitionController(AgentAdminService agentAdminService,
                                     AgentService agentService) {
        this.agentAdminService = agentAdminService;
        this.agentService = agentService;
    }

    // ======================== 来自 AgentAdminController ========================

    @Operation(summary = "Get Page")
    @GetMapping("/page")
    public Result<PageData<AgentVO>> getPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Pair<Long, List<AgentVO>> result = agentAdminService.getPage(pageNo, pageSize, name, status);
        return Result.success(new PageData<>(result.getRight(), result.getLeft()));
    }

    @Operation(summary = "Get by Id")
    @GetMapping("/detail")
    public Result<AgentDetailVO> getById(@RequestParam Long id) {
        AgentDetailVO vo = agentAdminService.getById(id);
        if (vo == null) return Result.fail("Agent不存在");
        return Result.success(vo);
    }

    @Operation(summary = "Create")
    @PostMapping("/create")
    public Result<AgentVO> create(@Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.create(request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("新增Agent失败", e);
            return Result.fail("新增失败");
        }
    }

    @Operation(summary = "Update")
    @PostMapping("/update")
    public Result<AgentVO> update(@RequestParam Long id, @Valid @RequestBody AgentRequest request) {
        try {
            AgentVO vo = agentAdminService.update(id, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("修改Agent失败", e);
            return Result.fail("修改失败");
        }
    }

    @Operation(summary = "Delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        try {
            agentAdminService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除Agent失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Update Status")
    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        AgentRequest request = new AgentRequest();
        request.setStatus(status);
        try {
            agentAdminService.update(id, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新Agent状态失败", e);
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "List All Options")
    @GetMapping("/options")
    public Result<List<IdNameOptionVO>> listAllOptions() {
        return Result.success(agentAdminService.listAllOptions());
    }

    // ======================== 来自 AgentController（非执行部分） ========================

    @Operation(summary = "Register Agent")
    @PostMapping("/register")
    public Result<Map<String, Object>> registerAgent(@RequestBody RegisterAgentRequest request) {
        log.info("收到 Agent 注册请求：agentId={}, name={}", request.getAgentId(), request.getName());
        try {
            AgentDefinition definition = AgentDefinition.builder()
                    .agentId(request.getAgentId())
                    .name(request.getName())
                    .description(request.getDescription())
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .build();

            if (request.getGraph() != null) {
                com.shiyu.ai.agent.AgentVersion version = com.shiyu.ai.agent.AgentVersion.builder()
                        .versionNumber(request.getVersionNumber() != null ? request.getVersionNumber() : "v1.0.0")
                        .description(request.getVersionDescription())
                        .graph(request.getGraph())
                        .createdAt(System.currentTimeMillis())
                        .build();
                definition.addVersion(version);
                definition.setCurrentVersion(version.getVersionNumber());
            }

            agentService.registerAgent(definition);
            return Result.success(Map.of("agentId", request.getAgentId()));
        } catch (Exception e) {
            log.error("Agent 注册失败：agentId={}", request.getAgentId(), e);
            return Result.fail("Agent 注册失败");
        }
    }

    @Operation(summary = "Get Agent")
    @GetMapping("/detail/by-agent-id")
    public Result<AgentDefinition> getAgent(@RequestParam String agentId) {
        log.info("收到 Agent 查询请求：agentId={}", agentId);
        AgentDefinition definition = agentService.getAgent(agentId);
        if (definition == null) {
            return Result.fail("Agent 不存在");
        }
        return Result.success(definition);
    }

    @Operation(summary = "Delete Agent")
    @PostMapping("/delete/by-agent-id")
    public Result<Void> deleteAgent(@RequestParam String agentId) {
        log.info("收到 Agent 删除请求：agentId={}", agentId);
        boolean success = agentService.unregisterAgent(agentId);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("Agent 不存在，删除失败");
        }
    }

    @Operation(summary = "Switch Version")
    @PostMapping("/version/switch")
    public Result<Void> switchVersion(
            @RequestParam String agentId,
            @RequestParam String version) {
        log.info("收到 Agent 版本切换请求：agentId={}, version={}", agentId, version);
        boolean success = agentService.switchVersion(agentId, version);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("版本切换失败，版本不存在");
        }
    }

    @Operation(summary = "List Agents")
    @GetMapping("/list")
    public Result<List<AgentDefinition>> listAgents() {
        log.info("收到 Agent 列表查询请求");
        return Result.success(agentService.listAgents());
    }

    // ======================== 来自 NodeTypeController ========================

    @Operation(summary = "Get Node Types")
    @GetMapping("/node-types")
    public Result<List<NodeTypeMetaVO>> getNodeTypes() {
        return Result.success(agentAdminService.getNodeTypes());
    }

    @Operation(summary = "Get Node Type")
    @GetMapping("/node-types/detail")
    public Result<NodeTypeMetaVO> getNodeType(@RequestParam String nodeType) {
        List<NodeTypeMetaVO> types = agentAdminService.getNodeTypes();
        return types.stream()
                .filter(t -> t.getCode().equalsIgnoreCase(nodeType))
                .findFirst()
                .map(Result::success)
                .orElse(Result.fail("节点类型不存在: " + nodeType));
    }

    // ======================== 内嵌请求模型 ========================

    @lombok.Data
    public static class RegisterAgentRequest {
        private String agentId;
        private String name;
        private String description;
        private String versionNumber;
        private String versionDescription;
        private com.shiyu.ai.agent.graph.Graph graph;
    }
}
