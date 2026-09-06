package com.shiyu.ai.knowledge.retrieval;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.runtime.ContextCitation;
import com.shiyu.ai.runtime.ContextItem;
import com.shiyu.ai.runtime.ContextQuery;
import com.shiyu.ai.runtime.ContextRetrievalPort;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class KnowledgeContextRetrievalAdapter implements ContextRetrievalPort {
    private final KnowledgeRetrievalService retrieval;
    public KnowledgeContextRetrievalAdapter(KnowledgeRetrievalService retrieval) { this.retrieval = retrieval; }
    @Override public List<ContextItem> retrieve(ContextQuery query) {
        if (query.namespace() != null && !query.namespace().isBlank()
                && !"knowledge".equalsIgnoreCase(query.namespace()) && !"rag".equalsIgnoreCase(query.namespace())) return List.of();
        List<Long> spaces;
        try {
            spaces = query.filters().getOrDefault("spaceIds", "").isBlank() ? List.of() : Arrays.stream(query.filters().get("spaceIds").split(",")).map(String::trim).map(Long::valueOf).toList();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("spaceIds must be numeric", ex);
        }
        KnowledgeRetrievalResult result = retrieval.retrieve(new KnowledgeRetrievalRequest(
                new ActorContext(query.tenantId(), query.ownerUserId(), false),
                spaces, null, null, query.text(), Math.max(query.topK() * 4, 20),
                query.topK(), 0D, true));
        return result.hits().stream().map(hit -> new ContextItem("KNOWLEDGE_CHUNK", String.valueOf(hit.chunkId()), String.valueOf(hit.documentVersionId()), hit.content(), hit.rerankScore() > 0 ? hit.rerankScore() : hit.rrfScore(), new ContextCitation(hit.title(), null, hit.sectionPath(), null), List.of("space:" + hit.spaceId(), "document:" + hit.documentId(), "version:" + hit.documentVersionId()), "knowledge-access", 0, null)).toList();
    }
}
