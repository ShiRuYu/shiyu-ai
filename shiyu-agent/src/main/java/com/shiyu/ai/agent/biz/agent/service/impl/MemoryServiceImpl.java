package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.repository.ConversationMessageRepository;
import com.shiyu.ai.agent.biz.agent.repository.LongTermMemoryRepository;
import com.shiyu.ai.agent.biz.agent.service.MemoryService;
import com.shiyu.ai.agent.dal.dataobject.agent.ConversationMessageDO;
import com.shiyu.ai.agent.dal.dataobject.agent.LongTermMemoryDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    @Override
    public void saveMessage(String sessionId, Long userId, String agentId, String role, String content) {
        if (sessionId == null || content == null) return;
        ConversationMessageDO msg = new ConversationMessageDO();
        msg.setSessionId(sessionId);
        msg.setUserId(userId);
        msg.setAgentId(agentId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        conversationMessageRepository.insert(msg);
        log.debug("保存对话消息: sessionId={}, role={}, len={}", sessionId, role, content.length());
    }

    @Override
    public List<Map<String, Object>> getRecentMessages(String sessionId, int limit) {
        List<ConversationMessageDO> messages = conversationMessageRepository.selectRecentBySession(sessionId, limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ConversationMessageDO msg : messages) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            m.put("createTime", msg.getCreateTime());
            result.add(m);
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    public String buildConversationHistory(String sessionId, int maxMessages) {
        List<Map<String, Object>> messages = getRecentMessages(sessionId, maxMessages);
        if (messages.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> msg : messages) {
            String role = (String) msg.get("role");
            String content = (String) msg.get("content");
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

    @Override
    public void saveLongTermMemory(Long userId, String agentId, String category, String key, String content, double importance, String source) {
        if (content == null || content.isBlank()) return;
        LongTermMemoryDO mem = new LongTermMemoryDO();
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
        List<LongTermMemoryDO> list;
        if (keyword != null && !keyword.isBlank()) {
            list = longTermMemoryRepository.searchByKeyword(keyword, userId, agentId, topK);
        } else {
            list = longTermMemoryRepository.selectTopByImportance(userId, agentId, topK);
        }
        return toMemoryMapList(list);
    }

    @Override
    public List<Map<String, Object>> retrieveShortTerm(String sessionId, int limit) {
        return getRecentMessages(sessionId, limit);
    }

    @Override
    public List<Map<String, Object>> retrieveLongTerm(String query, Long userId, String agentId, int topK) {
        return searchLongTermMemory(query, userId, agentId, topK);
    }

    private List<Map<String, Object>> toMemoryMapList(List<LongTermMemoryDO> list) {
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
