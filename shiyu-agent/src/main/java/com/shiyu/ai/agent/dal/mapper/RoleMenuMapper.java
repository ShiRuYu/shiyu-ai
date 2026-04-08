package com.shiyu.ai.agent.dal.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.MenuDO;
import com.shiyu.ai.agent.dal.dataobject.RoleMenuDO;
import com.shiyu.ai.common.mybatis.core.mapper.BaseMapperFlex;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色菜单关联表 数据层
 */
@Mapper
public interface RoleMenuMapper extends BaseMapperFlex<RoleMenuDO> {

    /**
     * 根据角色 ID 查询菜单列表
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    default List<MenuDO> selectMenusByRoleId(Long roleId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(
                        "m.id",
                        "m.name",
                        "m.code",
                        "m.type",
                        "m.parent_id",
                        "m.path",
                        "m.redirect",
                        "m.icon",
                        "m.component",
                        "m.layout",
                        "m.keep_alive",
                        "m.method",
                        "m.description",
                        "m.show",
                        "m.enable",
                        "m.`order`",
                        "m.del_flag",
                        "m.create_time",
                        "m.update_time"
                )
                .from("menu").as("m")
                .innerJoin("role_menu").as("rm").on("m.id = rm.menu_id")
                .where("rm.role_id = ?", roleId)
                .and("m.enable = 1")
                .and("m.del_flag = 0")
                .orderBy("m.`order`", true)
                .orderBy("m.id", true);
        
        return selectListByQueryAs(queryWrapper, MenuDO.class);
    }

    /**
     * 为角色分配菜单
     * @param roleId 角色 ID
     * @param menuId 菜单 ID
     * @return 影响行数
     */
    default int insertRoleMenu(Long roleId, Long menuId) {
        RoleMenuDO roleMenu = new RoleMenuDO();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        return insert(roleMenu);
    }

    /**
     * 取消角色的菜单
     * @param roleId 角色 ID
     * @param menuId 菜单 ID
     * @return 影响行数
     */
    default int deleteRoleMenu(Long roleId, Long menuId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(RoleMenuDO::getRoleId).eq(roleId)
                .and(RoleMenuDO::getMenuId).eq(menuId);
        return deleteByQuery(queryWrapper);
    }

    /**
     * 删除角色的所有菜单
     * @param roleId 角色 ID
     * @return 影响行数
     */
    default int deleteMenusByRoleId(Long roleId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(RoleMenuDO::getRoleId).eq(roleId);
        return deleteByQuery(queryWrapper);
    }
}
