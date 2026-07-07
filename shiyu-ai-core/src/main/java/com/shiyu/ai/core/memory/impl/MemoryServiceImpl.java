package com.shiyu.ai.core.memory.impl;

import com.shiyu.ai.core.ChatEngine;
import com.shiyu.ai.core.ChatRequest;
import com.shiyu.ai.core.ChatResponse;
import com.shiyu.ai.dal.repository.ConversationMessageRepository;
import com.shiyu.ai.dal.repository.LongTermMemoryRepository;
import com.shiyu.ai.core.memory.MemoryService;
import com.shiyu.ai.model.bo.ConversationMessageBO;
import com.shiyu.ai.model.bo.LongTermMemoryBO;
import com.shiyu.ai.model.vo.ConversationMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
@Slf4j
@Service
@Primary
public class MemoryServiceImpl implements MemoryService {

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private final ConversationMessageRepository conversationMessageRepository;
    private final LongTermMemoryRepository longTermMemoryRepository;
    private final ChatEngine chatEngine;

    public MemoryServiceImpl(ConversationMessageRepository conversationMessageRepository,
                             LongTermMemoryRepository longTermMemoryRepository,
                             ChatEngine chatEngine) {
        this.conversationMessageRepository = conversationMessageRepository;
        this.longTermMemoryRepository = longTermMemoryRepository;
        this.chatEngine = chatEngine;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveMessage(String sessionId, Long userId, String agentId, String role, String content) {
        if (sessionId == null || content == null) return;
        ConversationMessageBO msg = new ConversationMessageBO();
        msg.setSessionId(sessionId);
        msg.setUserId(userId);
        msg.setAgentId(agentId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        conversationMessageRepository.insert(msg);
        log.debug("保存对话消息: sessionId={}, role={}, len={}", sessionId, role, content.length());
    }

    private List<ConversationMessageVO> getRecentMessages(String sessionId, int limit) {
        List<ConversationMessageBO> messages = conversationMessageRepository.selectRecentBySession(sessionId, limit);
        List<ConversationMessageVO> result = new ArrayList<>();
        for (ConversationMessageBO msg : messages) {
            ConversationMessageVO vo = new ConversationMessageVO();
            vo.setId(msg.getId());
            vo.setSessionId(msg.getSessionId());
            vo.setRole(msg.getRole());
            vo.setContent(msg.getContent());
            vo.setCreateTime(msg.getCreateTime());
            result.add(vo);
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    public String buildConversationHistory(String sessionId, int maxMessages) {
        List<ConversationMessageVO> messages = getRecentMessages(sessionId, maxMessages);
        if (messages.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (ConversationMessageVO msg : messages) {
            String role = msg.getRole();
            String content = msg.getContent();
            if (ROLE_USER.equals(role)) {
                sb.append("用户: ").append(content).append("\n");
            } else if (ROLE_ASSISTANT.equals(role)) {
                sb.append("助手: ").append(content).append("\n");
            } else {
                sb.append(role).append(": ").append(content).append("\n");
            }
        }
        return sb.toString();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveLongTermMemory(Long userId, String agentId, String category, String key, String content, double importance, String source) {
        if (content == null || content.isBlank()) return;
        LongTermMemoryBO mem = new LongTermMemoryBO();
        mem.setUserId(userId);
        mem.setAgentId(agentId);
        mem.setCategory(category != null ? category : "general");
        mem.setMemoryKey(key);
        mem.setContent(content);
        mem.setImportance(importance);
        mem.setSource(source);
        mem.setCreateTime(LocalDateTime.now());
        mem.setUpdateTime(LocalDateTime.now());
        longTermMemoryRepository.insert(mem);
        log.info("保存长期记忆: userId={}, category={}, importance={}", userId, category, importance);
    }

    @Override
    public List<Map<String, Object>> searchLongTermMemory(String keyword, Long userId, String agentId, int topK) {
        List<LongTermMemoryBO> list;
        if (keyword != null && !keyword.isBlank()) {
            list = longTermMemoryRepository.searchByKeyword(keyword, userId, agentId, topK);
        } else {
            list = longTermMemoryRepository.selectTopByImportance(userId, agentId, topK);
        }
        return toMemoryMapList(list);
    }

    @Override
    public List<Map<String, Object>> retrieveShortTerm(String sessionId, int limit) {
        List<ConversationMessageVO> messages = getRecentMessages(sessionId, limit);
        return messages.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("sessionId", m.getSessionId());
            map.put("role", m.getRole());
            map.put("content", m.getContent());
            map.put("timestamp", m.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> retrieveLongTerm(String query, Long userId, String agentId, int topK) {
        return searchLongTermMemory(query, userId, agentId, topK);
    }

    @Override
    public String summarizeSession(String sessionId, Long userId, String agentId) {
        String history = buildConversationHistory(sessionId, 50);
        if (history.isBlank()) return "";

        // 调用 LLM 生成摘要
        try {
            String prompt = "请对以下对话进行简要总结，提取关键信息和学习要点（100字以内）：\n\n" + history;
            ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());
            if (resp.isSuccess() && resp.getContent() != null && !resp.getContent().isBlank()) {
                String summary = resp.getContent().trim();
                saveLongTermMemory(userId, agentId, "summary", "session:" + sessionId,
                        summary, 0.9, sessionId);
                log.info("LLM 会话摘要已生成: sessionId={}, len={}", sessionId, summary.length());
                return summary;
            }
        } catch (Exception e) {
            log.warn("LLM 摘要生成失败，使用截断回退: {}", e.getMessage());
        }

        // 回退：截断前500字符
        String fallback = history.length() > 500 ? history.substring(0, 500) + "..." : history;
        saveLongTermMemory(userId, agentId, "summary", "session:" + sessionId,
                fallback, 0.9, sessionId);
        log.info("会话摘要（截断回退）已生成: sessionId={}", sessionId);
        return fallback;
    }

    @Override
    public String getSessionSummary(String sessionId) {
        List<LongTermMemoryBO> mems = longTermMemoryRepository.searchByKeyword(
                "session:" + sessionId, null, null, 1);
        return mems.isEmpty() ? null : mems.get(0).getContent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredSessions(int maxDays) {
        LocalDate deadline = LocalDate.now().minusDays(maxDays);
        int deleted = conversationMessageRepository.deleteBySessionBefore(deadline);
        if (deleted > 0) {
            log.info("清理过期会话消息: {} 条 (截止日期={})", deleted, deadline);
        }
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateImportance(Long userId, String agentId) {
        List<LongTermMemoryBO> allMems = longTermMemoryRepository.selectAllByUser(userId, agentId);
        for (LongTermMemoryBO mem : allMems) {
            // 重要性 = 基础值 + 时效因子（越近越重要）
            double baseImportance = mem.getImportance() != null ? mem.getImportance() : 0.5;
            if (mem.getUpdateTime() != null) {
                long daysSinceUpdate = java.time.temporal.ChronoUnit.DAYS.between(
                        mem.getUpdateTime(), LocalDateTime.now());
                double timeDecay = Math.max(0, 1.0 - daysSinceUpdate * 0.01);
                double newImportance = baseImportance * 0.7 + timeDecay * 0.3;
                newImportance = Math.max(0.1, Math.min(1.0, newImportance));
                if (Math.abs(newImportance - baseImportance) > 0.05) {
                    mem.setImportance(newImportance);
                    longTermMemoryRepository.update(mem);
                }
            }
        }
        log.info("长期记忆重要性已重新计算: userId={}, count={}", userId, allMems.size());
    }

    private List<Map<String, Object>> toMemoryMapList(List<LongTermMemoryBO> list) {
        return list.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("content", m.getContent());
            map.put("category", m.getCategory());
            map.put("importance", m.getImportance());
            map.put("memoryKey", m.getMemoryKey());
            map.put("timestamp", m.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }
}
