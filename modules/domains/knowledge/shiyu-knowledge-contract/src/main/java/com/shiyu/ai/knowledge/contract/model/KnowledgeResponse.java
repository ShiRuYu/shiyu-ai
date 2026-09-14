package com.shiyu.ai.knowledge.contract.model;

import java.util.List;

/**
 * 处理知识响应。
 * @param id 标识，表示该记录组件承载的数据。
 * @param code 编码，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param description 描述，表示该记录组件承载的数据。
 * @param difficulty difficulty 属性，表示该记录组件承载的数据。
 * @param category category 属性，表示该记录组件承载的数据。
 * @param tags tags 属性，表示该记录组件承载的数据。
 * @param parentIds parentIds 属性，表示该记录组件承载的数据。
 * @param childIds childIds 属性，表示该记录组件承载的数据。
 * @param documents documents 属性，表示该记录组件承载的数据。
 */
public record KnowledgeResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer difficulty,
        String category,
        String tags,
        List<Long> parentIds,
        List<Long> childIds,
        List<KnowledgeDocumentDTO> documents) {}
