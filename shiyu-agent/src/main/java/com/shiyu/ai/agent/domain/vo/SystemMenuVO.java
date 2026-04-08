package com.shiyu.ai.agent.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 系统菜单视图对象 - 符合 API 文档规范
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemMenuVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单 ID
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 类型
     */
    private String type;

    /**
     * 图标（可选）
     */
    private String icon;

    /**
     * 路径
     */
    private String path;

    /**
     * 组件（可选）
     */
    private String component;

    /**
     * 父菜单 ID
     */
    private Long pid;

    /**
     * 权限编码
     */
    private String authCode;

    /**
     * 元数据
     */
    private MetaVO meta;

    /**
     * 子菜单列表
     */
    private List<SystemMenuVO> children;

    /**
     * 元数据内部类
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 图标（可选）
         */
        private String icon;

        /**
         * 标题
         */
        private String title;

        /**
         * 是否固定标签页（可选）
         */
        private Boolean affixTab;

        /**
         * 排序（可选）
         */
        private Integer order;
    }
}
