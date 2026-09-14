package com.shiyu.ai.iam.implementation.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shiyu.ai.iam.implementation.domain.model.MenuBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code MenuRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@AutoMapper(target = MenuBO.class, reverseConvertGenerate = false)
@Schema(description = "菜单创建/更新请求")
public class MenuRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称")
    private String name;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @Schema(description = "菜单编码（唯一标识）")
    private String code;

    /**
     * 类型，表示当前对象中的对应属性。
     */
    @NotBlank(message = "菜单类型不能为空")
    @Schema(description = "菜单类型：CATALOG（目录）/ MENU（路由菜单）/ LINK（外链）/ EMBEDDED（内嵌）")
    private String type;

    /**
     * 父级标识，表示当前对象中的对应属性。
     */
    @JsonProperty("pid")
    @Schema(description = "父菜单ID（null表示为根节点）")
    private Long parentId;

    /**
     * 路径，表示当前对象中的对应属性。
     */
    @Schema(description = "路由路径")
    private String path;

    /**
     * redirect 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "重定向地址")
    private String redirect;

    /**
     * icon 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "菜单图标")
    private String icon;

    /**
     * component 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "组件路径")
    private String component;

    /**
     * layout 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "布局")
    private String layout;

    /**
     * keepAlive 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "是否缓存")
    private Boolean keepAlive;

    /**
     * method 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "请求方法")
    private String method;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    @Schema(description = "菜单描述")
    private String description;

    /**
     * show 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "是否显示")
    private Boolean show;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    @Schema(description = "状态（0停用 1正常）")
    private String status;

    /**
     * 顺序，表示当前对象中的对应属性。
     */
    @Schema(description = "排序号")
    private Integer order;
}
