package com.shiyu.ai.model.implementation.infrastructure.embedding.impl;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.contract.api.EmbeddingService;
import com.shiyu.ai.model.implementation.domain.event.EmbeddingCallEvent;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提供 Lang Chain 4 j 嵌入 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class LangChain4jEmbeddingService implements EmbeddingService {

    /**
     * embeddingModel 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EmbeddingModel embeddingModel;
    /**
     * eventPublisher 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 执行 Lang Chain 4 j 嵌入 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param embeddingModels 用于完成本次业务处理的 embeddingModels 参数。
     * @param eventPublisher 用于完成本次业务处理的 eventPublisher 参数。
     */
    @Autowired
    public LangChain4jEmbeddingService(
            ObjectProvider<EmbeddingModel> embeddingModels,
            ApplicationEventPublisher eventPublisher) {
        this.embeddingModel =
                embeddingModels.getIfAvailable(
                        LangChain4jEmbeddingService::createOptionalLocalModel);
        this.eventPublisher = eventPublisher;
        log.info("EmbeddingService 初始化完成, 维度={}", modelDimension(this.embeddingModel));
    }

    // 保留无参构造用于手动测试场景
    /**
     * {@code LangChain4jEmbeddingService} 创建并初始化当前类型实例。
     */
    public LangChain4jEmbeddingService() {
        this.embeddingModel = createOptionalLocalModel();
        this.eventPublisher = event -> {};
    }

    /**
     * 执行 Lang Chain 4 j 嵌入 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 Lang Chain 4 j 嵌入 相关操作生成的结果数据。
     */
    @Override
    public float[] embed(TenantId tenantId, String text) {
        return embedInternal(tenantId, null, text);
    }

    /**
     * 执行 Lang Chain 4 j 嵌入 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 Lang Chain 4 j 嵌入 相关操作生成的结果数据。
     */
    @Override
    public float[] embed(ActorContext actor, String text) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return embedInternal(actor.tenantId(), actor.userId(), text);
    }

    private float[] embedInternal(TenantId tenantId, UserId userId, String text) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        requireModel();
        long startMs = System.currentTimeMillis();
        Embedding embedding = embeddingModel.embed(text).content();
        long latencyMs = System.currentTimeMillis() - startMs;

        int estimatedTokens = estimateTokens(text);
        eventPublisher.publishEvent(
                new EmbeddingCallEvent(
                        "BGE-small-zh-v1.5",
                        text.length(),
                        estimatedTokens,
                        1,
                        latencyMs,
                        tenantId,
                        userId));

        log.debug(
                "Embedding 完成: textLen={}, tokens≈{}, latency={}ms",
                text.length(),
                estimatedTokens,
                latencyMs);
        return embedding.vector();
    }

    /**
     * 执行 Lang Chain 4 j 嵌入 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param texts 用于完成本次业务处理的 texts 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<float[]> embedBatch(TenantId tenantId, List<String> texts) {
        requireModel();
        long startMs = System.currentTimeMillis();
        List<TextSegment> segments = texts.stream().map(TextSegment::from).toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        long latencyMs = System.currentTimeMillis() - startMs;

        int totalTextLen = texts.stream().mapToInt(String::length).sum();
        int estimatedTokens = estimateTokens(String.join("", texts));
        eventPublisher.publishEvent(
                new EmbeddingCallEvent(
                        "BGE-small-zh-v1.5",
                        totalTextLen,
                        estimatedTokens,
                        texts.size(),
                        latencyMs,
                        tenantId));

        log.debug(
                "Embedding 批处理完成: batchSize={}, totalLen={}, tokens≈{}, latency={}ms",
                texts.size(),
                totalTextLen,
                estimatedTokens,
                latencyMs);
        return embeddings.stream().map(Embedding::vector).toList();
    }

    /**
     * 执行 Lang Chain 4 j 嵌入 相关业务数据，并返回处理结果。
     *
     * @return 返回 Lang Chain 4 j 嵌入 相关操作生成的结果数据。
     */
    @Override
    public int dimension() {
        return modelDimension(embeddingModel);
    }

    private static int modelDimension(EmbeddingModel model) {
        return model == null ? 0 : model.dimension();
    }

    private void requireModel() {
        if (embeddingModel == null) {
            throw new IllegalStateException(
                    "EmbeddingModel is not configured; enable a cloud provider or offline-models");
        }
    }

    private static EmbeddingModel createOptionalLocalModel() {
        try {
            Class<?> type =
                    Class.forName(
                            "dev.langchain4j.model.embedding.onnx.bgesmallzhv15.BgeSmallZhV15EmbeddingModel");
            return (EmbeddingModel) type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError exception) {
            return null;
        }
    }

    /** 估算 Token 数量（中文约 1 token/1.5 字，英文约 1 token/4 字符） */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chineseChars = 0;
        int otherChars = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseChars++;
            } else {
                otherChars++;
            }
        }
        return (int) Math.ceil(chineseChars / 1.5 + otherChars / 4.0);
    }
}
