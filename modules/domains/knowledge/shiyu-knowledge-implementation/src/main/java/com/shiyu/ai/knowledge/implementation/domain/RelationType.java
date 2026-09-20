package com.shiyu.ai.knowledge.implementation.domain;

import lombok.Getter;

/**
 * 定义 关系 Type 可用的枚举值及其业务语义。
 */
@Getter
public enum RelationType {
    PRE("前置知识"),
    NEXT("后续知识"),
    INCLUDE("包含关系"),
    RELATED("相关知识点"),
    SIMILAR("相似知识点"),
    BELONG("属于");

    /**
     * label 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String label;

    RelationType(String label) {
        this.label = label;
    }
}
