package com.shiyu.ai.common.vector.model;

public enum VectorSearchType {
    /** 近似最近邻（HNSW 默认） */
    ANN,
    /** 精确搜索（暴力扫描） */
    EXACT
}
