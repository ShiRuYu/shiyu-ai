package com.shiyu.ai.knowledge.rag;

import org.springframework.stereotype.Component;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.knowledge.rag.RagOrchestrator.RagChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LLM 重排序器
 * 调用 LLM 对检索结果重新评分排序
 */
@Slf4j
@Component
public class LlmReranker implements Reranker {

    private static final int DEFAULT_TOP_K = 5;
    private final ChatEngine chatEngine;

    public LlmReranker(ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
    }

    @Override
    public List<RagChunk> rerank(String query, List<RagChunk> chunks, int topK) {
        if (chunks == null || chunks.size() <= 1) {
            return chunks;
        }

        int actualTopK = Math.min(topK > 0 ? topK : DEFAULT_TOP_K, chunks.size());

        try {
            // 构建重排序 prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append("请对以下文档片段进行相关性重排序。\n");
            prompt.append("查询: ").append(query).append("\n\n");
            prompt.append("文档片段:\n");

            for (int i = 0; i < chunks.size(); i++) {
                RagChunk chunk = chunks.get(i);
                prompt.append("[").append(i).append("] ").append(chunk.content()).append("\n");
            }

            prompt.append("\n请返回最相关的 ").append(actualTopK).append(" 个片段编号，仅输出数字编号，用逗号分隔。");

            ChatResponse response = chatEngine.chat(
                ChatRequest.builder().prompt(prompt.toString()).build()
            );

            if (response.isSuccess() && response.getContent() != null) {
                Set<Integer> selectedIndices = parseIndices(response.getContent());
                List<RagChunk> reranked = selectedIndices.stream()
                    .filter(i -> i >= 0 && i < chunks.size())
                    .map(chunks::get)
                    .collect(Collectors.toList());

                if (!reranked.isEmpty()) {
                    log.debug("LLM 重排序完成: {} → {} 条", chunks.size(), reranked.size());
                    return reranked;
                }
            }
        } catch (Exception e) {
            log.warn("LLM 重排序失败，使用原始排序: {}", e.getMessage());
        }

        return chunks.stream().limit(actualTopK).collect(Collectors.toList());
    }

    private Set<Integer> parseIndices(String content) {
        Set<Integer> indices = new LinkedHashSet<>();
        String[] parts = content.split("[,\\s\\[\\]]+");
        for (String part : parts) {
            try {
                indices.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return indices;
    }
}
