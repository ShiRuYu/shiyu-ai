package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.repository.WorkspaceRepository;
import com.shiyu.ai.auth.service.WorkspaceService;
import com.shiyu.ai.model.bo.WorkspaceBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 宸ヤ綔绌洪棿鏈嶅姟瀹炵幇绫?
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
        log.info("鑾峰彇宸ヤ綔绌洪棿鍒楄〃锛宯ame: {}", name);
        List<WorkspaceBO> allWorkspaces = workspaceRepository.selectAll(name);
        return buildTree(allWorkspaces);
    }

    @Override
    public WorkspaceBO getById(Long id) {
        log.info("鑾峰彇宸ヤ綔绌洪棿锛宨d: {}", id);
        return workspaceRepository.selectById(id);
    }

    @Override
    public boolean createWorkspace(WorkspaceBO workspaceBO) {
        log.info("鏂板宸ヤ綔绌洪棿锛宯ame: {}", workspaceBO.getName());
        if (workspaceBO.getParentId() == null) {
            workspaceBO.setParentId(0L);
        }
        workspaceRepository.insert(workspaceBO);
        return true;
    }

    @Override
    public boolean updateWorkspace(Long id, WorkspaceBO workspaceBO) {
        log.info("淇敼宸ヤ綔绌洪棿锛宨d: {}", id);
        WorkspaceBO existing = workspaceRepository.selectById(id);
        if (existing == null) {
            return false;
        }
        workspaceBO.setId(id);
        return workspaceRepository.update(workspaceBO);
    }

    @Override
    public boolean deleteWorkspace(Long id) {
        log.info("鍒犻櫎宸ヤ綔绌洪棿锛宨d: {}", id);
        List<WorkspaceBO> allWorkspaces = workspaceRepository.selectAll(null);
        boolean hasChildren = allWorkspaces.stream()
                .anyMatch(d -> id.equals(d.getParentId()));
        if (hasChildren) {
            log.warn("宸ヤ綔绌洪棿 {} 瀛樺湪瀛愬伐浣滅┖闂达紝涓嶈兘鍒犻櫎", id);
            return false;
        }
        return workspaceRepository.softDelete(id);
    }

    /**
     * 灏嗗钩閾哄垪琛ㄦ瀯閫犱负鏍戝舰缁撴瀯锛堝惈寰幆寮曠敤闃叉姢锛?
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
                // 杩囨护鎺夎嚜韬紩鐢紙parentId == id锛夊拰涓嶅瓨鍦ㄧ殑鑺傜偣锛岄槻姝㈠惊鐜紩鐢?
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
