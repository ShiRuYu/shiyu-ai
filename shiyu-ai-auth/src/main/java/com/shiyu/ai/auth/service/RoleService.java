package com.shiyu.ai.auth.service;

import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.auth.vo.RolePageResponse;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 获取角色列表 - 分页
     */
    RolePageResponse getRoleList(Number pageNo, Number pageSize, String name);

    /**
     * 获取角色列表-all
     */
    List<RoleBO> getAllRoles(String status);

    RoleBO getRoleDetail(Long id);

    /**
     * 修改角色
     */
    boolean updateRole(Long id, RoleBO roleBO);

    /**
     * 替换当前租户作用域下的角色菜单。
     */
    boolean replaceRoleMenus(Long id, List<Long> menuIds);

    /**
     * 删除角色
     */
    boolean deleteRole(Long id);

    /**
     * 取消分配角色 - 批量（从当前工作空间移除角色）
     */
    boolean removeUserRoles(Long id, List<Long> userIds);

    /**
     * 分配角色 - 批量（在当前工作空间分配角色）
     */
    boolean assignUserRoles(Long id, List<Long> userIds);

    /**
     * 新增角色
     */
    boolean createRole(RoleBO roleBO);
}
