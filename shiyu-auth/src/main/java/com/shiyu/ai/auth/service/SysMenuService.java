package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysMenuBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 菜单服务层
 *
 * @author shiyu-ai
 */
public interface SysMenuService {

    /**
     * 分页查询菜单列表
     *
     * @param pageNumber 页码
     * @param pageSize   每页数量
     * @return 菜单列表
     */
    Pair<Long, List<SysMenuBO>> getAll(Number pageNumber, Number pageSize);

    /**
     * 根据 ID 查询菜单
     *
     * @param menuId 菜单 ID
     * @return 菜单信息
     */
    SysMenuBO getById(Long menuId);

    /**
     * 创建菜单
     *
     * @param sysMenuBO 菜单信息
     * @return 创建后的菜单信息
     */
    SysMenuBO create(SysMenuBO sysMenuBO);

    /**
     * 更新菜单
     *
     * @param sysMenuBO 菜单信息
     * @return 更新后的菜单信息
     */
    SysMenuBO update(SysMenuBO sysMenuBO);

    /**
     * 删除菜单
     *
     * @param menuId 菜单 ID
     */
    void deleteById(Long menuId);
}
