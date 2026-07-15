package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ResourceRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "资源名称不能为空")
    private Long id;
    @NotBlank(message = "资源名称不能为空")
    private String name;

    @NotBlank(message = "资源类型不能为空")
    private String type;

    private String subjectCode;
    private String url;
    private String description;
    private Integer status;
}

