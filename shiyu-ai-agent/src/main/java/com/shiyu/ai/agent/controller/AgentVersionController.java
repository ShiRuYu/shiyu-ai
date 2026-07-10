package com.shiyu.ai.agent.controller;

import com.shiyu.ai.agent.service.AgentVersionService;
import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.vo.AgentVersionVO;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Slf4j
@Tag(name = "Agent Version", description = "Agent Version")
@RestController
@RequestMapping("/agent/version")
public class AgentVersionController {

    private final AgentVersionService agentVersionService;

    public AgentVersionController(AgentVersionService agentVersionService) {
        this.agentVersionService = agentVersionService;
    }

    @Operation(summary = "Get Versions")
    @GetMapping("/list")
    public Result<List<AgentVersionVO>> getVersions(@RequestParam String agentId) {
        List<AgentVersionVO> versions = agentVersionService.getVersions(agentId);
        return Result.success(versions);
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
    @PostMapping("/create")
    public Result<AgentVersionVO> createVersion(
            @RequestParam String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentVersionService.createVersion(agentId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("新增版本失败", e);
            return Result.fail("新增失败");
        }
    }

    @PostMapping("/update")
    public Result<AgentVersionVO> updateVersion(
            @RequestParam String agentId, @RequestParam Long versionId,
            @Valid @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentVersionService.updateVersion(agentId, versionId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("修改版本失败", e);
            return Result.fail("修改失败");
        }
    }

    @Operation(summary = "Delete Version")
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

    @Operation(summary = "Publish")
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
    @PostMapping("/copy")
    public Result<AgentVersionVO> copy(@RequestParam String agentId, @Valid @RequestBody VersionRequest request) {
        try {
            AgentVersionVO vo = agentVersionService.copyVersion(agentId, request);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("复制版本失败", e);
            return Result.fail("复制失败");
        }
    }
}
