package com.shiyu.ai.agent.node.rag;

import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.knowledge.retrieval.KnowledgeSourceType;
import com.shiyu.ai.knowledge.retrieval.RetrievalMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

/** Configuration for the space-scoped Knowledge retrieval node. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RagRetrievalConfig extends NodeConfig {

    /** Empty means all spaces the calling user can view. */
    @Builder.Default
    private List<Long> spaceIds = List.of();

    @Builder.Default
    private Set<KnowledgeSourceType> sourceTypes = Set.of(
            KnowledgeSourceType.DOCUMENT, KnowledgeSourceType.KNOWLEDGE_ENTRY);

    @Builder.Default
    private RetrievalMode retrievalMode = RetrievalMode.HYBRID;

    @Builder.Default
    private Integer candidateTopK = 20;

    @Builder.Default
    private Integer topK = 5;

    @Builder.Default
    private Double scoreThreshold = 0D;

    @Builder.Default
    private Boolean enableRerank = true;

    @Builder.Default
    private Integer rerankTopK = 5;
}
