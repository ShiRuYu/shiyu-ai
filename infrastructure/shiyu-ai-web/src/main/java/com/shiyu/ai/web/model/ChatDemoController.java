package com.shiyu.ai.web.model;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.common.core.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Chat Demo", description = "Chat Demo")
@SaCheckPermission("agent:chat:config")
@RestController
@RequestMapping("/chat")
public class ChatDemoController {

    private final ChatEngine chatEngine;
    private final ModelManager modelManager;

    public ChatDemoController(ChatEngine chatEngine, ModelManager modelManager) {
        this.chatEngine = chatEngine;
        this.modelManager = modelManager;
    }

    @Operation(summary = "Get Available Platforms")
    @GetMapping("/platforms")
    public Result<List<String>> getAvailablePlatforms() {
        return Result.success(modelManager.getAvailablePlatforms());
    }

    @Operation(summary = "操作: /send")
    @PostMapping("/send")
    public Result<DemoChatResponse> chat(@Valid @RequestBody DemoChatRequest request) {
        log.info("收到聊天请求：platform={}, model={}, prompt={}",
                request.getPlatform(), request.getModel(), request.getPrompt());

        try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(request.getPrompt())
                    .build();

            ChatResponse chatResponse = chatEngine.chat(chatRequest);

            log.info("模型响应成功");
            DemoChatResponse response = DemoChatResponse.builder()
                    .success(chatResponse.isSuccess())
                    .content(chatResponse.getContent())
                    .build();
            return Result.success(response);

        } catch (Exception e) {
            log.error("模型调用失败", e);
            DemoChatResponse response = DemoChatResponse.builder()
                    .success(false)
                    .content("调用失败：" + e.getMessage())
                    .build();
            return Result.success(response);
        }
    }

    @Operation(summary = "Stream Chat")
    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamChat(@Valid @RequestBody DemoChatRequest request) {
        log.info("收到流式聊天请求：platform={}, model={}, prompt={}",
                request.getPlatform(), request.getModel(), request.getPrompt());

        try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(request.getPrompt())
                    .build();

            return chatEngine.stream(chatRequest);
        } catch (Exception e) {
            log.error("流式对话处理失败", e);
            return Flux.error(e);
        }
    }

    @Operation(summary = "Chat With Memory")
    @PostMapping("/send-with-memory")
    public Result<DemoChatResponse> chatWithMemory(
            @RequestParam String sessionId,
            @Valid @RequestBody DemoChatRequest request) {
        log.info("收到带记忆的聊天请求：sessionId={}, platform={}, model={}",
                sessionId, request.getPlatform(), request.getModel());

        try {
            ChatRequest chatRequest = ChatRequest.builder()
                    .platform(request.getPlatform())
                    .model(request.getModel())
                    .prompt(request.getPrompt())
                    .build();

            ChatResponse chatResponse = chatEngine.chatWithMemory(sessionId, chatRequest);

            DemoChatResponse response = DemoChatResponse.builder()
                    .success(chatResponse.isSuccess())
                    .content(chatResponse.getContent())
                    .build();
            return Result.success(response);

        } catch (Exception e) {
            log.error("带记忆的模型调用失败", e);
            DemoChatResponse response = DemoChatResponse.builder()
                    .success(false)
                    .content("调用失败：" + e.getMessage())
                    .build();
            return Result.success(response);
        }
    }

    @Operation(summary = "Get Default Model")
    @GetMapping("/default-model")
    public Result<Map<String, String>> getDefaultModel(@RequestParam String platform) {
        Map<String, String> result = new HashMap<>();
        result.put("platform", platform);
        result.put("defaultModel", modelManager.getDefaultModelName(platform));
        return Result.success(result);
    }

    @Data
    public static class DemoChatRequest {
        private String platform = "SILICON_FLOW";
        private String model;
        private String prompt;
    }

    @Data
    @lombok.Builder
    public static class DemoChatResponse {
        private boolean success;
        private String content;
    }
}
