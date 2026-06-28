package com.shiyu.ai.aiagent.controller;

import com.shiyu.ai.aiagent.service.AgentVersionService;
import com.shiyu.ai.model.request.VersionRequest;
import com.shiyu.ai.model.vo.AgentVersionDetailVO;
import com.shiyu.ai.model.vo.AgentVersionVO;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/agent/{agentId}/version")
public class AgentVersionController {

    private final AgentVersionService agentVersionService;

    public AgentVersionController(AgentVersionService agentVersionService) {
        this.agentVersionService = agentVersionService;
    }

    @GetMapping
    public Result<List<AgentVersionVO>> getVersions(@PathVariable String agentId) {
        List<AgentVersionVO> versions = agentVersionService.getVersions(agentId);
        return Result.success(versions);
    }

    @GetMapping("/{versionId}")
    public Result<AgentVersionDetailVO> getVersionDetail(
            @PathVariable String agentId, @PathVariable Long versionId) {
        AgentVersionDetailVO vo = agentVersionService.getVersionDetail(agentId, versionId);
        if (vo == null) return Result.fail("版本不存在");
        return Result.success(vo);
    }

    @PostMapping
    public Result<AgentVersionVO> createVersion(
            @PathVariable String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentVersionService.createVersion(agentId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("新增版本失败", e);
            return Result.fail("新增失败");
        }
    }

    @PatchMapping("/{versionId}")
    public Result<AgentVersionVO> updateVersion(
            @PathVariable String agentId, @PathVariable Long versionId,
            @Valid @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentVersionService.updateVersion(agentId, versionId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("修改版本失败", e);
            return Result.fail("修改失败");
        }
    }

    @DeleteMapping("/{versionId}")
    public Result<Void> deleteVersion(
            @PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentVersionService.deleteVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除版本失败", e);
            return Result.fail("删除失败");
        }
    }

    @PostMapping("/{versionId}/publish")
    public Result<Void> publish(@PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentVersionService.publishVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("发布版本失败", e);
            return Result.fail("发布失败");
        }
    }

    @PostMapping("/{versionId}/archive")
    public Result<Void> archive(@PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentVersionService.archiveVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("归档版本失败", e);
            return Result.fail("归档失败");
        }
    }

    @PostMapping("/{versionId}/activate")
    public Result<Void> activate(@PathVariable String agentId, @PathVariable Long versionId) {
        try {
            agentVersionService.activateVersion(agentId, versionId);
            return Result.success();
        } catch (Exception e) {
            log.error("激活版本失败", e);
            return Result.fail("激活失败");
        }
    }

    @PostMapping("/{versionId}/copy")
    public Result<AgentVersionVO> copy(@PathVariable String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentVersionService.copyVersion(agentId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("复制版本失败", e);
            return Result.fail("复制失败");
        }
    }
}
