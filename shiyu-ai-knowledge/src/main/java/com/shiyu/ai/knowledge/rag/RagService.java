package com.shiyu.ai.knowledge.rag;

import com.shiyu.ai.knowledge.search.SearchSource;

import java.util.List;
import java.util.Map;

/**
 * RAG 检索服务接口
 * 用于从知识库中检索相关信息
 */
public interface RagService {

    /**
     * 检索文档
     *
     * @param query           查询文本
     * @param source          检索来源（KNOWLEDGE = 知识点, DOCUMENT = 文档 chunk）
     * @param topK            返回结果数量
     * @return 检索结果
     */
    RagRetrievalResult retrieve(String query, SearchSource source, int topK);

    /**
     * 检索文档（使用默认配置）
     *
     * @param query 查询文本
     * @return 检索结果
     */
    RagRetrievalResult retrieve(String query);

    /**
     * RAG 检索结果
     */
    record RagRetrievalResult(
        boolean success,
        List<Document> documents,
        String errorMessage
    ) {}

    /**
     * 文档对象
     */
    record Document(
        String id,
        String content,
        Double score,
        Map<String, Object> metadata
    ) {}
}
