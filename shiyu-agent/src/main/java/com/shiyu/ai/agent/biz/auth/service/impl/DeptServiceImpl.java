package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.DeptRepository;
import com.shiyu.ai.agent.biz.auth.service.DeptService;
import com.shiyu.ai.agent.domain.bo.DeptBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 */
@Slf4j
@Service
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;

    public DeptServiceImpl(DeptRepository deptRepository) {
        this.deptRepository = deptRepository;
    }

    @Override
    public List<DeptBO> getDeptList(String name) {
        log.info("获取部门列表，name: {}", name);
        List<DeptBO> allDepts = deptRepository.selectAll(name);
        return buildTree(allDepts);
    }

    @Override
    public DeptBO getById(Long id) {
        log.info("获取部门，id: {}", id);
        return deptRepository.selectById(id);
    }

    @Override
    public boolean createDept(DeptBO deptBO) {
        log.info("新增部门，name: {}", deptBO.getName());
        if (deptBO.getParentId() == null) {
            deptBO.setParentId(0L);
        }
        deptRepository.insert(deptBO);
        return true;
    }

    @Override
    public boolean updateDept(Long id, DeptBO deptBO) {
        log.info("修改部门，id: {}", id);
        DeptBO existing = deptRepository.selectById(id);
        if (existing == null) {
            return false;
        }
        deptBO.setId(id);
        return deptRepository.update(deptBO);
    }

    @Override
    public boolean deleteDept(Long id) {
        log.info("删除部门，id: {}", id);
        List<DeptBO> allDepts = deptRepository.selectAll(null);
        boolean hasChildren = allDepts.stream()
                .anyMatch(d -> id.equals(d.getParentId()));
        if (hasChildren) {
            log.warn("部门 {} 存在子部门，不能删除", id);
            return false;
        }
        return deptRepository.softDelete(id);
    }

    /**
     * 将平铺列表构建为树形结构
     */
    private List<DeptBO> buildTree(List<DeptBO> depts) {
        if (depts == null || depts.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<DeptBO>> grouped = depts.stream()
                .collect(Collectors.groupingBy(d ->
                        d.getParentId() == null ? 0L : d.getParentId()));

        for (DeptBO dept : depts) {
            List<DeptBO> children = grouped.get(dept.getId());
            dept.setChildren(children != null ? children : new ArrayList<>());
        }

        return grouped.getOrDefault(0L, new ArrayList<>());
    }
}
