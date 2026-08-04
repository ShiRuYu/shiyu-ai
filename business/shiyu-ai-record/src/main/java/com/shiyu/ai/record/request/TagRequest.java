package com.shiyu.ai.record.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TagRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotBlank(message = "标签名称不能为空")
    private String name;
}
