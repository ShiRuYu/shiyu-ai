package com.shiyu.ai.common.vector.model;

/**
 * {@code VectorSearchType} 表示平台基础设施模块中的一组受控业务状态或分类。
 */
public enum VectorSearchType {
    /** 近似最近邻（HNSW 默认） */
    ANN,
    /** 精确搜索（暴力扫描） */
    EXACT
}
