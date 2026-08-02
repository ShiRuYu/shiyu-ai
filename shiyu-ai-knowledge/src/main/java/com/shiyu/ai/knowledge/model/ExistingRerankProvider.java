package com.shiyu.ai.knowledge.model;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Embedded adapter for the configured chat model used as a reranker. */
@Component
public class ExistingRerankProvider implements RerankProvider {

    private final ChatEngine chatEngine;

    public ExistingRerankProvider(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    @Override
    public String profile() {
        return "platform";
    }

    @Override
    public List<Integer> rerank(String query, List<String> candidates, int topK) {
        if (candidates == null || candidates.size() <= 1) {
            return java.util.stream.IntStream.range(0, candidates == null ? 0 : candidates.size())
                    .boxed().toList();
        }
        int actualTopK = Math.min(Math.max(1, topK), candidates.size());
        StringBuilder prompt = new StringBuilder("请按与问题的相关性对候选片段排序，只返回编号：\n问题：")
                .append(query).append("\n\n");
        for (int i = 0; i < candidates.size(); i++) {
            prompt.append('[').append(i).append("] ").append(candidates.get(i)).append('\n');
        }
        prompt.append("\n返回前 ").append(actualTopK).append(" 个编号，用逗号分隔。");
        try {
            ChatResponse response = chatEngine.chat(ChatRequest.builder().prompt(prompt.toString()).build());
            if (response != null && response.isSuccess() && response.getContent() != null) {
                Set<Integer> indexes = new LinkedHashSet<>();
                for (String value : response.getContent().split("[,\\s\\[\\]]+")) {
                    try {
                        int index = Integer.parseInt(value.trim());
                        if (index >= 0 && index < candidates.size()) indexes.add(index);
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (!indexes.isEmpty()) return new ArrayList<>(indexes).subList(0,
                        Math.min(actualTopK, indexes.size()));
            }
        } catch (Exception ignored) {
            // Fall back to the deterministic RRF order when the model is unavailable.
        }
        return java.util.stream.IntStream.range(0, actualTopK).boxed().toList();
    }
}
