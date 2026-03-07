package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysRoleBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 角色服务层
 *
 * @author shiyu-ai
 */
public interface SysRoleService {

    /**
     * 分页查询角色列表
     *
     * @param pageNumber 页码
     * @param pageSize   每页数量
     * @return 角色列表
     */
    Pair<Long, List<SysRoleBO>> getAll(Number pageNumber, Number pageSize);

    /**
     * 根据 ID 查询角色
     *
     * @param roleId 角色 ID
     * @return 角色信息
     */
    SysRoleBO getById(Long roleId);

    /**
     * 创建角色
     *
     * @param sysRoleBO 角色信息
     * @return 创建后的角色信息
     */
    SysRoleBO create(SysRoleBO sysRoleBO);

    /**
     * 更新角色
     *
     * @param sysRoleBO 角色信息
     * @return 更新后的角色信息
     */
    SysRoleBO update(SysRoleBO sysRoleBO);

    /**
     * 删除角色
     *
     * @param roleId 角色 ID
     */
    void deleteById(Long roleId);
}
