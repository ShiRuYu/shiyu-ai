package com.shiyu.ai.agent.biz.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.auth.MenuDO;
import com.shiyu.ai.agent.dal.mapper.auth.MenuMapper;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单数据仓储层
 */
@Component
public class MenuRepository {

    @Resource
    private MenuMapper menuMapper;

    /**
     * 查询所有菜单
     */
    public List<MenuBO> selectAll() {
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(new QueryWrapper());
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据类型查询菜单
     * @param type 菜单类型（MENU-菜单，BUTTON-按钮）
     * @return 菜单列表
     */
    public List<MenuBO> selectAllByType(String type) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(MenuDO::getType).eq(type);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 查询菜单（排除指定类型）
     * @param type 需要排除的菜单类型（如 BUTTON）
     * @return 菜单列表
     */
    public List<MenuBO> selectAllExcludingType(String type) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(MenuDO::getType).ne(type);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据ID查询菜单
     */
    public MenuBO selectById(Long id) {
        MenuDO menuDO = menuMapper.selectOneById(id);
        return MapstructUtils.convert(menuDO, MenuBO.class);
    }

    /**
     * 创建菜单
     */
    public MenuBO insert(MenuBO menuBO) {
        MenuDO menuDO = MapstructUtils.convert(menuBO, MenuDO.class);
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        menuMapper.insertSelective(menuDO);
        menuBO.setId(menuDO.getId());
        return menuBO;
    }

    /**
     * 更新菜单
     */
    public boolean update(MenuBO menuBO) {
        MenuDO menuDO = MapstructUtils.convert(menuBO, MenuDO.class);
        return menuMapper.update(menuDO) > 0;
    }

    /**
     * 删除菜单
     */
    public boolean deleteById(Long id) {
        return menuMapper.deleteById(id) > 0;
    }

}
