package com.shiyu.ai.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shiyu.ai.dal.auth.bo.MenuBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = MenuBO.class, reverseConvertGenerate = false)
@Schema(description = "菜单创建/更新请求")
public class MenuRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单编码（唯一标识）")
    private String code;

    @NotBlank(message = "菜单类型不能为空")
    @Schema(description = "菜单类型：CATALOG（目录）/ MENU（路由菜单）/ LINK（外链）/ EMBEDDED（内嵌）")
    private String type;

    @JsonProperty("pid")
    @Schema(description = "父菜单ID（null表示为根节点）")
    private Long parentId;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "重定向地址")
    private String redirect;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "布局")
    private String layout;

    @Schema(description = "是否缓存")
    private Boolean keepAlive;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "菜单描述")
    private String description;

    @Schema(description = "是否显示")
    private Boolean show;

    @Schema(description = "状态（0停用 1正常）")
    private String status;

    @Schema(description = "排序号")
    private Integer order;
}
