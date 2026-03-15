package com.shiyu.ai.chat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.shiyu.ai.chat.domain.ChatRequest;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.StreamResult;
import com.shiyu.ai.chat.service.ChatService;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话服务实现类
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatEngine chatEngine;

    @Resource
    private FlowExecutor flowExecutor;

    @Override
    public Map<String, Object> chat(ChatRequest request) {
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
            return flux != null ? flux : Flux.error(new RuntimeException("流式调用失败，未获取到响应"));
            
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
}
