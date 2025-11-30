package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.yomahub.liteflow.core.FlowExecutor;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatEngine chatEngine;

    @Resource
    private FlowExecutor flowExecutor;

    @GetMapping("/stream")
    public Flux<String> stream(String text,@RequestParam(required = false,defaultValue = "SILICON_FLOW") String modelEnum) {
        return chatEngine.stream(text, ModelEnum.fromEnumName(modelEnum));
    }

    @GetMapping
    public Map<String, Object> chat(Map<String, Object> request) {
        GlobalContext context = new GlobalContext();
        try {
            context.set(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), MapUtils.getString(request,"text"));
            flowExecutor.execute2Resp("chatFlow", null, context);
            String noSolution = context.get(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), "no_solution");
            return Collections.singletonMap("result", noSolution);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.singletonMap("error", e.getMessage());
        }
    }
}
