package com.shiyu.ai.agent.biz.agent.controller;

import com.shiyu.ai.agent.biz.agent.service.AgentAdminService;
import com.shiyu.ai.agent.domain.request.*;
import com.shiyu.ai.agent.domain.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.domain.vo.GraphValidationVO;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/agent/{agentId}/version/{versionId}/graph")
public class AgentGraphController {

    private final AgentAdminService agentAdminService;

    public AgentGraphController(AgentAdminService agentAdminService) {
        this.agentAdminService = agentAdminService;
    }

    @GetMapping
    public Result<AgentVersionDetailVO> getGraph(
            @PathVariable String agentId, @PathVariable Long versionId) {
        AgentVersionDetailVO vo = agentAdminService.getGraphConfig(agentId, versionId);
        if (vo == null) return Result.fail("版本不存在");
        return Result.success(vo);
    }

    @PutMapping
    public Result<AgentVersionDetailVO> updateGraph(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody GraphConfigRequest request) {
        try {
            AgentVersionDetailVO vo = agentAdminService.updateGraphConfig(agentId, versionId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("更新Graph配置失败", e);
            return Result.fail("更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public Result<GraphValidationVO> validate(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody GraphConfigRequest request) {
        GraphValidationVO result = agentAdminService.validateGraphConfig(request);
        return Result.success(result);
    }

    @PostMapping("/node")
    public Result<Void> addNode(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody NodeConfigRequest request) {
        try {
            agentAdminService.addNode(agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加节点失败", e);
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/node/{nodeId}")
    public Result<Void> updateNode(
            @PathVariable String agentId, @PathVariable Long versionId,
            @PathVariable String nodeId, @RequestBody NodeConfigRequest request) {
        try {
            agentAdminService.updateNode(agentId, versionId, nodeId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新节点失败", e);
            return Result.fail("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/node/{nodeId}")
    public Result<Void> deleteNode(
            @PathVariable String agentId, @PathVariable Long versionId,
            @PathVariable String nodeId) {
        try {
            agentAdminService.deleteNode(agentId, versionId, nodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/edge")
    public Result<Void> addEdge(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody EdgeRequest request) {
        try {
            agentAdminService.addEdge(agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加边失败", e);
            return Result.fail("添加失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/edge")
    public Result<Void> deleteEdge(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestParam String sourceNodeId, @RequestParam String targetNodeId) {
        try {
            agentAdminService.deleteEdge(agentId, versionId, sourceNodeId, targetNodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除边失败", e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/canvas")
    public Result<String> getCanvas(
            @PathVariable String agentId, @PathVariable Long versionId) {
        String canvas = agentAdminService.getCanvasConfig(agentId, versionId);
        return Result.success(canvas);
    }

    @PutMapping("/canvas")
    public Result<Void> updateCanvas(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody String canvasConfig) {
        try {
            agentAdminService.updateCanvasConfig(agentId, versionId, canvasConfig);
            return Result.success();
        } catch (Exception e) {
            log.error("更新画布失败", e);
            return Result.fail("更新失败：" + e.getMessage());
        }
    }
}
