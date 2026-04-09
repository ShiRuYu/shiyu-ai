package com.shiyu.ai.agent.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单全部列表视图对象 - 用于 /menu/all 接口
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuAllVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    private List<MenuAllVO> children;

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
         * 是否缓存
         */
        private Boolean keepAlive;

        /**
         * 是否固定标签页
         */
        private Boolean affixTab;

        /**
         * 权限列表
         */
        private List<String> authority;

        /**
         * 菜单可见但禁止访问
         */
        private Boolean menuVisibleWithForbidden;
    }
}
