package com.shiyu.ai.memory.config;
import com.shiyu.ai.memory.service.ConsolidationService;

import com.shiyu.ai.dal.memory.repository.ConversationMessageRepository;
import com.shiyu.ai.dal.memory.repository.LongTermMemoryRepository;
import com.shiyu.ai.dal.memory.repository.EpisodicMemoryRepository;
import com.shiyu.ai.memory.spi.impl.EpisodicMemoryStore;
import com.shiyu.ai.memory.impl.MemoryServiceImpl;
import com.shiyu.ai.memory.spi.impl.LongTermMemoryStore;
import com.shiyu.ai.memory.pipeline.ConsolidationPipeline;
import com.shiyu.ai.memory.recall.HybridRecallStrategy;
import com.shiyu.ai.memory.recall.MemoryRecallStrategy;
import com.shiyu.ai.memory.spi.impl.SemanticMemoryStore;
import com.shiyu.ai.memory.spi.impl.ShortTermMemoryStore;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.impl.WorkingMemoryStore;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Center 自动配置
 *
 * <p>自动装配所有记忆存储层 Bean：
 * <ul>
 *   <li>短期记忆：ShortTermMemoryStore (DB + Caffeine)</li>
 *   <li>工作记忆：WorkingMemoryStore (内存)</li>
 *   <li>长期记忆：LongTermMemoryStore (DB)</li>
 *   <li>情景记忆：EpisodicMemoryStore (DB, 条件装配)</li>
 *   <li>语义记忆：SemanticMemoryStore (向量库, 条件装配)</li>
 * </ul>
 * </p>
 */
@Slf4j
@Configuration
@AutoConfigureAfter(name = {
        "com.shiyu.ai.vector.config.VectorStoreAutoConfiguration",
        "com.shiyu.ai.model.config.EmbeddingAutoConfiguration"
})
public class MemoryAutoConfiguration {

    // ========================
    // MemoryStore Beans
    // ========================

    @Bean
    @ConditionalOnMissingBean
    public ShortTermMemoryStore shortTermMemoryStore(ConversationMessageRepository repository) {
        return new ShortTermMemoryStore(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public LongTermMemoryStore longTermMemoryStore(LongTermMemoryRepository repository) {
        return new LongTermMemoryStore(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public WorkingMemoryStore workingMemoryStore() {
        return new WorkingMemoryStore();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EpisodicMemoryRepository.class)
    public EpisodicMemoryStore episodicMemoryStore(EpisodicMemoryRepository repository) {
        log.info("EpisodicMemoryStore 已启用");
        return new EpisodicMemoryStore(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({VectorStore.class, EmbeddingService.class})
    public SemanticMemoryStore semanticMemoryStore(VectorStore vectorStore, EmbeddingService embeddingService) {
        log.info("SemanticMemoryStore 已启用（向量检索）");
        return new SemanticMemoryStore(vectorStore, embeddingService);
    }

    // ========================
    // 基础设施
    // ========================

    @Bean
    @ConditionalOnMissingBean
    public ConsolidationService consolidationService(ConversationMessageRepository conversationMessageRepository,
                                                     LongTermMemoryRepository longTermMemoryRepository,
                                                     ChatEngine chatEngine) {
        return new ConsolidationService(conversationMessageRepository, longTermMemoryRepository, chatEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public MemoryRecallStrategy hybridRecallStrategy(List<MemoryStore> stores) {
        return new HybridRecallStrategy(stores);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsolidationPipeline consolidationPipeline(
            ShortTermMemoryStore shortTermStore,
            LongTermMemoryStore longTermStore,
            WorkingMemoryStore workingStore,
            ObjectProvider<EpisodicMemoryStore> episodicStoreProvider,
            ConsolidationService consolidationService) {
        EpisodicMemoryStore episodicStore = episodicStoreProvider.getIfAvailable();
        if (episodicStore == null) {
            log.info("ConsolidationPipeline: EpisodicMemoryStore 不可用，WM→EPI 管道将跳过");
        }
        return new ConsolidationPipeline(shortTermStore, longTermStore, workingStore,
                episodicStore, consolidationService);
    }

    // ========================
    // MemoryService — 整合所有 Store
    // ========================

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "memoryServiceImpl")
    public MemoryServiceImpl memoryServiceImpl(
            ShortTermMemoryStore shortTermStore,
            LongTermMemoryStore longTermStore,
            WorkingMemoryStore workingStore,
            ObjectProvider<SemanticMemoryStore> semanticStoreProvider,
            ObjectProvider<EpisodicMemoryStore> episodicStoreProvider,
            MemoryRecallStrategy recallStrategy,
            ConsolidationService consolidationService) {

        // 收集所有可用 Store
        List<MemoryStore> stores = new ArrayList<>();
        stores.add(shortTermStore);
        stores.add(longTermStore);
        stores.add(workingStore);

        SemanticMemoryStore semanticStore = semanticStoreProvider.getIfAvailable();
        if (semanticStore != null) {
            stores.add(semanticStore);
        }

        EpisodicMemoryStore episodicStore = episodicStoreProvider.getIfAvailable();
        if (episodicStore != null) {
            stores.add(episodicStore);
        }

        MemoryServiceImpl service = new MemoryServiceImpl(stores, recallStrategy, consolidationService);
        service.setStoreReferences(shortTermStore, longTermStore, workingStore, semanticStore, episodicStore);
        log.info("MemoryService 已初始化: stores={} (semantic={}, episodic={})",
                stores.size(), semanticStore != null, episodicStore != null);
        return service;
    }
}
