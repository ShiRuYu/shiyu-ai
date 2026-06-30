package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.repository.WorkspaceRepository;
import com.shiyu.ai.auth.service.WorkspaceService;
import com.shiyu.ai.auth.bo.WorkspaceBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作空间服务实现类
 */
@Slf4j
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public List<WorkspaceBO> getWorkspaceList(String name) {
        log.info("获取工作空间列表，name: {}", name);
        List<WorkspaceBO> allWorkspaces = workspaceRepository.selectAll(name);
        return buildTree(allWorkspaces);
    }

    @Override
    public WorkspaceBO getById(Long id) {
        log.info("获取工作空间，id: {}", id);
        return workspaceRepository.selectById(id);
    }

    @Override
    public boolean createWorkspace(WorkspaceBO workspaceBO) {
        log.info("新增工作空间，name: {}", workspaceBO.getName());
        if (workspaceBO.getParentId() == null) {
            workspaceBO.setParentId(0L);
        }
        workspaceRepository.insert(workspaceBO);
        return true;
    }

    @Override
    public boolean updateWorkspace(Long id, WorkspaceBO workspaceBO) {
        log.info("修改工作空间，id: {}", id);
        WorkspaceBO existing = workspaceRepository.selectById(id);
        if (existing == null) {
            return false;
        }
        workspaceBO.setId(id);
        return workspaceRepository.update(workspaceBO);
    }

    @Override
    public boolean deleteWorkspace(Long id) {
        log.info("删除工作空间，id: {}", id);
        List<WorkspaceBO> allWorkspaces = workspaceRepository.selectAll(null);
        boolean hasChildren = allWorkspaces.stream()
                .anyMatch(d -> id.equals(d.getParentId()));
        if (hasChildren) {
            log.warn("工作空间 {} 存在子工作空间，不能删除", id);
            return false;
        }
        return workspaceRepository.softDelete(id);
    }

    /**
     * 将平铺列表构造为树形结构（含循环引用防护）
     */
    private List<WorkspaceBO> buildTree(List<WorkspaceBO> workspaces) {
        if (workspaces == null || workspaces.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<WorkspaceBO>> grouped = workspaces.stream()
                .collect(Collectors.groupingBy(d ->
                        d.getParentId() == null ? 0L : d.getParentId()));

        Set<Long> allIds = workspaces.stream()
                .map(WorkspaceBO::getId)
                .collect(Collectors.toSet());

        for (WorkspaceBO workspace : workspaces) {
            Long id = workspace.getId();
            List<WorkspaceBO> children = grouped.get(id);
            if (children != null) {
                // 过滤掉自身引用（parentId == id）和不存在的节点，防止循环引用
                workspace.setChildren(
                        children.stream()
                                .filter(c -> allIds.contains(c.getId()) && !c.getId().equals(id))
                                .collect(Collectors.toList())
                );
            } else {
                workspace.setChildren(new ArrayList<>());
            }
        }

        return grouped.getOrDefault(0L, new ArrayList<>());
    }
}
