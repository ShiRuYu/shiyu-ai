package com.shiyu.ai.agent.biz.agent.controller;

import com.shiyu.ai.agent.biz.agent.service.AgentAdminService;
import com.shiyu.ai.agent.domain.request.GraphConfigRequest;
import com.shiyu.ai.agent.domain.request.VersionRequest;
import com.shiyu.ai.agent.domain.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.domain.vo.AgentVersionVO;
import com.shiyu.ai.agent.domain.vo.GraphValidationVO;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/agent/{agentId}/version")
public class AgentVersionController {

    private final AgentAdminService agentAdminService;

    public AgentVersionController(AgentAdminService agentAdminService) {
        this.agentAdminService = agentAdminService;
    }

    @GetMapping
    public Result<List<AgentVersionVO>> getVersions(@PathVariable String agentId) {
        List<AgentVersionVO> versions = agentAdminService.getVersions(agentId);
        return Result.success(versions);
    }

    @GetMapping("/{versionId}")
    public Result<AgentVersionDetailVO> getVersionDetail(
            @PathVariable String agentId, @PathVariable Long versionId) {
        AgentVersionDetailVO vo = agentAdminService.getVersionDetail(agentId, versionId);
        if (vo == null) return Result.fail("版本不存在");
        return Result.success(vo);
    }

    @PostMapping
    public Result<AgentVersionVO> createVersion(
            @PathVariable String agentId, @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentAdminService.createVersion(agentId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("新增版本失败", e);
            return Result.fail("新增失败：" + e.getMessage());
        }
    }

    @PatchMapping("/{versionId}")
    public Result<AgentVersionVO> updateVersion(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentAdminService.updateVersion(agentId, versionId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("修改版本失败", e);
            return Result.fail("修改失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{versionId}")
    public Result<Void> deleteVersion(
            @PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentAdminService.deleteVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除版本失败", e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/{versionId}/publish")
    public Result<Void> publish(@PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentAdminService.publishVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("发布版本失败", e);
            return Result.fail("发布失败：" + e.getMessage());
        }
    }

    @PostMapping("/{versionId}/archive")
    public Result<Void> archive(@PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentAdminService.archiveVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("归档版本失败", e);
            return Result.fail("归档失败：" + e.getMessage());
        }
    }

    @PostMapping("/{versionId}/activate")
    public Result<Void> activate(@PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentAdminService.activateVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("激活版本失败", e);
            return Result.fail("激活失败：" + e.getMessage());
        }
    }

    @PostMapping("/{versionId}/copy")
    public Result<AgentVersionVO> copy(@PathVariable String agentId, @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentAdminService.copyVersion(agentId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("复制版本失败", e);
            return Result.fail("复制失败：" + e.getMessage());
        }
    }

    // ==================== Graph 编排接口 ====================

    @GetMapping("/{versionId}/graph")
    public Result<AgentVersionDetailVO> getGraphConfig(
            @PathVariable String agentId, @PathVariable Long versionId) {
        AgentVersionDetailVO vo = agentAdminService.getGraphConfig(agentId, versionId);
        if (vo == null) return Result.fail("版本不存在");
        return Result.success(vo);
    }

    @PutMapping("/{versionId}/graph")
    public Result<AgentVersionDetailVO> updateGraphConfig(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody GraphConfigRequest request) {
        try {
            AgentVersionDetailVO vo = agentAdminService.updateGraphConfig(agentId, versionId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("更新Graph配置失败", e);
            return Result.fail("更新Graph失败：" + e.getMessage());
        }
    }

    @PostMapping("/{versionId}/graph/validate")
    public Result<GraphValidationVO> validateGraphConfig(
            @PathVariable String agentId, @PathVariable Long versionId,
            @RequestBody GraphConfigRequest request) {
        GraphValidationVO result = agentAdminService.validateGraphConfig(request);
        return Result.success(result);
    }
}
