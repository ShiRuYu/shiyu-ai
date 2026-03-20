package com.shiyu.ai.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.shiyu.ai.chat.domain.memory.Memory;
import com.shiyu.ai.chat.domain.memory.ConversationHistory;
import com.shiyu.ai.chat.domain.memory.MemoryContext;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.service.MemoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记忆服务实现类（基于内存存储，后续可扩展到数据库）
 */
@Slf4j
@Service
public class MemoryServiceImpl implements MemoryService {
    
    @Resource
    private ChatEngine chatEngine;
    
    @Resource
    private com.shiyu.ai.chat.config.MemoryConfig memoryConfig;
    
    // 短期记忆存储：sessionId -> memories
    private final Map<String, List<Memory>> shortTermMemoryStore = new ConcurrentHashMap<>();
    
    // 长期记忆存储：userId -> memories
    private final Map<String, List<Memory>> longTermMemoryStore = new ConcurrentHashMap<>();
    
    // 对话历史存储：sessionId -> histories
    private final Map<String, List<ConversationHistory>> historyStore = new ConcurrentHashMap<>();
    
    @Override
    public void addShortTermMemory(Memory memory) {
        if (memory == null || memory.getSessionId() == null) {
            log.warn("无效的记忆对象");
            return;
        }
        
        // 设置默认值
        if (memory.getId() == null) {
            memory.setId(IdUtil.fastSimpleUUID());
        }
        if (memory.getCreatedAt() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        if (memory.getType() == null) {
            memory.setType(Memory.MemoryType.SHORT_TERM);
        }
        if (memory.getWeight() == null) {
            memory.setWeight(0.8);
        }
        
        // 计算过期时间
        if (memory.getExpiresAt() == null) {
            memory.setExpiresAt(LocalDateTime.now().plusHours(memoryConfig.getShortTermExpireHours()));
        }
        
        String sessionId = memory.getSessionId();
        shortTermMemoryStore.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(memory);
        
        // 限制记忆数量，移除最旧的
        List<Memory> memories = shortTermMemoryStore.get(sessionId);
        if (memories.size() > memoryConfig.getMaxShortTermMemories()) {
            memories.sort(Comparator.comparing(Memory::getCreatedAt));
            memories.remove(0);
        }
        
        log.debug("添加短期记忆：sessionId={}, content={}", sessionId, memory.getContent());
    }
    
    @Override
    public void addLongTermMemory(Memory memory) {
        if (memory == null || memory.getUserId() == null) {
            log.warn("无效的记忆对象");
            return;
        }
        
        // 设置默认值
        if (memory.getId() == null) {
            memory.setId(IdUtil.fastSimpleUUID());
        }
        if (memory.getCreatedAt() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        if (memory.getType() == null) {
            memory.setType(Memory.MemoryType.LONG_TERM);
        }
        if (memory.getWeight() == null) {
            memory.setWeight(1.0); // 长期记忆默认权重为 1
        }
        
        String userId = memory.getUserId();
        longTermMemoryStore.computeIfAbsent(userId, k -> new ArrayList<>()).add(memory);
        
        log.info("添加长期记忆：userId={}, content={}", userId, memory.getContent());
    }
    
    @Override
    public List<Memory> getShortTermMemories(String sessionId, int limit) {
        List<Memory> memories = shortTermMemoryStore.getOrDefault(sessionId, new ArrayList<>());
        
        // 过滤掉过期的记忆
        LocalDateTime now = LocalDateTime.now();
        List<Memory> validMemories = memories.stream()
                .filter(m -> m.getExpiresAt() == null || m.getExpiresAt().isAfter(now))
                .sorted(Comparator.comparing(Memory::getCreatedAt).reversed())
                .limit(Math.min(limit, memoryConfig.getMaxShortTermMemories()))
                .toList();
        
        return validMemories;
    }
    
    @Override
    public List<Memory> getLongTermMemories(String userId, int limit) {
        List<Memory> memories = longTermMemoryStore.getOrDefault(userId, new ArrayList<>());
        
        // 按权重降序排序，返回最重要的记忆
        return memories.stream()
                .sorted(Comparator.comparing(Memory::getWeight).reversed())
                .limit(limit)
                .toList();
    }
    
    @Override
    public void saveConversationHistory(ConversationHistory history) {
        if (history == null || history.getSessionId() == null) {
            log.warn("无效的对话历史对象");
            return;
        }
        
        // 设置默认值
        if (history.getId() == null) {
            history.setId(IdUtil.fastSimpleUUID());
        }
        if (history.getCreatedAt() == null) {
            history.setCreatedAt(LocalDateTime.now());
        }
        
        String sessionId = history.getSessionId();
        historyStore.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(history);
        
        // 限制历史记录数量
        List<ConversationHistory> histories = historyStore.get(sessionId);
        if (histories.size() > memoryConfig.getMaxHistoryRecords()) {
            histories.sort(Comparator.comparing(ConversationHistory::getCreatedAt));
            histories.remove(0);
        }
        
        log.debug("保存对话历史：sessionId={}, query={}", sessionId, history.getUserQuery());
    }
    
    @Override
    public List<ConversationHistory> getRecentHistories(String sessionId, int limit) {
        List<ConversationHistory> histories = historyStore.getOrDefault(sessionId, new ArrayList<>());
        
        // 按时间倒序排序，返回最近的记录
        return histories.stream()
                .sorted(Comparator.comparing(ConversationHistory::getCreatedAt).reversed())
                .limit(Math.min(limit, memoryConfig.getMaxHistoryRecords()))
                .toList();
    }
    
    @Override
    public MemoryContext getMemoryContext(String sessionId, String userId) {
        List<Memory> shortTermMemories = getShortTermMemories(sessionId, memoryConfig.getMaxShortTermMemories());
        List<Memory> longTermMemories = getLongTermMemories(userId, memoryConfig.getMaxLongTermMemories());
        List<ConversationHistory> recentHistories = getRecentHistories(sessionId, memoryConfig.getMaxHistoryRecords());
        
        // 生成记忆摘要
        String memorySummary = generateMemorySummary(longTermMemories);
        
        return MemoryContext.builder()
                .sessionId(sessionId)
                .userId(userId)
                .shortTermMemories(shortTermMemories)
                .longTermMemories(longTermMemories)
                .recentHistories(recentHistories)
                .memorySummary(memorySummary)
                .build();
    }
    
    @Override
    public void cleanupExpiredMemories() {
        LocalDateTime now = LocalDateTime.now();
        
        // 清理短期记忆
        shortTermMemoryStore.forEach((sessionId, memories) -> {
            List<Memory> validMemories = memories.stream()
                    .filter(m -> m.getExpiresAt() == null || m.getExpiresAt().isAfter(now))
                    .toList();
            
            if (validMemories.size() < memories.size()) {
                shortTermMemoryStore.put(sessionId, validMemories);
                log.debug("清理会话 {} 的过期记忆，删除 {} 条", sessionId, memories.size() - validMemories.size());
            }
        });
    }
    
    @Override
    public void extractAndStoreLongTermMemory(String sessionId, String query, String response) {
        try {
            // 构建提取提示词
            String extractPrompt = String.format(
                    "请从以下对话中提取重要的事实、偏好、知识点等长期记忆信息。\n\n" +
                    "用户问题：%s\n\n" +
                    "AI 回答：%s\n\n" +
                    "要求：\n" +
                    "1. 只提取重要、持久性的信息（如用户偏好、事实知识、关键决策等）\n" +
                    "2. 忽略临时性、上下文相关的信息\n" +
                    "3. 如果没有值得提取的信息，返回'无'\n" +
                    "4. 如果有多个要点，用分号分隔\n" +
                    "5. 保持简洁，每个要点不超过 50 字",
                    query, response);
            
            ChatResult result = chatEngine.call(new LmRequest(extractPrompt, PlatformEnum.SILICON_FLOW.getAdapterName(), null, "MemoryService"));
            
            if (result != null && !result.getAnswer().trim().isEmpty() && !result.getAnswer().contains("无")) {
                // 将提取的信息存储为长期记忆
                String[] points = result.getAnswer().split(";");
                for (String point : points) {
                    String cleanPoint = point.trim();
                    if (!cleanPoint.isEmpty()) {
                        Memory memory = Memory.builder()
                                .sessionId(sessionId)
                                .userId(extractUserIdFromSession(sessionId))
                                .type(Memory.MemoryType.LONG_TERM)
                                .content(cleanPoint)
                                .weight(calculateImportanceScore(cleanPoint))
                                .build();
                        
                        addLongTermMemory(memory);
                    }
                }
            }
        } catch (Exception e) {
            log.error("提取长期记忆失败：{}", e.getMessage(), e);
        }
    }
    
    @Override
    public String buildPromptWithMemory(String originalPrompt, MemoryContext memoryContext) {
        if (memoryContext == null) {
            return originalPrompt;
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 添加记忆摘要
        if (memoryContext.getMemorySummary() != null && !memoryContext.getMemorySummary().isEmpty()) {
            sb.append("【相关记忆】\n").append(memoryContext.getMemorySummary()).append("\n\n");
        }
        
        // 添加最近的对话历史
        if (memoryContext.getRecentHistories() != null && !memoryContext.getRecentHistories().isEmpty()) {
            sb.append("【对话历史】\n");
            for (int i = memoryContext.getRecentHistories().size() - 1; i >= 0; i--) {
                ConversationHistory h = memoryContext.getRecentHistories().get(i);
                sb.append("用户：").append(h.getUserQuery()).append("\n");
                sb.append("AI: ").append(h.getAiResponse()).append("\n\n");
            }
        }
        
        // 添加原始问题
        sb.append("【当前问题】\n").append(originalPrompt);
        
        return sb.toString();
    }
    
    /**
     * 生成长期记忆的摘要
     */
    private String generateMemorySummary(List<Memory> longTermMemories) {
        if (longTermMemories == null || longTermMemories.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("已知信息:\n");
        
        // 取权重最高的 10 条记忆
        longTermMemories.stream()
                .sorted(Comparator.comparing(Memory::getWeight).reversed())
                .limit(10)
                .forEach(m -> {
                    sb.append("- ").append(m.getContent()).append("\n");
                });
        
        return sb.toString();
    }
    
    /**
     * 从 Session ID 中提取 User ID（简单实现，可根据实际业务调整）
     */
    private String extractUserIdFromSession(String sessionId) {
        // 简单实现：直接使用 sessionId 作为 userId
        // 实际业务中可以从 sessionId 解析出 userId
        return sessionId != null ? sessionId.split("_")[0] : "anonymous";
    }
    
    /**
     * 计算信息的重要性分数（简单启发式方法）
     */
    private Double calculateImportanceScore(String content) {
        if (content == null || content.isEmpty()) {
            return 0.5;
        }
        
        double score = 0.5;
        
        // 包含特定关键词的信息可能更重要
        if (content.matches(".*(喜欢 | 不喜欢 | 想要 | 需要 | 计划 | 决定 | 记住).*")) {
            score += 0.3;
        }
        
        // 短句子的权重更高（通常是关键信息）
        if (content.length() < 30) {
            score += 0.1;
        }
        
        return Math.min(score, 1.0);
    }
}
