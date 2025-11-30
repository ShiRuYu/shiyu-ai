package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.config.ChatResource;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.ModelEnum;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resource")
public class ResourceController {

    @Resource
    private ChatEngine chatEngine;

    @Resource
    private ChatResource chatResource;


    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam(required = false,defaultValue = "SILICON_FLOW") String modelEnum,
                               @RequestParam(
                                       value = "message",
                                       required = false,
                                       defaultValue = "Tell me about three famous pirates from the Golden Age of Piracy and why they did.  Write at least a sentence for each pirate.") String message,
                               @RequestParam(value = "name", required = false, defaultValue = "Bob") String name,
                               @RequestParam(value = "voice", required = false, defaultValue = "pirate") String voice
    ) {

        // 用户输入
        UserMessage userMessage = new UserMessage(message);

        // 使用 System prompt tmpl
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(chatResource.getSystemResource());
        // 填充 System prompt 中的变量值
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));

        ChatClient chatClient = chatEngine.getChatClient(ModelEnum.fromEnumName(modelEnum));
        // 调用大模型
        return chatClient.prompt(
                        new Prompt(List.of(
                                userMessage,
                                systemMessage)))
                .stream().content();
    }

}
