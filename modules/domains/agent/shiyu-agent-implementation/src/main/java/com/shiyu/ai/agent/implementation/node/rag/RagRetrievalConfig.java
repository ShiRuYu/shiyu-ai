package com.shiyu.ai.agent.implementation.node.rag;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.knowledge.contract.model.KnowledgeSourceType;
import com.shiyu.ai.knowledge.contract.model.RetrievalMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

/**
 * RagRetrievalConfig 配置组件，负责注册和配置智能体领域相关基础设施。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RagRetrievalConfig extends NodeConfig {

    /** 允许的来源类型集合。 */
    @Builder.Default private List<Long> spaceIds = List.of();

    @Builder.Default
    private Set<KnowledgeSourceType> sourceTypes =
            Set.of(KnowledgeSourceType.DOCUMENT, KnowledgeSourceType.KNOWLEDGE_ENTRY);

    /**
     * retrievalMode 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Builder.Default private RetrievalMode retrievalMode = RetrievalMode.HYBRID;

    /**
     * candidateTopK 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Builder.Default private Integer candidateTopK = 20;

    /**
     * topK 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Builder.Default private Integer topK = 5;

    /**
     * scoreThreshold 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Builder.Default private Double scoreThreshold = 0D;

    /**
     * enableRerank 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Builder.Default private Boolean enableRerank = true;

    /**
     * rerankTopK 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Builder.Default private Integer rerankTopK = 5;
}
