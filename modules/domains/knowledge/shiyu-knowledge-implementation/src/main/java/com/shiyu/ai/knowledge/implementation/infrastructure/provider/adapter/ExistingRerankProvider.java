package com.shiyu.ai.knowledge.implementation.infrastructure.provider.adapter;

import com.shiyu.ai.knowledge.implementation.domain.port.provider.RerankProvider;

import com.shiyu.ai.kernel.context.TenantId;
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
 * 创建或提供 Existing Rerank 相关的业务组件和运行时能力。
 */
@Component
public class ExistingRerankProvider implements RerankProvider {

    /**
     * chatEngine 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ChatEngine chatEngine;

    /**
     * 执行 Existing Rerank 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param chatEngine 用于完成本次业务处理的 chatEngine 参数。
     */
    public ExistingRerankProvider(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    /**
     * 执行 Existing Rerank 相关业务数据，并返回处理结果。
     *
     * @return 返回 Existing Rerank 相关操作生成的结果数据。
     */
    @Override
    public String profile() {
        return "platform";
    }

    /**
     * 调用聊天模型生成候选片段排序；模型失败或未返回有效索引时退回原始顺序。
     *
     * @param tenantId 本次重排请求所属租户
     * @param query 用于评价片段相关性的查询文本
     * @param candidates 待排序的候选片段文本列表
     * @param topK 期望保留的最高相关片段数量
     * @return 按相关性从高到低排列的候选索引
     */
    @Override
    public List<Integer> rerank(TenantId tenantId, String query, List<String> candidates, int topK) {
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
                                    .tenantId(tenantId == null ? 0L : tenantId.value())
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
