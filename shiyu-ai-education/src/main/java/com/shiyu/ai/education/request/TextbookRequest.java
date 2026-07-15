package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TextbookRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "教材名称不能为空")
    private String name;

    @NotBlank(message = "科目编码不能为空")
    private String subjectCode;

    @NotNull(message = "年级不能为空")
    private Integer grade;

    private String publisher;
    private String author;
    private String edition;
    private Integer status;
}
