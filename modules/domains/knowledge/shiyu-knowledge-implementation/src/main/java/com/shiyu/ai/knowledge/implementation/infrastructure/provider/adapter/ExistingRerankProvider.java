package com.shiyu.ai.knowledge.implementation.infrastructure.provider.adapter;

import com.shiyu.ai.knowledge.implementation.domain.port.provider.RerankProvider;

import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatMessage;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ExistingRerankProvider 边界接口，负责向外部组件提供知识领域相关能力。
 */
@Component
public class ExistingRerankProvider implements RerankProvider {

    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    /**
     * {@code ExistingRerankProvider} 创建并初始化当前类型实例。
     *
     * @param chatEngine 参数值，用于执行当前操作。
     */
    public ExistingRerankProvider(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    /**
     * {@code profile} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String profile() {
        return "platform";
    }

    /**
     * {@code rerank} 执行当前类型定义的业务操作。
     *
     * @param query 参数值，用于执行当前操作。
     * @param candidates 参数值，用于执行当前操作。
     * @param topK 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Integer> rerank(String query, List<String> candidates, int topK) {
        if (candidates == null || candidates.size() <= 1) {
            return java.util.stream.IntStream.range(0, candidates == null ? 0 : candidates.size())
                    .boxed()
                    .toList();
        }
        int actualTopK = Math.min(Math.max(1, topK), candidates.size());
        StringBuilder prompt =
                new StringBuilder("请按与问题的相关性对候选片段排序，只返回编号：\n问题：").append(query).append("\n\n");
        for (int i = 0; i < candidates.size(); i++) {
            prompt.append('[').append(i).append("] ").append(candidates.get(i)).append('\n');
        }
        prompt.append("\n返回前 ").append(actualTopK).append(" 个编号，用逗号分隔。");
        try {
            ChatResponse response =
                    chatEngine.chat(
                            ChatRequest.builder()
                                    .platform("default")
                                    .messages(List.of(ChatMessage.text("user", prompt.toString())))
                                    .build());
            if (response != null && response.isSuccess() && response.getContent() != null) {
                Set<Integer> indexes = new LinkedHashSet<>();
                for (String value : response.getContent().split("[,\\s\\[\\]]+")) {
                    try {
                        int index = Integer.parseInt(value.trim());
                        if (index >= 0 && index < candidates.size()) indexes.add(index);
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (!indexes.isEmpty())
                    return new ArrayList<>(indexes)
                            .subList(0, Math.min(actualTopK, indexes.size()));
            }
        } catch (Exception ignored) {
        }
        return java.util.stream.IntStream.range(0, actualTopK).boxed().toList();
    }
}
