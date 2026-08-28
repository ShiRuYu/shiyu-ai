package com.shiyu.ai.education.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KnowledgeBindRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @NotNull(message = "知识点ID不能为空")
    private Long knowledgeId;
}

