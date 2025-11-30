package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.config.ChatResource;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.ModelEnum;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stuff")
public class StuffController {
    @Resource
    private ChatEngine chatEngine;

    @Resource
    private ChatResource chatResource;

    /**
     * 演示使用特定的 prompt 上下文信息以增强大模型的回答。
     */
    @GetMapping(value = "/stuff")
    public Flux<String> completion(@RequestParam(required = false,defaultValue = "SILICON_FLOW") String modelEnum,
                                   @RequestParam(
                                           value = "message",
                                           required = false,
                                           defaultValue = "Which athletes won the mixed doubles gold medal in curling at the 2022 Winter Olympics?'") String message,
                                   @RequestParam(value = "stuffit", defaultValue = "false") boolean stuffit
    ) {

        PromptTemplate promptTemplate = new PromptTemplate(chatResource.getQaPromptResource());

        Map<String, Object> map = new HashMap<>();
        map.put("question", message);

        // 是否填充 prompt 上下文，以增强大模型回答。
        if (stuffit) {
            map.put("context", chatResource.getDocsToStuffResource());
        } else {
            map.put("context", "");
        }

        ChatClient chatClient = chatEngine.getChatClient(ModelEnum.fromEnumName(modelEnum));
        return chatClient.prompt(promptTemplate.create(map))
                .stream().content();
    }
}
