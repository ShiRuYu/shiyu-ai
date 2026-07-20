package com.shiyu.ai.knowledge.search;

import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;

import java.util.List;
import java.util.function.Consumer;

/**
 * 知识搜索服务接口
 *
 * <p>支持向量搜索、关键词搜索、索引管理等。</p>
 */
public interface KnowledgeSearchService {

    void rebuildIndex();

    int rebuildIndexWithProgress(Consumer<Integer> progressCallback);

    List<SearchResult> search(String query, int topK);

    List<SearchResult> keywordSearch(String query, int topK);

    List<SearchResult> vectorSearch(String query, int topK);

    List<SearchResult> recommendRelated(Long knowledgeId, int topK);

    void clearIndex();

    void removeFromIndex(Long id);

    void indexKnowledge(KnowledgeBO knowledgeDO);
}
