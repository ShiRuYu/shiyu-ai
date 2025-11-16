package com.shiyu.ai.chat.api.controller;

import com.shiyu.ai.chat.core.context.GlobalContext;
import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private FlowExecutor flowExecutor;

    @GetMapping
    public Map<String, Object> chat(Map<String, Object> request) {
        GlobalContext context = new GlobalContext();
        try {
            context.set(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), request.get("text"));
            flowExecutor.execute2Resp("chatFlow", null, context);
            String noSolution = context.get(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), "no_solution");
            return Collections.singletonMap("result", noSolution);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", e.getMessage());
        }
    }
}
