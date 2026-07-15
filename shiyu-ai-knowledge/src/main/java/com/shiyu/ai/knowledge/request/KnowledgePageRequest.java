package com.shiyu.ai.knowledge.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KnowledgePageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;
    private String subjectCode;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
