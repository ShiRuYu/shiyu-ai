package com.shiyu.ai.knowledge.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KnowledgeSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "搜索关键词不能为空")
    private String query;

    private String subjectCode;
    private Integer topK = 10;
}
