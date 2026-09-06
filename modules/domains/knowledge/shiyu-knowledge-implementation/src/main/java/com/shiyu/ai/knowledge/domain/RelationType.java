package com.shiyu.ai.knowledge.domain;

import lombok.Getter;

@Getter
public enum RelationType {

    PRE("前置知识"),
    NEXT("后续知识"),
    INCLUDE("包含关系"),
    RELATED("相关知识点"),
    SIMILAR("相似知识点"),
    BELONG("属于");

    private final String label;

    RelationType(String label) {
        this.label = label;
    }
}
