package com.shiyu.ai.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shiyu.ai.dal.bo.auth.WorkspaceBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = WorkspaceBO.class, reverseConvertGenerate = false)
public class WorkspaceRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("pid")
    private Long parentId;

    @NotBlank(message = "工作空间名称不能为空")
    private String name;

    private Integer order;

    private String leader;

    private String phone;

    private String email;

    private String status;

    private String remark;
}
