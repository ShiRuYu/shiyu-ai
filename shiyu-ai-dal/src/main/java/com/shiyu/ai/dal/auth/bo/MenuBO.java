package com.shiyu.ai.dal.auth.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.auth.dataobject.MenuDO;

/**
 * 菜单业务对象
 */
@AutoMapper(target = MenuDO.class, reverseConvertGenerate = true)
@Data
public class MenuBO implements Serializable {

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
     * 状态（1正常 0停用）
     */
    private String status;

    /**
     * 排序
     */
    private Integer order;

    /**
     * 删除标志（0：正常 1：已删除）
     */
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 子菜单列表
     */
    private List<MenuBO> children;
}
