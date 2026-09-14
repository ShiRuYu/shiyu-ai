package com.shiyu.ai.agent.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.implementation.request.EdgeRequest;
import com.shiyu.ai.agent.implementation.request.GraphConfigRequest;
import com.shiyu.ai.agent.implementation.request.NodeConfigRequest;
import com.shiyu.ai.agent.implementation.request.VersionRequest;
import com.shiyu.ai.agent.implementation.service.AgentVersionService;
import com.shiyu.ai.agent.implementation.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.implementation.vo.AgentVersionVO;
import com.shiyu.ai.agent.implementation.vo.GraphValidationVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 版本 + Graph 配置管理 Controller
 *
 * <p>职责：版本 CRUD、版本生命周期（发布/归档/激活）、Graph 配置、节点/边管理、画布管理。 合并来源：AgentVersionController +
 * AgentGraphController
 */
@Slf4j
@Tag(name = "Agent Version", description = "Agent Version & Graph")
@SaCheckPermission("agent:admin:list")
@RestController
@RequestMapping("/api/agent/versions")
public class AgentVersionController {

    /**
     * agentVersionService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentVersionService agentVersionService;

    /**
     * {@code AgentVersionController} 创建并初始化当前类型实例。
     *
     * @param agentVersionService 参数值，用于执行当前操作。
     */
    public AgentVersionController(AgentVersionService agentVersionService) {
        this.agentVersionService = agentVersionService;
    }

    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }

    // ======================== 版本基础 CRUD ========================

    /**
     * {@code getVersions} 查询并返回当前操作所需的数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Versions")
    @GetMapping("/list")
    public Result<List<AgentVersionVO>> getVersions(@RequestParam String agentId) {
        return Result.success(agentVersionService.getVersions(actor(), agentId));
    }

    /**
     * {@code getVersionDetail} 查询并返回当前操作所需的数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Version Detail")
    @GetMapping("/detail")
    public Result<AgentVersionDetailVO> getVersionDetail(
            @RequestParam String agentId, @RequestParam Long versionId) {
        AgentVersionDetailVO vo = agentVersionService.getVersionDetail(actor(), agentId, versionId);
        if (vo == null) return Result.fail(BizResultCode.NOT_FOUND, "版本不存在");
        return Result.success(vo);
    }

    /**
     * {@code createVersion} 写入或更新当前模块中的业务数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Create Version")
    @SaCheckPermission("agent:admin:create")
    @PostMapping("/create")
    public Result<AgentVersionVO> createVersion(
            @RequestParam String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(agentVersionService.createVersion(actor(), agentId, request));
        } catch (Exception e) {
            log.error("新增版本失败", e);
            return Result.fail("新增失败");
        }
    }

    /**
     * {@code updateVersion} 写入或更新当前模块中的业务数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Update Version")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/update")
    public Result<AgentVersionVO> updateVersion(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(
                    agentVersionService.updateVersion(actor(), agentId, versionId, request));
        } catch (Exception e) {
            log.error("修改版本失败", e);
            return Result.fail("修改失败");
        }
    }

    /**
     * {@code deleteVersion} 释放或移除当前操作涉及的资源。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Delete Version")
    @SaCheckPermission("agent:admin:delete")
    @PostMapping("/delete")
    public Result<Void> deleteVersion(@RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.deleteVersion(actor(), agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除版本失败", e);
            return Result.fail("删除失败");
        }
    }

    // ======================== 版本生命周期 ========================

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Publish")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/publish")
    public Result<Void> publish(@RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.publishVersion(actor(), agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("发布版本失败", e);
            return Result.fail("发布失败");
        }
    }

    /**
     * {@code archive} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Archive")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/archive")
    public Result<Void> archive(@RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.archiveVersion(actor(), agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("归档版本失败", e);
            return Result.fail("归档失败");
        }
    }

    /**
     * {@code activate} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Activate")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/activate")
    public Result<Void> activate(@RequestParam String agentId, @RequestParam Long versionId) {
        try {
            agentVersionService.activateVersion(actor(), agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("激活版本失败", e);
            return Result.fail("激活失败");
        }
    }

    /**
     * {@code copy} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Copy")
    @SaCheckPermission("agent:admin:create")
    @PostMapping("/copy")
    public Result<AgentVersionVO> copy(
            @RequestParam String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(agentVersionService.copyVersion(actor(), agentId, request));
        } catch (Exception e) {
            log.error("复制版本失败", e);
            return Result.fail("复制失败");
        }
    }

    // ======================== Graph 配置（来自 AgentGraphController） ========================

    /**
     * {@code getGraph} 查询并返回当前操作所需的数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Graph")
    @GetMapping("/graph/detail")
    public Result<AgentVersionDetailVO> getGraph(
            @RequestParam String agentId, @RequestParam Long versionId) {
        AgentVersionDetailVO vo = agentVersionService.getGraphConfig(actor(), agentId, versionId);
        if (vo == null) return Result.fail(BizResultCode.NOT_FOUND, "版本不存在");
        return Result.success(vo);
    }

    /**
     * {@code updateGraph} 写入或更新当前模块中的业务数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Update Graph")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/update")
    public Result<AgentVersionDetailVO> updateGraph(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @Valid @RequestBody GraphConfigRequest request) {
        try {
            return Result.success(
                    agentVersionService.updateGraphConfig(actor(), agentId, versionId, request));
        } catch (Exception e) {
            log.error("更新Graph配置失败", e);
            return Result.fail("更新失败");
        }
    }

    /**
     * {@code validate} 校验当前操作的输入或状态是否满足约束。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Validate Graph")
    @PostMapping("/graph/validate")
    public Result<GraphValidationVO> validate(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @Valid @RequestBody GraphConfigRequest request) {
        return Result.success(agentVersionService.validateGraphConfig(request));
    }

    /**
     * {@code addNode} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Add Node")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/node/create")
    public Result<Void> addNode(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @Valid @RequestBody NodeConfigRequest request) {
        try {
            agentVersionService.addNode(actor(), agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加节点失败", e);
            return Result.fail("添加失败");
        }
    }

    /**
     * {@code updateNode} 写入或更新当前模块中的业务数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param nodeId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Update Node")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/node/update")
    public Result<Void> updateNode(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @RequestParam String nodeId,
            @Valid @RequestBody NodeConfigRequest request) {
        try {
            agentVersionService.updateNode(actor(), agentId, versionId, nodeId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新节点失败", e);
            return Result.fail("更新失败");
        }
    }

    /**
     * {@code deleteNode} 释放或移除当前操作涉及的资源。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param nodeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Delete Node")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/node/delete")
    public Result<Void> deleteNode(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @RequestParam String nodeId) {
        try {
            agentVersionService.deleteNode(actor(), agentId, versionId, nodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除节点失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * {@code addEdge} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Add Edge")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/edge/create")
    public Result<Void> addEdge(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @Valid @RequestBody EdgeRequest request) {
        try {
            agentVersionService.addEdge(actor(), agentId, versionId, request);
            return Result.success();
        } catch (Exception e) {
            log.error("添加边失败", e);
            return Result.fail("添加失败");
        }
    }

    /**
     * {@code deleteEdge} 释放或移除当前操作涉及的资源。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param sourceNodeId 参数值，用于执行当前操作。
     * @param targetNodeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Delete Edge")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/edge/delete")
    public Result<Void> deleteEdge(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @RequestParam String sourceNodeId,
            @RequestParam String targetNodeId) {
        try {
            agentVersionService.deleteEdge(actor(), agentId, versionId, sourceNodeId, targetNodeId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除边失败", e);
            return Result.fail("删除失败");
        }
    }

    /**
     * {@code getCanvas} 查询并返回当前操作所需的数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Get Canvas")
    @GetMapping("/graph/canvas")
    public Result<String> getCanvas(@RequestParam String agentId, @RequestParam Long versionId) {
        String canvas = agentVersionService.getCanvasConfig(actor(), agentId, versionId);
        return Result.success(canvas);
    }

    /**
     * {@code updateCanvas} 写入或更新当前模块中的业务数据。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param canvasConfig 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Operation(summary = "Update Canvas")
    @SaCheckPermission("agent:admin:edit")
    @PostMapping("/graph/canvas-update")
    public Result<Void> updateCanvas(
            @RequestParam String agentId,
            @RequestParam Long versionId,
            @RequestBody String canvasConfig) {
        try {
            agentVersionService.updateCanvasConfig(actor(), agentId, versionId, canvasConfig);
            return Result.success();
        } catch (Exception e) {
            log.error("更新画布失败", e);
            return Result.fail("更新失败");
        }
    }
}
