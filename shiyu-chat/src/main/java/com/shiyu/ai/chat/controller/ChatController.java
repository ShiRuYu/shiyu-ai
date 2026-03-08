package com.shiyu.ai.chat.controller;

import cn.hutool.core.util.IdUtil;
import com.shiyu.ai.chat.domain.ChatRequest;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatEngine chatEngine;

    @Resource
    private FlowExecutor flowExecutor;

    /**
     * 流式对话接口
     */
    @GetMapping("/stream")
    public Flux<String> stream(String text, 
                               @RequestParam(required = false, defaultValue = "SILICON_FLOW") String modelEnum) {
        return chatEngine.stream(text, ModelEnum.fromEnumName(modelEnum));
    }

    /**
     * 普通对话接口（基于 LiteFlow，支持多轮对话和记忆）
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody ChatRequest request) {
        GlobalContext context = new GlobalContext();
        try {
            String query = request.text();
            String sessionId = request.sessionId() != null ? request.sessionId() : generateSessionId();
            String userId = request.userId();
            
            // 设置上下文信息
            context.set(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), query);
            context.set(GlobalContext.ChatBizKeyEnum.SESSION_ID.getCode(), sessionId);
            context.set(GlobalContext.ChatBizKeyEnum.USER_ID.getCode(), userId);
            
            // 执行主流程（包含记忆加载和保存）
            flowExecutor.execute2Resp("chatFlow", null, context);
            
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
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * GET 方式的对话接口（兼容旧版本）
     */
    @GetMapping
    public Map<String, Object> chatGet(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        ChatRequest request = new ChatRequest(text, sessionId, userId);
        return chat(request);
    }
    
    /**
     * 生成会话 ID
     */
    private String generateSessionId() {
        return IdUtil.fastSimpleUUID();
    }
}
