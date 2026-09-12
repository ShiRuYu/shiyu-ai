package com.shiyu.ai.iam.implementation.request;

import com.shiyu.ai.iam.implementation.domain.model.UserBO;

import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code UserRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@AutoMapper(target = UserBO.class, reverseConvertGenerate = false)
@Schema(description = "用户创建/更新请求")
public class UserRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * username 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    /**
     * password 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "密码")
    private String password;

    /**
     * nickName 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * email 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * phone 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * gender 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "性别")
    private String gender;

    /**
     * avatar 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * address 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "地址")
    private String address;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    @Schema(description = "状态（0停用 1正常）")
    private String status;

    /**
     * remark 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 角色标识集合，表示当前对象中的对应属性。
     */
    @Schema(description = "角色ID数组")
    private Long[] roleIds;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    @NotNull(message = "目标租户不能为空")
    @Schema(description = "目标租户ID")
    private Long tenantId;

    /**
     * postIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "岗位ID数组")
    private Long[] postIds;
}
