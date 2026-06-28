package com.shiyu.ai.model.request;

import com.shiyu.ai.model.bo.RoleBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AutoMapper(target = RoleBO.class, reverseConvertGenerate = false)
public class RoleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "角色编码不能为空")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String status;

    private String remark;

    private List<Long> permissions;
}
