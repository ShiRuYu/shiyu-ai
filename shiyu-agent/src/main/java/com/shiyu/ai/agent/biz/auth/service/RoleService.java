package com.shiyu.ai.agent.biz.auth.service;

import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.vo.RolePageResponse;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 获取角色列表 - 分页
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @param name     角色名称（可选）
     * @return 分页数据
     */
    RolePageResponse getRoleList(Number pageNo, Number pageSize, String name);

    /**
     * 获取角色列表-all
     *
     * @param status 状态（可选，1正常 0停用）
     * @return 角色列表
     */
    List<RoleBO> getAllRoles(String status);

    /**
     * 修改角色
     *
     * @param id      角色 ID
     * @param roleBO  角色信息
     * @return 是否成功
     */
    boolean updateRole(Long id, RoleBO roleBO);

    /**
     * 删除角色
     *
     * @param id 角色 ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);

    /**
     * 取消分配角色 - 批量
     *
     * @param id      角色 ID
     * @param userIds 用户 ID 列表
     * @return 是否成功
     */
    boolean removeUserRoles(Long id, List<Long> userIds);

    /**
     * 分配角色 - 批量
     *
     * @param id      角色 ID
     * @param userIds 用户 ID 列表
     * @return 是否成功
     */
    boolean assignUserRoles(Long id, List<Long> userIds);

    /**
     * 新增角色
     *
     * @param roleBO 角色信息
     * @return 是否成功
     */
    boolean createRole(RoleBO roleBO);
}
