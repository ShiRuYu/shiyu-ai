package com.shiyu.ai.knowledge.implementation.infrastructure.retrieval;

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
 * {@code KnowledgeContextRetrievalAdapter} 承载知识模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
public class KnowledgeContextRetrievalAdapter implements ContextRetrievalPort {
    /**
     * retrieval 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRetrievalService retrieval;

    /**
     * {@code KnowledgeContextRetrievalAdapter} 创建并初始化当前类型实例。
     *
     * @param retrieval 参数值，用于执行当前操作。
     */
    public KnowledgeContextRetrievalAdapter(KnowledgeRetrievalService retrieval) {
        this.retrieval = retrieval;
    }

    /**
     * {@code retrieve} 执行当前类型定义的业务操作。
     *
     * @param query 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
