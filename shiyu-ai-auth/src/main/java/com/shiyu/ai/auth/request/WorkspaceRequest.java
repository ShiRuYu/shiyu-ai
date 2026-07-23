package com.shiyu.ai.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shiyu.ai.dal.auth.bo.WorkspaceBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = WorkspaceBO.class, reverseConvertGenerate = false)
@Schema(description = "工作空间创建/更新请求")
public class WorkspaceRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("pid")
    @Schema(description = "父工作空间ID（0或null表示根节点）")
    private Long parentId;

    @NotBlank(message = "工作空间名称不能为空")
    @Schema(description = "工作空间名称")
    private String name;

    @Schema(description = "排序号")
    private Integer order;

    @Schema(description = "负责人")
    private String leader;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态（0停用 1正常）")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
