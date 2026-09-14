package com.shiyu.ai.knowledge.contract.model;

import java.util.List;

/**
 * 处理知识documentdto。
 * @param id 标识，表示该记录组件承载的数据。
 * @param title 标题，表示该记录组件承载的数据。
 * @param content 内容，表示该记录组件承载的数据。
 * @param docType docType 属性，表示该记录组件承载的数据。
 * @param source 来源，表示该记录组件承载的数据。
 * @param knowledgeIds knowledgeIds 属性，表示该记录组件承载的数据。
 */
public record KnowledgeDocumentDTO(
        Long id,
        String title,
        String content,
        String docType,
        String source,
        List<Long> knowledgeIds) {}
