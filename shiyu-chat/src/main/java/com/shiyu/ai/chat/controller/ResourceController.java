package com.shiyu.ai.chat.controller;

import com.shiyu.ai.chat.config.ChatResource;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.StreamResult;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/resource")
public class ResourceController {

    @Resource
    private ChatEngine chatEngine;

    @Resource
    private ChatResource chatResource;

    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam(required = false, defaultValue = "SILICON_FLOW") String platformEnum,
                               @RequestParam(value = "message", required = false,
                                       defaultValue = "Tell me about three famous pirates from the Golden Age of Piracy and why they did. Write at least a sentence for each pirate.") String message,
                               @RequestParam(value = "name", required = false, defaultValue = "Bob") String name,
                               @RequestParam(value = "voice", required = false, defaultValue = "pirate") String voice) {

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(chatResource.getSystemResource());
        String systemContent = systemPromptTemplate.render(Map.of("name", name, "voice", voice));

        String combinedPrompt = systemContent + "\n\n" + message;
        LmRequest request = new LmRequest(combinedPrompt, platformEnum, null, "ResourceController");
        StreamResult result = chatEngine.stream(request);
        return result.getAnswer();
    }

}
