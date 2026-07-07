package com.shiyu.ai.dal.repository.auth;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.auth.WorkspaceDO;
import com.shiyu.ai.dal.mapper.auth.WorkspaceMapper;
import com.shiyu.ai.auth.bo.WorkspaceBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工作空间数据仓储层
 */
@Component
public class WorkspaceRepository {

    @Resource
    private WorkspaceMapper workspaceMapper;

    /**
     * 查询所有工作空间
     */
    public List<WorkspaceBO> selectAll(String name) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(WorkspaceDO::getDelFlag).eq(0);
        if (name != null && !name.isEmpty()) {
            queryWrapper.and(WorkspaceDO::getName).like(name);
        }
        queryWrapper.orderBy(WorkspaceDO::getOrder, true);
        List<WorkspaceDO> workspaceDOs = workspaceMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(workspaceDOs, WorkspaceBO.class);
    }

    /**
     * 根据ID查询工作空间
     */
    public WorkspaceBO selectById(Long id) {
        WorkspaceDO workspaceDO = workspaceMapper.selectOneById(id);
        return MapstructUtils.convert(workspaceDO, WorkspaceBO.class);
    }

    /**
     * 新增工作空间
     */
    public WorkspaceBO insert(WorkspaceBO workspaceBO) {
        WorkspaceDO workspaceDO = MapstructUtils.convert(workspaceBO, WorkspaceDO.class);
        workspaceDO.setDelFlag(0);
        if (workspaceDO.getOrder() == null) {
            workspaceDO.setOrder(0);
        }
        workspaceMapper.insertSelective(workspaceDO);
        workspaceBO.setId(workspaceDO.getId());
        return workspaceBO;
    }

    /**
     * 更新工作空间
     */
    public boolean update(WorkspaceBO workspaceBO) {
        WorkspaceDO workspaceDO = MapstructUtils.convert(workspaceBO, WorkspaceDO.class);
        return workspaceMapper.update(workspaceDO) > 0;
    }

    /**
     * 删除工作空间（逻辑删除）
     */
    public boolean softDelete(Long id) {
        WorkspaceBO bo = new WorkspaceBO();
        bo.setId(id);
        bo.setDelFlag(1);
        return update(bo);
    }
}
