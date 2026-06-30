package com.shiyu.ai.knowledge.search;

/**
 * 搜索模式枚举
 */
public enum SearchMode {
    /**
     * 关键词搜索 (BM25)
     */
    KEYWORD,

    /**
     * 语义搜索 (向量 ANN)，需配置 Embedding API
     */
    SEMANTIC,

    /**
     * 混合检索 (向量 + BM25 + RRF 融合)，需配置 Embedding API
     */
    HYBRID
}
