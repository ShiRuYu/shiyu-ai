package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysDeptBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 部门服务层
 *
 * @author shiyu-ai
 */
public interface SysDeptService {

    /**
     * 分页查询部门列表
     *
     * @param pageNumber 页码
     * @param pageSize   每页数量
     * @return 部门列表
     */
    Pair<Long, List<SysDeptBO>> getAll(Number pageNumber, Number pageSize);

    /**
     * 根据 ID 查询部门
     *
     * @param deptId 部门 ID
     * @return 部门信息
     */
    SysDeptBO getById(Long deptId);

    /**
     * 创建部门
     *
     * @param sysDeptBO 部门信息
     * @return 创建后的部门信息
     */
    SysDeptBO create(SysDeptBO sysDeptBO);

    /**
     * 更新部门
     *
     * @param sysDeptBO 部门信息
     * @return 更新后的部门信息
     */
    SysDeptBO update(SysDeptBO sysDeptBO);

    /**
     * 删除部门
     *
     * @param deptId 部门 ID
     */
    void deleteById(Long deptId);
}
