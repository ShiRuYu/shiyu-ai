package com.shiyu.ai.agent.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单视图对象
 */
@Data
@AutoMapper(target = MenuBO.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuVO implements Serializable {

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
     * 菜单编码
     */
    private String code;

    /**
     * 菜单类型（MENU/BUTTON）
     */
    private String type;

    /**
     * 父菜单 ID
     */
    private Long parentId;

    /**
     * 路径
     */
    private String path;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 图标
     */
    private String icon;

    /**
     * 组件
     */
    private String component;

    /**
     * 布局
     */
    private String layout;

    /**
     * 是否缓存
     */
    private Boolean keepAlive;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否显示
     */
    private Boolean show;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排序
     */
    private Integer order;

    /**
     * 子菜单列表
     */
    private List<MenuVO> children;
}
