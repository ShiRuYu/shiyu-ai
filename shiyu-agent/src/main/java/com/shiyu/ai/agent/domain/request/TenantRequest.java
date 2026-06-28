package com.shiyu.ai.agent.domain.request;

import com.shiyu.ai.model.bo.TenantBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = TenantBO.class, reverseConvertGenerate = false)
public class TenantRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "租户编码不能为空")
    private String code;

    @NotBlank(message = "租户名称不能为空")
    private String name;

    private String contactName;

    private String contactPhone;

    private String address;

    private String domain;

    private String intro;

    private String status;
}
