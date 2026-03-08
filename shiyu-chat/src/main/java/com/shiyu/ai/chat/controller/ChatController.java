package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.ModelEnum;
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
     * 普通对话接口（基于 LiteFlow）
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        GlobalContext context = new GlobalContext();
        try {
            String query = (String) request.get("text");
            context.set(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), query);
            
            // 执行主流程
            flowExecutor.execute2Resp("chatFlow", null, context);
            
            String result = context.get(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode());
            String intent = context.get(GlobalContext.ChatBizKeyEnum.INTENT.getCode()) != null ? 
                    context.get(GlobalContext.ChatBizKeyEnum.INTENT.getCode()).toString() : null;
            String chain = context.get(GlobalContext.ChatBizKeyEnum.CHAIN.getCode());
            
            Map<String, Object> response = new HashMap<>();
            response.put("result", result != null ? result : "no_solution");
            response.put("intent", intent);
            response.put("chain", chain);
            
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
    public Map<String, Object> chatGet(Map<String, Object> request) {
        return chat(request);
    }
}
