package com.shiyu.ai.auth.request;

import com.shiyu.ai.auth.domain.model.UserBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AutoMapper(target = UserBO.class, reverseConvertGenerate = false)
@Schema(description = "用户创建/更新请求")
public class UserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "状态（0停用 1正常）")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "角色ID数组")
    private Long[] roleIds;

    @NotNull(message = "目标租户不能为空")
    @Schema(description = "目标租户ID")
    private Long tenantId;

    @Schema(description = "岗位ID数组")
    private Long[] postIds;
}
