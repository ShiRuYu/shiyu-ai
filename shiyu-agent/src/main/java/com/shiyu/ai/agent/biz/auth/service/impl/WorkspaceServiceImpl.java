package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.WorkspaceRepository;
import com.shiyu.ai.agent.biz.auth.service.WorkspaceService;
import com.shiyu.ai.agent.domain.bo.WorkspaceBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * 将平铺列表构建为树形结构
     */
    private List<WorkspaceBO> buildTree(List<WorkspaceBO> workspaces) {
        if (workspaces == null || workspaces.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<WorkspaceBO>> grouped = workspaces.stream()
                .collect(Collectors.groupingBy(d ->
                        d.getParentId() == null ? 0L : d.getParentId()));

        for (WorkspaceBO workspace : workspaces) {
            List<WorkspaceBO> children = grouped.get(workspace.getId());
            workspace.setChildren(children != null ? children : new ArrayList<>());
        }

        return grouped.getOrDefault(0L, new ArrayList<>());
    }
}
