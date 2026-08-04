package com.shiyu.ai.dal.auth.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.common.mybatis.model.ServiceAssignedTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import com.shiyu.ai.auth.domain.model.MenuBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * 菜单数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "auth_menu")
@AutoMapper(target = MenuBO.class, reverseConvertGenerate = true)
public class MenuDO extends TenantEntity implements ServiceAssignedTenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单ID
     */
    @Id(keyType = KeyType.Auto)
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
     * 菜单类型（CATALOG/MENU）
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
     * 排序
     */
    private Integer order;

}
