package com.shiyu.ai.core.impl.helper;

import com.shiyu.ai.core.Lc4jRequest;
import com.shiyu.ai.core.Lc4jResponse;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import reactor.core.publisher.Flux;

/**
 * Lc4jService 辅助工具类
 * 提供通用的验证、构建等方法
 */
public class Lc4jServiceHelper {
    
    /**
     * 验证对话请求参数
     * @param request 对话请求
     * @return 验证结果，null 表示验证通过，否则返回错误信息
     */
    public static String validateRequest(Lc4jRequest request) {
        if (request == null) {
            return "请求不能为空";
        }
        
        if (request.getPlatform() == null || request.getPlatform().trim().isEmpty()) {
            return "平台类型不能为空";
        }
        
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return "问题内容不能为空";
        }
        
        return null;
    }
    
    /**
     * 验证同步模型实例
     * @param chatModel 模型实例
     * @param platform 平台类型
     * @param model 模型名称
     * @return 验证通过的 ChatModel，或抛出异常
     */
    public static ChatModel validateChatModel(ChatModel chatModel, String platform, String model) {
        if (chatModel == null) {
            throw new IllegalStateException("无法获取模型实例，请检查平台配置");
        }
        return chatModel;
    }
    
    /**
     * 验证流式模型实例
     * @param streamingChatModel 流式模型实例
     * @param platform 平台类型
     * @param model 模型名称
     * @return 验证通过的 StreamingChatModel，或抛出异常
     */
    public static StreamingChatModel validateStreamingChatModel(
            StreamingChatModel streamingChatModel, String platform, String model) {
        if (streamingChatModel == null) {
            throw new IllegalStateException("无法获取流式模型实例，请检查平台配置");
        }
        return streamingChatModel;
    }
    
    /**
     * 构建成功的对话响应
     * @param content 响应内容
     * @param platform 平台类型
     * @param model 模型名称
     * @return 成功的响应
     */
    public static Lc4jResponse buildSuccessResponse(String content, String platform, String model) {
        return Lc4jResponse.builder()
                .success(true)
                .content(content)
                .platform(platform)
                .model(model)
                .build();
    }
    
    /**
     * 构建失败的对话响应
     * @param errorMessage 错误信息
     * @param platform 平台类型
     * @param model 模型名称
     * @return 失败的响应
     */
    public static Lc4jResponse buildErrorResponse(String errorMessage, String platform, String model) {
        return Lc4jResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .platform(platform)
                .model(model)
                .build();
    }
    
    /**
     * 创建同步助手
     * @param chatModel 聊天模型
     * @return 助手实例
     */
    public static Assistant createAssistant(ChatModel chatModel) {
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .build();
    }
    
    /**
     * 创建流式助手
     * @param streamingChatModel 流式聊天模型
     * @return 流式助手实例
     */
    public static StreamingAssistant createStreamingAssistant(StreamingChatModel streamingChatModel) {
        return AiServices.builder(StreamingAssistant.class)
                .streamingChatModel(streamingChatModel)
                .build();
    }
    
    /**
     * 获取实际使用的模型名称
     * @param request 对话请求
     * @param defaultModelName 默认模型名称
     * @return 实际使用的模型名称
     */
    public static String getActualModelName(Lc4jRequest request, String defaultModelName) {
        if (request.getModel() != null && !request.getModel().trim().isEmpty()) {
            return request.getModel();
        }
        return defaultModelName;
    }
    
    /**
     * 同步助手接口
     */
    public interface Assistant {
        String chat(String message);
    }
    
    /**
     * 流式助手接口
     */
    public interface StreamingAssistant {
        Flux<String> chat(String message);
    }
}
