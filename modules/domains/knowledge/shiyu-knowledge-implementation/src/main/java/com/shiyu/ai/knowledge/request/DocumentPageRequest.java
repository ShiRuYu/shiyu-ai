package com.shiyu.ai.knowledge.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DocumentPageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;
    private Integer topK = 10;
    private Long knowledgeId;
}
