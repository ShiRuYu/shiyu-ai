package com.shiyu.ai.agent.domain.vo;

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
    private Integer status;

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
         * 标题
         */
        private String title;

        /**
         * 图标
         */
        private String icon;

        /**
         * 排序
         */
        private Integer order;

        /**
         * 是否固定标签页（可选）
         */
        private Boolean affixTab;

        /**
         * 徽章内容（可选）
         */
        private String badge;

        /**
         * 徽章类型（可选）
         */
        private String badgeType;

        /**
         * 徽章样式（可选）
         */
        private String badgeVariants;

        /**
         * iframe 地址（可选）
         */
        private String iframeSrc;

        /**
         * 外链地址（可选）
         */
        private String link;
    }
}
