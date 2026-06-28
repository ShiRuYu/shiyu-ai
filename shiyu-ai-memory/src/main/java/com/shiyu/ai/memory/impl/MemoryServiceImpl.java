package com.shiyu.ai.memory.impl;

import com.shiyu.ai.dal.repository.ConversationMessageRepository;
import com.shiyu.ai.dal.repository.LongTermMemoryRepository;
import com.shiyu.ai.memory.MemoryService;
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
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemoryServiceImpl implements MemoryService {

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private final ConversationMessageRepository conversationMessageRepository;
    private final LongTermMemoryRepository longTermMemoryRepository;

    public MemoryServiceImpl(ConversationMessageRepository conversationMessageRepository,
                             LongTermMemoryRepository longTermMemoryRepository) {
        this.conversationMessageRepository = conversationMessageRepository;
        this.longTermMemoryRepository = longTermMemoryRepository;
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
