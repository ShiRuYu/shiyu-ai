package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ChapterRequest implements Serializable {

    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "教材ID不能为空")
    private Long textbookId;

    @NotBlank(message = "章节名称不能为空")
    private String name;

    private Long parentId;
    private Integer sort;
    private Integer status;
}

