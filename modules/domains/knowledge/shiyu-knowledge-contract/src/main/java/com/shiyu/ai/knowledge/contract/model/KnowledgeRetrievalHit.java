package com.shiyu.ai.knowledge.contract.model;

/**
 * {@code KnowledgeRetrievalHit} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param spaceId spaceId 属性，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param documentId documentId 属性，表示该记录组件承载的数据。
 * @param documentVersionId documentVersionId 属性，表示该记录组件承载的数据。
 * @param chunkId chunkId 属性，表示该记录组件承载的数据。
 * @param title 标题，表示该记录组件承载的数据。
 * @param content 内容，表示该记录组件承载的数据。
 * @param highlight highlight 属性，表示该记录组件承载的数据。
 * @param pageNumber 页码，表示该记录组件承载的数据。
 * @param sectionPath sectionPath 属性，表示该记录组件承载的数据。
 * @param bm25Score bm25Score 属性，表示该记录组件承载的数据。
 * @param vectorScore vectorScore 属性，表示该记录组件承载的数据。
 * @param rrfScore rrfScore 属性，表示该记录组件承载的数据。
 * @param rerankScore rerankScore 属性，表示该记录组件承载的数据。
 */
public record KnowledgeRetrievalHit(
        Long spaceId,
        Long knowledgeId,
        Long documentId,
        Long documentVersionId,
        Long chunkId,
        String title,
        String content,
        String highlight,
        Integer pageNumber,
        String sectionPath,
        double bm25Score,
        double vectorScore,
        double rrfScore,
        double rerankScore) {}
