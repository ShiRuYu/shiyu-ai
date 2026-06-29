package com.shiyu.ai.auth.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 前端路由菜单视图对象 - 符合 API 文档规范
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouteMenuVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单 ID
     */
    private Long id;

    /**
     * 父菜单 ID
     */
    private Long pid;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路径
     */
    private String path;

    /**
     * 组件
     */
    private String component;

    /**
     * 类型（menu/catalog/button/embedded/link）
     */
    private String type;

    /**
     * 状态（1 启用，0 禁用）
     */
    private String status;

    /**
     * 权限码
     */
    private String authCode;

    /**
     * 图标
     */
    private String icon;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 元数据
     */
    private MetaVO meta;

    /**
     * 子菜单列表
     */
    private List<RouteMenuVO> children;

    /**
     * 元数据内部类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 标题名称（必填）
         */
        private String title;

        /**
         * 激活图标（菜单）
         */
        private String activeIcon;

        /**
         * 当前激活的菜单，有时候不想激活现有菜单，需要激活父级菜单时使用
         */
        private String activePath;

        /**
         * 是否固定标签页
         * @default false
         */
        private Boolean affixTab;

        /**
         * 固定标签页的顺序
         * @default 0
         */
        private Integer affixTabOrder;

        /**
         * 需要特定的角色标识才可以访问
         * @default []
         */
        private List<String> authority;

        /**
         * 徽标
         */
        private String badge;

        /**
         * 徽标类型
         */
        private String badgeType;

        /**
         * 徽标颜色
         */
        private String badgeVariants;

        /**
         * 路由的完整路径作为key（默认true）
         */
        private Boolean fullPathKey;

        /**
         * 当前路由的子级在菜单中不展现
         * @default false
         */
        private Boolean hideChildrenInMenu;

        /**
         * 当前路由在面包屑中不展现
         * @default false
         */
        private Boolean hideInBreadcrumb;

        /**
         * 当前路由在菜单中不展现
         * @default false
         */
        private Boolean hideInMenu;

        /**
         * 当前路由在标签页不展现
         * @default false
         */
        private Boolean hideInTab;

        /**
         * 图标（菜单/tab）
         */
        private String icon;

        /**
         * iframe 地址
         */
        private String iframeSrc;

        /**
         * 忽略权限，直接可以访问
         * @default false
         */
        private Boolean ignoreAccess;

        /**
         * 开启KeepAlive缓存
         */
        private Boolean keepAlive;

        /**
         * 外链-跳转路径
         */
        private String link;

        /**
         * 路由是否已经加载过
         */
        private Boolean loaded;

        /**
         * 标签页最大打开数量
         * @default false
         */
        private Integer maxNumOfOpenTab;

        /**
         * 菜单可以看到，但是访问会被重定向到403
         */
        private Boolean menuVisibleWithForbidden;

        /**
         * 当前路由不使用基础布局（仅在顶级生效）
         */
        private Boolean noBasicLayout;

        /**
         * 在新窗口打开
         */
        private Boolean openInNewWindow;

        /**
         * 用于路由->菜单排序
         */
        private Integer order;

        /**
         * 菜单所携带的参数
         */
        private Object query;
    }
}
