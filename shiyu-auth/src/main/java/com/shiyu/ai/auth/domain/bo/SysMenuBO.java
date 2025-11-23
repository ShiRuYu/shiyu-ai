package com.shiyu.ai.auth.domain.bo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shiyu.ai.common.core.utils.StreamUtils;
import com.shiyu.ai.common.core.utils.TreeBuildUtils;
import com.shiyu.ai.common.core.validate.AddGroup;
import com.shiyu.ai.common.core.validate.EditGroup;
import com.shiyu.ai.common.mybatis.core.domain.BaseEntity;
import com.shiyu.ai.auth.domain.SysMenuDO;
import com.shiyu.ai.auth.domain.vo.SysMenuVO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限业务对象 sys_menu
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysMenuDO.class, reverseConvertGenerate = false)
public class SysMenuBO extends BaseEntity {

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空", groups = { EditGroup.class })
    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空", groups = { AddGroup.class, EditGroup.class })
    @Size(min = 0, max = 50, message = "菜单名称长度不能超过{max}个字符")
    private String menuName;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer orderNum;

    /**
     * 路由地址
     */
    @Size(min = 0, max = 200, message = "路由地址不能超过{max}个字符")
    private String path;

    /**
     * 组件路径
     */
    @Size(min = 0, max = 200, message = "组件路径不能超过{max}个字符")
    private String component;

    /**
     * 路由参数
     */
    private String queryParam;

    /**
     * 是否为外链（0是 1否）
     */
    private String isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    private String isCache;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    @NotBlank(message = "菜单类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String menuType;

    /**
     * 显示状态（0显示 1隐藏）
     */
    private String visible;

    /**
     * 菜单状态（0正常 1停用）
     */
    private String status;

    /**
     * 权限标识
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(min = 0, max = 100, message = "权限标识长度不能超过{max}个字符")
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 备注
     */
    private String remark;

    /**
     * 父菜单名称
     */
    private String parentName;

    /**
     * 子菜单
     */
    private List<SysMenuBO> children = new ArrayList<>();

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
/*
    public List<RouterVO> buildMenus(List<SysMenuDO> menus) {
        List<RouterVO> routers = new LinkedList<>();
        for (SysMenuDO menu : menus) {
            RouterVO router = new RouterVO();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(menu.getRouteName());
            router.setPath(menu.getRouterPath());
            router.setComponent(menu.getComponentInfo());
            router.setQuery(menu.getQueryParam());
            router.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon(), menu.getCache(), menu.getPath()));
            List<SysMenuDO> cMenus = menu.getChildren();
            if (CollUtil.isNotEmpty(cMenus) && menu.getMenuType().isDirectory()) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            } else if (menu.isMenuFrame()) {
                router.setMeta(null);
                List<RouterVO> childrenList = new ArrayList<>();
                RouterVO children = new RouterVO();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponentInfo());
                children.setName(StringUtils.capitalize(menu.getPath()));
                children.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon(), menu.getCache(), menu.getPath()));
                children.setQuery(menu.getQueryParam());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().intValue() == 0 && menu.isInnerLink()) {
                router.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterVO> childrenList = new ArrayList<>();
                RouterVO children = new RouterVO();
                String routerPath = SysMenuDO.innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(ComponentTypeEnum.INNER_LINK.getValue());
                children.setName(StringUtils.capitalize(routerPath));
                children.setMeta(new MetaVO(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }
*/

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    public List<Tree<Long>> buildMenuTreeSelect(List<SysMenuVO> menus) {
        if (CollUtil.isEmpty(menus)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.build(menus, (menu, tree) ->
                tree.setId(menu.getMenuId())
                        .setParentId(menu.getParentId())
                        .setName(menu.getMenuName())
                        .setWeight(menu.getOrderNum()));
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    private List<SysMenuBO> getChildPerms(List<SysMenuBO> list, int parentId) {
        List<SysMenuBO> returnList = new ArrayList<>();
        for (SysMenuBO t : list) {
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysMenuBO> list, SysMenuBO t) {
        // 得到子节点列表
        List<SysMenuBO> childList = StreamUtils.filter(list, n -> n.getParentId().equals(t.getMenuId()));
        t.setChildren(childList);
        for (SysMenuBO tChild : childList) {
            // 判断是否有子节点
            if (list.stream().anyMatch(n -> n.getParentId().equals(tChild.getMenuId()))) {
                recursionFn(list, tChild);
            }
        }
    }
}
