package com.shiyu.ai.agent.implementation.web;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.shiyu.ai.agent.implementation.request.VersionRequest;
import com.shiyu.ai.agent.implementation.service.AgentVersionService;
import com.shiyu.ai.agent.implementation.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.implementation.vo.AgentVersionVO;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.foundation.enums.BizResultCode;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供智能体版本资源的 REST CRUD 接口。
 *
 * <p>版本的生命周期和图编辑动作仍由 {@link AgentVersionController} 保持原有入口处理，
 * 本控制器只负责版本集合和单个版本资源的创建、查询、更新与删除。
 */
@Slf4j
@Tag(name = "Agent Version CRUD", description = "Agent Version resource CRUD")
@RestController
@RequestMapping("/api/agent/agents/{agentId}/versions")
public class AgentVersionCrudController {

    private final AgentVersionService agentVersionService;

    /**
     * 创建版本 CRUD 控制器。
     *
     * @param agentVersionService 版本应用服务。
     */
    public AgentVersionCrudController(AgentVersionService agentVersionService) {
        this.agentVersionService = agentVersionService;
    }

    /**
     * 查询指定智能体的版本集合。
     *
     * @param agentId 智能体业务标识。
     * @return 版本摘要集合。
     */
    @Operation(summary = "List Agent Versions")
    @SaCheckPermission("agent:admin:list")
    @GetMapping
    public Result<List<AgentVersionVO>> getVersions(
            @PathVariable("agentId") String agentId) {
        return Result.success(agentVersionService.getVersions(actor(), agentId));
    }

    /**
     * 查询指定智能体的单个版本详情。
     *
     * @param agentId 智能体业务标识。
     * @param versionId 版本数据库标识。
     * @return 版本详情；目标不存在时返回未找到结果。
     */
    @Operation(summary = "Get Agent Version")
    @SaCheckPermission("agent:admin:list")
    @GetMapping("/{versionId}")
    public Result<AgentVersionDetailVO> getVersionDetail(
            @PathVariable("agentId") String agentId,
            @PathVariable("versionId") Long versionId) {
        AgentVersionDetailVO detail =
                agentVersionService.getVersionDetail(actor(), agentId, versionId);
        if (detail == null) {
            return Result.fail(BizResultCode.NOT_FOUND, "版本不存在");
        }
        return Result.success(detail);
    }

    /**
     * 为指定智能体创建版本。
     *
     * @param agentId 智能体业务标识。
     * @param request 版本创建数据。
     * @return 创建后的版本摘要。
     */
    @Operation(summary = "Create Agent Version")
    @SaCheckPermission("agent:admin:create")
    @PostMapping
    public Result<AgentVersionVO> createVersion(
            @PathVariable("agentId") String agentId,
            @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(agentVersionService.createVersion(actor(), agentId, request));
        } catch (Exception exception) {
            log.error("新增版本失败", exception);
            return Result.fail("新增失败");
        }
    }

    /**
     * 更新指定智能体的版本。
     *
     * @param agentId 智能体业务标识。
     * @param versionId 版本数据库标识。
     * @param request 版本更新数据。
     * @return 更新后的版本摘要。
     */
    @Operation(summary = "Update Agent Version")
    @SaCheckPermission("agent:admin:edit")
    @PutMapping("/{versionId}")
    public Result<AgentVersionVO> updateVersion(
            @PathVariable("agentId") String agentId,
            @PathVariable("versionId") Long versionId,
            @Valid @RequestBody VersionRequest request) {
        try {
            return Result.success(
                    agentVersionService.updateVersion(actor(), agentId, versionId, request));
        } catch (Exception exception) {
            log.error("修改版本失败", exception);
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除指定智能体的版本。
     *
     * @param agentId 智能体业务标识。
     * @param versionId 版本数据库标识。
     * @return 删除结果。
     */
    @Operation(summary = "Delete Agent Version")
    @SaCheckPermission("agent:admin:delete")
    @DeleteMapping("/{versionId}")
    public Result<Void> deleteVersion(
            @PathVariable("agentId") String agentId,
            @PathVariable("versionId") Long versionId) {
        try {
            agentVersionService.deleteVersion(actor(), agentId, versionId);
            return Result.success();
        } catch (Exception exception) {
            log.error("删除版本失败", exception);
            return Result.fail("删除失败");
        }
    }

    private ActorContext actor() {
        return ActorContextHttpAdapter.currentActor();
    }
}
