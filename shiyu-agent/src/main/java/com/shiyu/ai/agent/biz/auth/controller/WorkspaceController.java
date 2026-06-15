package com.shiyu.ai.agent.biz.auth.controller;

import com.shiyu.ai.agent.biz.auth.service.WorkspaceService;
import com.shiyu.ai.agent.domain.bo.WorkspaceBO;
import com.shiyu.ai.agent.domain.request.WorkspaceRequest;
import com.shiyu.ai.agent.domain.vo.WorkspaceVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作空间管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 获取工作空间列表（树形）
     */
    @GetMapping("/list")
    public Result<List<WorkspaceVO>> getWorkspaceList(
            @RequestParam(required = false) String name) {
        log.info("获取工作空间列表，name: {}", name);

        List<WorkspaceBO> workspaceBOs = workspaceService.getWorkspaceList(name);
        List<WorkspaceVO> workspaceVOs = MapstructUtils.convert(workspaceBOs, WorkspaceVO.class);

        return Result.success(workspaceVOs);
    }

    /**
     * 新增工作空间
     */
    @PostMapping("")
    public Result<Void> createWorkspace(@RequestBody WorkspaceRequest request) {
        log.info("新增工作空间，name: {}", request.getName());

        WorkspaceBO workspaceBO = MapstructUtils.convert(request, WorkspaceBO.class);
        boolean success = workspaceService.createWorkspace(workspaceBO);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("新增失败");
        }
    }

    /**
     * 修改工作空间
     */
    @PatchMapping("/{id}")
    public Result<Void> updateWorkspace(
            @PathVariable Long id,
            @RequestBody WorkspaceRequest request) {
        log.info("修改工作空间，id: {}", id);

        WorkspaceBO workspaceBO = MapstructUtils.convert(request, WorkspaceBO.class);
        boolean success = workspaceService.updateWorkspace(id, workspaceBO);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("修改失败");
        }
    }

    /**
     * 删除工作空间
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteWorkspace(@PathVariable Long id) {
        log.info("删除工作空间，id: {}", id);

        boolean success = workspaceService.deleteWorkspace(id);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除失败，可能存在子工作空间");
        }
    }
}
