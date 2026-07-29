package com.shiyu.ai.auth.service;

import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.common.core.api.PageData;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 获取角色列表 - 分页
     */
    PageData<RoleVO> getRoleList(Number pageNum, Number pageSize, String name);

    /**
     * 获取角色列表-all
     */
    List<RoleBO> getAllRoles(String status);

    RoleBO getRoleDetail(Long id, Long scopedTenantId);

    /**
     * 修改角色
     */
    boolean updateRole(Long id, RoleBO roleBO);

    /**
     * 替换当前租户作用域下的角色菜单。
     */
    boolean replaceRoleMenus(Long id, Long scopedTenantId, List<Long> menuIds);

    /**
     * 删除角色
     */
    boolean deleteRole(Long id);

    /**
     * 从指定租户作用域批量移除角色。
     */
    boolean removeUserRoles(Long id, Long scopedTenantId, List<Long> userIds);

    /**
     * 向指定租户作用域批量分配角色。
     */
    boolean assignUserRoles(Long id, Long scopedTenantId, List<Long> userIds);

    /**
     * 新增角色
     */
    boolean createRole(RoleBO roleBO);
}
