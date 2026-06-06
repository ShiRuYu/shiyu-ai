package com.shiyu.ai.agent.biz.auth.service;

import com.shiyu.ai.agent.domain.bo.DeptBO;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DeptService {

    /**
     * 获取部门列表（树形）
     *
     * @param name 部门名称（可选，用于过滤）
     * @return 部门树形列表
     */
    List<DeptBO> getDeptList(String name);

    /**
     * 根据 ID 获取部门
     *
     * @param id 部门 ID
     * @return 部门信息
     */
    DeptBO getById(Long id);

    /**
     * 新增部门
     *
     * @param deptBO 部门信息
     * @return 是否成功
     */
    boolean createDept(DeptBO deptBO);

    /**
     * 修改部门
     *
     * @param id     部门 ID
     * @param deptBO 部门信息
     * @return 是否成功
     */
    boolean updateDept(Long id, DeptBO deptBO);

    /**
     * 删除部门
     *
     * @param id 部门 ID
     * @return 是否成功
     */
    boolean deleteDept(Long id);
}
