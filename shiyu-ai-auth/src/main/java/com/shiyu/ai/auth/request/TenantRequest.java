package com.shiyu.ai.auth.request;

import com.shiyu.ai.dal.auth.bo.TenantBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = TenantBO.class, reverseConvertGenerate = false)
@Schema(description = "租户创建/更新请求")
public class TenantRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "状态（0停用 1正常）")
    private String status;
}
