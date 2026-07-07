package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.service.AgentGraphService;
import com.shiyu.ai.aiagent.request.GraphConfigRequest;
import com.shiyu.ai.aiagent.request.GraphPreviewRequest;
import com.shiyu.ai.aiagent.request.CanvasConfigRequest;
import com.shiyu.ai.aiagent.request.EdgeRequest;
import com.shiyu.ai.aiagent.request.NodeConfigRequest;
import com.shiyu.ai.aiagent.request.VersionRequest;
import com.shiyu.ai.aiagent.vo.AgentVersionDetailVO;
import com.shiyu.ai.aiagent.vo.GraphValidationVO;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Slf4j
@Tag(name = "Agent Graph", description = "Agent Graph")
@RestController
@RequestMapping("/agent/graph")
public class AgentGraphController {

    private final AgentGraphService agentGraphService;

    public AgentGraphController(AgentGraphService agentGraphService) {
        this.agentGraphService = agentGraphService;
    }

    @Operation(summary = "Get Graph")
    @GetMapping("/detail")
    public Result<AgentVersionDetailVO> getGraph(
            @RequestParam String agentId, @RequestParam Long versionId) {
        AgentVersionDetailVO vo = agentGraphService.getGraphConfig(agentId, versionId);
        if (vo == null) return Result.fail("版本不存在");
        return Result.success(vo);
    }

    @Operation(summary = "Update Graph")
    @PostMapping("/update")
    public Result<AgentVersionDetailVO> updateGraph(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody GraphConfigRequest request) {
        try {
            AgentVersionDetailVO vo = agentGraphService.updateGraphConfig(agentId, versionId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("更新Graph配置失败", e);
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "Validate")
    @PostMapping("/validate")
    public Result<GraphValidationVO> validate(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody GraphConfigRequest request) {
        GraphValidationVO result = agentGraphService.validateGraphConfig(request);
        return Result.success(result);
    }

    @Operation(summary = "Add Node")
    @PostMapping("/node/create")
    public Result<Void> addNode(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody NodeConfigRequest request) {
        try {
            agentGraphService.addNode(agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加节点失败", e);
            return Result.fail("添加失败");
        }
    }

    @Operation(summary = "Update Node")
    @PostMapping("/node/update")
    public Result<Void> updateNode(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestParam String nodeId, @Valid @RequestBody NodeConfigRequest request) {
        try {
            agentGraphService.updateNode(agentId, versionId, nodeId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新节点失败", e);
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "Delete Node")
    @PostMapping("/node/delete")
    public Result<Void> deleteNode(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestParam String nodeId) {
        try {
            agentGraphService.deleteNode(agentId, versionId, nodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Add Edge")
    @PostMapping("/edge/create")
    public Result<Void> addEdge(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody EdgeRequest request) {
        try {
            agentGraphService.addEdge(agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加边失败", e);
            return Result.fail("添加失败");
        }
    }

    @Operation(summary = "Delete Edge")
    @PostMapping("/edge/delete")
    public Result<Void> deleteEdge(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestParam String sourceNodeId, @RequestParam String targetNodeId) {
        try {
            agentGraphService.deleteEdge(agentId, versionId, sourceNodeId, targetNodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除边失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Get Canvas")
    @GetMapping("/canvas")
    public Result<String> getCanvas(
            @RequestParam String agentId, @RequestParam Long versionId) {
        String canvas = agentGraphService.getCanvasConfig(agentId, versionId);
        return Result.success(canvas);
    }

    @Operation(summary = "Update Canvas")
    @PostMapping("/canvas-update")
    public Result<Void> updateCanvas(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestBody String canvasConfig) {
        try {
            agentGraphService.updateCanvasConfig(agentId, versionId, canvasConfig);
            return Result.success();
        } catch (Exception e) {
            log.error("更新画布失败", e);
            return Result.fail("更新失败");
        }
    }
}
