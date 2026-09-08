package com.shiyu.ai.auth.request;

import com.shiyu.ai.auth.domain.model.TenantBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AutoMapper(target = TenantBO.class, reverseConvertGenerate = false)
@Schema(description = "租户创建/更新请求")
@SuppressWarnings("serial")
public class TenantRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "父租户ID（null=根租户）")
    private Long parentId;

    @NotBlank(message = "租户编码不能为空")
    @Schema(description = "租户编码（唯一）")
    private String code;

    @NotBlank(message = "租户名称不能为空")
    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "域名")
    private String domain;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "排序")
    private Integer order;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态（0停用 1正常）")
    private String status;

    private List<Long> menuIds;

    private List<Long> authCodeIds;

    @Schema(description = "租户超级管理员角色名称")
    private String adminRoleName;

    @Schema(description = "租户管理员用户名")
    private String adminUsername;

    @Schema(description = "租户管理员初始密码")
    private String adminPassword;
}
