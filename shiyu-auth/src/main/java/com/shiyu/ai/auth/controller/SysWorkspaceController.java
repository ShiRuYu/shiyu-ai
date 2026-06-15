package com.shiyu.ai.auth.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.auth.domain.bo.SysWorkspaceBO;
import com.shiyu.ai.auth.service.SysWorkspaceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作空间管理控制器
 *
 * @author shiyu-ai
 */
@Tag(name = "工作空间管理", description = "工作空间管理接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class SysWorkspaceController {

    private final SysWorkspaceService sysWorkspaceService;

    /**
     * 获取工作空间列表
     */
    @GetMapping
    public Result<Pair<Long, List<SysWorkspaceBO>>> getWorkspaces(@RequestParam(defaultValue = "1") Number pageNumber,
                                                                   @RequestParam(defaultValue = "10") Number pageSize) {
        return Result.success(sysWorkspaceService.getAll(pageNumber, pageSize));
    }

    /**
     * 获取工作空间详情
     */
    @GetMapping("/{workspaceId}")
    public Result<SysWorkspaceBO> getWorkspace(@PathVariable Long workspaceId) {
        return Result.success(sysWorkspaceService.getById(workspaceId));
    }

    /**
     * 创建工作空间
     */
    @PostMapping
    public Result<SysWorkspaceBO> createWorkspace(@Valid @RequestBody SysWorkspaceBO sysWorkspaceBO) {
        return Result.success(sysWorkspaceService.create(sysWorkspaceBO));
    }

    /**
     * 更新工作空间
     */
    @PutMapping("/{workspaceId}")
    public Result<SysWorkspaceBO> updateWorkspace(@PathVariable Long workspaceId, @Valid @RequestBody SysWorkspaceBO sysWorkspaceBO) {
        sysWorkspaceBO.setWorkspaceId(workspaceId);
        return Result.success(sysWorkspaceService.update(sysWorkspaceBO));
    }

    /**
     * 删除工作空间
     */
    @DeleteMapping("/{workspaceId}")
    public Result<Void> deleteWorkspace(@PathVariable Long workspaceId) {
        sysWorkspaceService.deleteById(workspaceId);
        return Result.success();
    }
}
