package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.iam.implementation.domain.model.TenantBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * {@code TenantRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@AutoMapper(target = TenantBO.class, reverseConvertGenerate = false)
@Schema(description = "租户创建/更新请求")
@SuppressWarnings("serial")
public class TenantRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 父级标识，表示当前对象中的对应属性。
     */
    @Schema(description = "父租户ID（null=根租户）")
    private Long parentId;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "租户编码不能为空")
    @Schema(description = "租户编码（唯一）")
    private String code;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    @NotBlank(message = "租户名称不能为空")
    @Schema(description = "租户名称")
    private String name;

    /**
     * contactName 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "联系人")
    private String contactName;

    /**
     * contactPhone 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * address 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "地址")
    private String address;

    /**
     * domain 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "域名")
    private String domain;

    /**
     * intro 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "简介")
    private String intro;

    /**
     * 顺序，表示当前对象中的对应属性。
     */
    @Schema(description = "排序")
    private Integer order;

    /**
     * leader 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "负责人")
    private String leader;

    /**
     * email 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * remark 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    @Schema(description = "状态（0停用 1正常）")
    private String status;

    /**
     * menuIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<Long> menuIds;

    /**
     * authCodeIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<Long> authCodeIds;

    /**
     * adminRoleName 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "租户超级管理员角色名称")
    private String adminRoleName;

    /**
     * adminUsername 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "租户管理员用户名")
    private String adminUsername;

    /**
     * adminPassword 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "租户管理员初始密码")
    private String adminPassword;
}
