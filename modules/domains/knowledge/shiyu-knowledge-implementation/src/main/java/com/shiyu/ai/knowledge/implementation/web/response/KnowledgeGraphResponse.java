package com.shiyu.ai.knowledge.implementation.web.response;

import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import java.util.List;



/**
 * 知识图谱邻域响应
 *
 * @param node node 属性，表示该记录组件承载的数据。
 * @param parentNodes parentNodes 属性，表示该记录组件承载的数据。
 * @param childNodes childNodes 属性，表示该记录组件承载的数据。
 * @param relatedNodes relatedNodes 属性，表示该记录组件承载的数据。
 */
public record KnowledgeGraphResponse(
        KnowledgeResponse node,
        List<KnowledgeResponse> parentNodes,
        List<KnowledgeResponse> childNodes,
        List<KnowledgeResponse> relatedNodes) {}
