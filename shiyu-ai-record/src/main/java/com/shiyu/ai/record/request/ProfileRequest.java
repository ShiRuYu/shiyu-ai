package com.shiyu.ai.record.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProfileRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotBlank(message = "名称不能为空")
    private String name;
    private String avatar;
}
