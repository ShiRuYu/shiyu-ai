package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.service.WorkspaceService;
import com.shiyu.ai.dal.auth.bo.WorkspaceBO;
import com.shiyu.ai.auth.request.WorkspaceRequest;
import com.shiyu.ai.auth.vo.WorkspaceVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作空间管理 Controller
 */
@Slf4j
@Tag(name = "Workspace", description = "Workspace")
@RestController
@RequestMapping("/auth/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 获取工作空间列表（树形），平铺转换避免循环引用导致的 StackOverflowError
     */
    @Operation(summary = "Get Workspace List")
    @GetMapping("/list")
    public Result<List<WorkspaceVO>> getWorkspaceList(
            @RequestParam(required = false) String name) {
        log.info("获取工作空间列表，name: {}", name);

        List<WorkspaceBO> workspaceBOs = workspaceService.getWorkspaceList(name);

        // 先平铺树 -> 清空 children 避免递归转换导致循环引用
        List<WorkspaceBO> flatBos = flattenBos(workspaceBOs);
        List<WorkspaceVO> flatVos = MapstructUtils.convert(flatBos, WorkspaceVO.class);

        // 从扁平 VO 列表重建树形结构
        List<WorkspaceVO> tree = buildVOTree(flatVos);

        return Result.success(tree);
    }

    /**
     * 新增工作空间
     */
    @Operation(summary = "Create Workspace")
    @PostMapping("/create")
    public Result<Void> createWorkspace(@Valid @RequestBody WorkspaceRequest request) {
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
    @Operation(summary = "Update Workspace")
    @PostMapping("/update")
    public Result<Void> updateWorkspace(
            @RequestParam Long id,
            @Valid @RequestBody WorkspaceRequest request) {
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
    @Operation(summary = "Delete Workspace")
    @PostMapping("/delete")
    public Result<Void> deleteWorkspace(@RequestParam Long id) {
        log.info("删除工作空间，id: {}", id);

        boolean success = workspaceService.deleteWorkspace(id);

        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除失败，可能存在子工作空间");
        }
    }

    /**
     * 将树形 BO 列表平铺为扁平列表，同时清除 children 以避免递归转换
     */
    private List<WorkspaceBO> flattenBos(List<WorkspaceBO> bos) {
        List<WorkspaceBO> flat = new ArrayList<>();
        flattenBosRecursive(bos, flat);
        return flat;
    }

    private void flattenBosRecursive(List<WorkspaceBO> nodes, List<WorkspaceBO> result) {
        if (nodes == null) {
            return;
        }
        for (WorkspaceBO node : nodes) {
            // 先保存 children 引用，再清空
            List<WorkspaceBO> children = node.getChildren();
            node.setChildren(null);
            result.add(node);
            // 递归处理子节点
            if (children != null) {
                flattenBosRecursive(children, result);
            }
        }
    }

    /**
     * 从扁平 VO 列表重建树形结构（基于 parentId 字段），
     * 父节点为 0L 或 null 的作为根节点
     */
    private List<WorkspaceVO> buildVOTree(List<WorkspaceVO> flatVos) {
        if (flatVos == null || flatVos.isEmpty()) {
            return new ArrayList<>();
        }

        // 建立 id -> VO 映射
        Map<Long, WorkspaceVO> voMap = new HashMap<>();
        for (WorkspaceVO vo : flatVos) {
            vo.setChildren(new ArrayList<>());
            voMap.put(vo.getId(), vo);
        }

        // 按 parentId 挂载子节点，同时收集根节点
        List<WorkspaceVO> roots = new ArrayList<>();
        for (WorkspaceVO vo : flatVos) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0L) {
                roots.add(vo);
            } else {
                WorkspaceVO parent = voMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // parentId 指向不存在的节点，降级为根节点
                    log.warn("工作空间 {} 的 parentId={} 不存在，降级为根节点", vo.getId(), parentId);
                    roots.add(vo);
                }
            }
        }

        return roots;
    }

}
