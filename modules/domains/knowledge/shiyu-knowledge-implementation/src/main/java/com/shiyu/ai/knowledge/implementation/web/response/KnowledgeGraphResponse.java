package com.shiyu.ai.knowledge.implementation.web.response;

import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import java.util.List;



/**
 * 封装 知识 Graph 相关的不可变数据及其字段约束。
 */
public record KnowledgeGraphResponse(
        KnowledgeResponse node,
        List<KnowledgeResponse> parentNodes,
        List<KnowledgeResponse> childNodes,
        List<KnowledgeResponse> relatedNodes) {}
