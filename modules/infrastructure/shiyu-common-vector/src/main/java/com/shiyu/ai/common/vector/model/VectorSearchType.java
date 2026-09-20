package com.shiyu.ai.common.vector.model;

/**
 * 定义 向量 Search Type 可用的枚举值及其业务语义。
 */
public enum VectorSearchType {
    /** 近似最近邻（HNSW 默认） */
    ANN,
    /** 精确搜索（暴力扫描） */
    EXACT
}
