package com.shiyu.ai.agent.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.agent.service.AgentVersionService;
import com.shiyu.ai.agent.request.EdgeRequest;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.agent.request.NodeConfigRequest;
import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.vo.AgentVersionVO;
import com.shiyu.ai.agent.vo.GraphValidationVO;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 版本 + Graph 配置管理 Controller
 *
 * 职责：版本 CRUD、版本生命周期（发布/归档/激活）、Graph 配置、节点/边管理、画布管理。
 * 合并来源：AgentVersionController + AgentGraphController
 */
@Slf4j
@Tag(name = "Agent Version", description = "Agent Version & Graph")
@SaCheckPermission("agent:admin:list")
@RestController
@RequestMapping("/agent/version")
public class AgentVersionController {

    private final AgentVersionService agentVersionService;

    public AgentVersionController(AgentVersionService agentVersionService) {
        this.agentVersionService = agentVersionService;
    }

    // ======================== 版本基础 CRUD ========================

    @Operation(summary = "Get Versions")
    @GetMapping("/list")
    public Result<List<AgentVersionVO>> getVersions(@RequestParam String agentId) {
        return Result.success(agentVersionService.getVersions(agentId));
    }

    @Operation(summary = "Get Version Detail")
    @GetMapping("/detail")
    public Result<AgentVersionDetailVO> getVersionDetail(
            @RequestParam String agentId, @RequestParam Long versionId) {
        AgentVersionDetailVO vo = agentVersionService.getVersionDetail(agentId, versionId);
        if (vo == null) return Result.fail("版本不存在");
        return Result.success(vo);
    }

    @Operation(summary = "Create Version")
    @SaCheckPermission("agent:admin:create")
    @PostMapping("/create")
    public Result<AgentVersionVO> createVersion(
            @RequestParam String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(agentVersionService.createVersion(agentId, request));
        } catch (Exception e) {
            log.error("新增版本失败", e);
            return Result.fail("新增失败");
        }
    }

    @Operation(summary = "Update Version")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/update")
    public Result<AgentVersionVO> updateVersion(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(agentVersionService.updateVersion(agentId, versionId, request));
        } catch (Exception e) {
            log.error("修改版本失败", e);
            return Result.fail("修改失败");
        }
    }

    @Operation(summary = "Delete Version")
    @SaCheckPermission("agent:admin:delete")
    @PostMapping("/delete")
    public Result<Void> deleteVersion(
            @RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.deleteVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除版本失败", e);
            return Result.fail("删除失败");
        }
    }

    // ======================== 版本生命周期 ========================

    @Operation(summary = "Publish")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/publish")
    public Result<Void> publish(@RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.publishVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("发布版本失败", e);
            return Result.fail("发布失败");
        }
    }

    @Operation(summary = "Archive")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/archive")
    public Result<Void> archive(@RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.archiveVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("归档版本失败", e);
            return Result.fail("归档失败");
        }
    }

    @Operation(summary = "Activate")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/activate")
    public Result<Void> activate(@RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.activateVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("激活版本失败", e);
            return Result.fail("激活失败");
        }
    }

    @Operation(summary = "Copy")
    @SaCheckPermission("agent:admin:create")
    @PostMapping("/copy")
    public Result<AgentVersionVO> copy(@RequestParam String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(agentVersionService.copyVersion(agentId, request));
        } catch (Exception e) {
            log.error("复制版本失败", e);
            return Result.fail("复制失败");
        }
    }

    // ======================== Graph 配置（来自 AgentGraphController） ========================

    @Operation(summary = "Get Graph")
    @GetMapping("/graph/detail")
    public Result<AgentVersionDetailVO> getGraph(
            @RequestParam String agentId, @RequestParam Long versionId) {
        AgentVersionDetailVO vo = agentVersionService.getGraphConfig(agentId, versionId);
        if (vo == null) return Result.fail("版本不存在");
        return Result.success(vo);
    }

    @Operation(summary = "Update Graph")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/update")
    public Result<AgentVersionDetailVO> updateGraph(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody GraphConfigRequest request) {
        try {
            return Result.success(agentVersionService.updateGraphConfig(agentId, versionId, request));
        } catch (Exception e) {
            log.error("更新Graph配置失败", e);
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "Validate Graph")
    @PostMapping("/graph/validate")
    public Result<GraphValidationVO> validate(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody GraphConfigRequest request) {
        return Result.success(agentVersionService.validateGraphConfig(request));
    }

    @Operation(summary = "Add Node")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/node/create")
    public Result<Void> addNode(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody NodeConfigRequest request) {
        try {
            agentVersionService.addNode(agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加节点失败", e);
            return Result.fail("添加失败");
        }
    }

    @Operation(summary = "Update Node")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/node/update")
    public Result<Void> updateNode(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestParam String nodeId, @Valid @RequestBody NodeConfigRequest request) {
        try {
            agentVersionService.updateNode(agentId, versionId, nodeId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新节点失败", e);
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "Delete Node")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/node/delete")
    public Result<Void> deleteNode(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestParam String nodeId) {
        try {
            agentVersionService.deleteNode(agentId, versionId, nodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Add Edge")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/edge/create")
    public Result<Void> addEdge(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody EdgeRequest request) {
        try {
            agentVersionService.addEdge(agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加边失败", e);
            return Result.fail("添加失败");
        }
    }

    @Operation(summary = "Delete Edge")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/edge/delete")
    public Result<Void> deleteEdge(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestParam String sourceNodeId, @RequestParam String targetNodeId) {
        try {
            agentVersionService.deleteEdge(agentId, versionId, sourceNodeId, targetNodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除边失败", e);
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "Get Canvas")
    @GetMapping("/graph/canvas")
    public Result<String> getCanvas(
            @RequestParam String agentId, @RequestParam Long versionId) {
        String canvas = agentVersionService.getCanvasConfig(agentId, versionId);
        return Result.success(canvas);
    }

    @Operation(summary = "Update Canvas")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/canvas-update")
    public Result<Void> updateCanvas(
            @RequestParam String agentId, @RequestParam Long versionId,
            @RequestBody String canvasConfig) {
        try {
            agentVersionService.updateCanvasConfig(agentId, versionId, canvasConfig);
            return Result.success();
        } catch (Exception e) {
            log.error("更新画布失败", e);
            return Result.fail("更新失败");
        }
    }
}
