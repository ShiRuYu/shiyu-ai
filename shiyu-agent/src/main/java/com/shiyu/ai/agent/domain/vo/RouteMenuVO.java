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
     * 元数据
     */
    private MetaVO meta;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路径
     */
    private String path;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 子菜单列表
     */
    private List<RouteMenuVO> children;

    /**
     * 组件
     */
    private String component;

    /**
     * 元数据内部类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 排序
         */
        private Integer order;

        /**
         * 标题
         */
        private String title;

        /**
         * 是否固定标签页（可选）
         */
        private Boolean affixTab;

        /**
         * 图标（可选）
         */
        private String icon;
    }
}
