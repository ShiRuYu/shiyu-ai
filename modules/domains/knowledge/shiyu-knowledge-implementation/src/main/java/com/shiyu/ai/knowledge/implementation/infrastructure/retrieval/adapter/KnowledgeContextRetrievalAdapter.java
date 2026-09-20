package com.shiyu.ai.knowledge.implementation.infrastructure.retrieval.adapter;

import com.shiyu.ai.agent.contract.runtime.ContextCitation;
import com.shiyu.ai.agent.contract.runtime.ContextItem;
import com.shiyu.ai.agent.contract.runtime.ContextQuery;
import com.shiyu.ai.agent.contract.runtime.ContextRetrievalPort;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRetrievalService;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalRequest;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 将 知识 Context Retrieval 在不同层之间进行适配、转换或组装。
 */
@Component
public class KnowledgeContextRetrievalAdapter implements ContextRetrievalPort {
    /**
     * retrieval 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRetrievalService retrieval;

    /**
     * 执行 知识 Context Retrieval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param retrieval 用于完成本次业务处理的 retrieval 参数。
     */
    public KnowledgeContextRetrievalAdapter(KnowledgeRetrievalService retrieval) {
        this.retrieval = retrieval;
    }

    /**
     * 执行 知识 Context Retrieval 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ContextItem> retrieve(ContextQuery query) {
        if (query.namespace() != null
                && !query.namespace().isBlank()
                && !"knowledge".equalsIgnoreCase(query.namespace())
                && !"rag".equalsIgnoreCase(query.namespace())) return List.of();
        List<Long> spaces;
        try {
            spaces =
                    query.filters().getOrDefault("spaceIds", "").isBlank()
                            ? List.of()
                            : Arrays.stream(query.filters().get("spaceIds").split(","))
                                    .map(String::trim)
                                    .map(Long::valueOf)
                                    .toList();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("spaceIds must be numeric", ex);
        }
        KnowledgeRetrievalResult result =
                retrieval.retrieve(
                        new KnowledgeRetrievalRequest(
                                new ActorContext(query.tenantId(), query.ownerUserId(), false),
                                spaces,
                                null,
                                null,
                                query.text(),
                                Math.max(query.topK() * 4, 20),
                                query.topK(),
                                0D,
                                true));
        return result.hits().stream()
                .map(
                        hit ->
                                new ContextItem(
                                        "KNOWLEDGE_CHUNK",
                                        String.valueOf(hit.chunkId()),
                                        String.valueOf(hit.documentVersionId()),
                                        hit.content(),
                                        hit.rerankScore() > 0 ? hit.rerankScore() : hit.rrfScore(),
                                        new ContextCitation(
                                                hit.title(), null, hit.sectionPath(), null),
                                        List.of(
                                                "space:" + hit.spaceId(),
                                                "document:" + hit.documentId(),
                                                "version:" + hit.documentVersionId()),
                                        "knowledge-access",
                                        0,
                                        null))
                .toList();
    }
}
