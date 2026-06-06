package com.shiyu.ai.agent.biz.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.auth.DeptDO;
import com.shiyu.ai.agent.dal.mapper.auth.DeptMapper;
import com.shiyu.ai.agent.domain.bo.DeptBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 部门数据仓储层
 */
@Component
public class DeptRepository {

    @Resource
    private DeptMapper deptMapper;

    /**
     * 查询所有部门
     */
    public List<DeptBO> selectAll(String name) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(DeptDO::getDelFlag).eq(0);
        if (name != null && !name.isEmpty()) {
            queryWrapper.and(DeptDO::getName).like(name);
        }
        queryWrapper.orderBy(DeptDO::getOrder, true);
        List<DeptDO> deptDOs = deptMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(deptDOs, DeptBO.class);
    }

    /**
     * 根据ID查询部门
     */
    public DeptBO selectById(Long id) {
        DeptDO deptDO = deptMapper.selectOneById(id);
        return MapstructUtils.convert(deptDO, DeptBO.class);
    }

    /**
     * 新增部门
     */
    public DeptBO insert(DeptBO deptBO) {
        DeptDO deptDO = MapstructUtils.convert(deptBO, DeptDO.class);
        deptDO.setDelFlag(0);
        if (deptDO.getOrder() == null) {
            deptDO.setOrder(0);
        }
        deptMapper.insertSelective(deptDO);
        deptBO.setId(deptDO.getId());
        return deptBO;
    }

    /**
     * 更新部门
     */
    public boolean update(DeptBO deptBO) {
        DeptDO deptDO = MapstructUtils.convert(deptBO, DeptDO.class);
        return deptMapper.update(deptDO) > 0;
    }

    /**
     * 删除部门（逻辑删除）
     */
    public boolean softDelete(Long id) {
        DeptBO bo = new DeptBO();
        bo.setId(id);
        bo.setDelFlag(1);
        return update(bo);
    }
}
