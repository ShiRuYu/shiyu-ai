package com.shiyu.ai.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.shiyu.ai.chat.domain.ChatRequest;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.domain.memory.ConversationHistory;
import com.shiyu.ai.chat.domain.memory.Memory;
import com.shiyu.ai.chat.service.ChatService;
import com.shiyu.ai.chat.service.MemoryService;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 对话服务实现类
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private FlowExecutor flowExecutor;
    
    @Resource
    private MemoryService memoryService;

    @Override
    public Map<String, Object> call(ChatRequest request) {
        GlobalContext context = new GlobalContext();
        try {
            String query = request.text();
            String sessionId = request.sessionId() != null ? request.sessionId() : generateSessionId();
            String userId = request.userId();
            String platform = request.platform();
            String modelName = request.modelName();
            
            // 设置上下文信息
            context.set(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), query);
            context.set(GlobalContext.ChatBizKeyEnum.SESSION_ID.getCode(), sessionId);
            context.set(GlobalContext.ChatBizKeyEnum.USER_ID.getCode(), userId);
            context.set(GlobalContext.ChatBizKeyEnum.PLATFORM.getCode(), platform);
            context.set(GlobalContext.ChatBizKeyEnum.MODEL_NAME.getCode(), modelName);
            
            // 执行主流程（包含记忆加载和保存）
            flowExecutor.execute2Resp("callFlow", null, context);
            
            String result = context.get(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode());
            Object intentObj = context.get(GlobalContext.ChatBizKeyEnum.INTENT.getCode());
            String intent = intentObj != null ? JSONUtils.toJsonString(intentObj) : null;
            String chain = context.get(GlobalContext.ChatBizKeyEnum.CHAIN.getCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("result", result != null ? result : "no_solution");
            response.put("intent", intent);
            response.put("chain", chain);
            response.put("sessionId", sessionId); // 返回 sessionId 用于后续对话
            
            return response;

        } catch (Exception e) {
            log.error("对话处理失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public Flux<String> stream(ChatRequest request) {
        GlobalContext context = new GlobalContext();
        try {
            String query = request.text();
            String sessionId = request.sessionId() != null ? request.sessionId() : generateSessionId();
            String userId = request.userId() != null ? request.userId() : "anonymous";
            String platform = request.platform();
            String modelName = request.modelName();
            
            // 设置上下文信息
            context.set(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), query);
            context.set(GlobalContext.ChatBizKeyEnum.SESSION_ID.getCode(), sessionId);
            context.set(GlobalContext.ChatBizKeyEnum.USER_ID.getCode(), userId);
            context.set(GlobalContext.ChatBizKeyEnum.PLATFORM.getCode(), platform);
            context.set(GlobalContext.ChatBizKeyEnum.MODEL_NAME.getCode(), modelName);
            
            // 设置流式模式标志
            context.set(GlobalContext.ChatBizKeyEnum.STREAM_MODE.getCode(), "true");
            
            // 执行流式调用流程（包含记忆加载和意图识别）
            flowExecutor.execute2Resp("streamFlow", null, context);
            
            // 从上下文中获取流式响应
            Flux<String> flux = context.get(GlobalContext.ChatBizKeyEnum.STREAM_FLUX.getCode());
            if (flux == null) {
                return Flux.error(new RuntimeException("流式调用失败，未获取到响应"));
            }
            
            // 使用 share() 将冷发布器转换为热发布器，允许多次订阅
            // 一次订阅用于返回给客户端，另一次订阅用于收集完整答案保存记忆
            Flux<String> sharedFlux = flux.share();
            
            // 立即开始收集完整答案（不等待客户端订阅）
            // 这样确保即使客户端断开连接，记忆也能被保存
            Mono<String> answerMono = sharedFlux.reduce((a, b) -> a + b)
                .doOnNext(fullAnswer -> {
                    try {
                        log.info("流式回答完成，已收集完整答案用于保存记忆");
                        // 将完整答案设置到上下文中
                        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), fullAnswer);
                        
                        // 保存记忆
                        saveMemories(sessionId, userId, query, context);
                    } catch (Exception e) {
                        log.error("保存记忆失败：{}", e.getMessage(), e);
                    }
                })
                .onErrorResume(error -> {
                    log.error("收集流式答案失败：{}", error.getMessage());
                    return Mono.empty(); // 错误时返回空，不影响主流程
                });
            
            // 立即订阅触发收集（无论是否有客户端订阅）
            answerMono.subscribe();
            
            // 返回共享的 Flux 给客户端
            return sharedFlux;
            
        } catch (Exception e) {
            log.error("流式对话处理失败", e);
            return Flux.error(e);
        }
    }
    
    /**
     * 生成会话 ID
     */
    private String generateSessionId() {
        return IdUtil.fastSimpleUUID();
    }
    
    /**
     * 保存对话记忆（流式完成后调用）
     */
    private void saveMemories(String sessionId, String userId, String query, GlobalContext context) {
        // 从上下文中获取最终答案
        String finalAnswer = context.get(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode());
        if (finalAnswer == null || finalAnswer.isEmpty()) {
            log.warn("最终答案为，无法保存记忆");
            return;
        }
        
        Object intentObj = context.get(GlobalContext.ChatBizKeyEnum.INTENT.getCode());
        String intentType = intentObj != null ? JSONUtils.toJsonString(intentObj) : null;
        String chainUsed = context.get(GlobalContext.ChatBizKeyEnum.CHAIN.getCode());
        
        try {
            // 1. 保存对话历史
            ConversationHistory history = ConversationHistory.builder()
                    .sessionId(sessionId)
                    .userId(userId != null ? userId : sessionId.split("_")[0])
                    .userQuery(query)
                    .aiResponse(finalAnswer)
                    .intentType(intentType)
                    .chainUsed(chainUsed)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            memoryService.saveConversationHistory(history);
            
            // 2. 添加短期记忆（用户问题和 AI 回答）
            Memory userMemory = Memory.builder()
                    .sessionId(sessionId)
                    .userId(userId != null ? userId : sessionId.split("_")[0])
                    .type(Memory.MemoryType.SHORT_TERM)
                    .content("用户：" + query)
                    .weight(0.9)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
            
            Memory aiMemory = Memory.builder()
                    .sessionId(sessionId)
                    .userId(userId != null ? userId : sessionId.split("_")[0])
                    .type(Memory.MemoryType.SHORT_TERM)
                    .content("AI: " + finalAnswer)
                    .weight(0.9)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();
            
            memoryService.addShortTermMemory(userMemory);
            memoryService.addShortTermMemory(aiMemory);
            
            // 3. 提取并存储长期记忆
            memoryService.extractAndStoreLongTermMemory(sessionId, query, finalAnswer);
            
            log.info("流式对话记忆保存完成");
            
        } catch (Exception e) {
            log.error("保存记忆失败：{}", e.getMessage(), e);
            throw new RuntimeException("保存记忆失败", e);
        }
    }
}
