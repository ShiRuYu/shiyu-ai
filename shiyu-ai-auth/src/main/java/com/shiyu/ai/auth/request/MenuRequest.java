package com.shiyu.ai.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shiyu.ai.dal.bo.auth.MenuBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = MenuBO.class, reverseConvertGenerate = false)
public class MenuRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    private String code;

    @NotBlank(message = "菜单类型不能为空")
    private String type;

    @JsonProperty("pid")
    private Long parentId;

    private String path;

    private String redirect;

    private String icon;

    private String component;

    private String layout;

    private Boolean keepAlive;

    private String method;

    private String description;

    private Boolean show;

    private String status;

    private Integer order;
}
