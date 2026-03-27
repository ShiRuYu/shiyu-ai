package com.shiyu.ai.agent.langchain4j;

import com.shiyu.ai.agent.langchain4j.config.Lc4jPlatformConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * LangChain4j 模型管理器
 * 统一管理所有平台的模型适配器，提供便捷的模型获取接口
 */
@Slf4j
@Service
public class Lc4jModelManager {
    
    /**
     * 平台适配器映射（按平台类型缓存）
     */
    private final Map<String, Lc4jPlatformAdapter> adapterMap = new ConcurrentHashMap<>();
    
    /**
     * 注入所有 PlatformAdapter 实例
     */
    public Lc4jModelManager(List<Lc4jPlatformAdapter> adapters) {
        // 注册所有可用的适配器
        for (Lc4jPlatformAdapter adapter : adapters) {
            registerAdapter(adapter);
        }
        log.info("LangChain4j 模型管理器初始化完成，已注册 {} 个平台适配器", adapterMap.size());
    }
    
    /**
     * 注册平台适配器
     * @param adapter 平台适配器实例
     */
    public void registerAdapter(Lc4jPlatformAdapter adapter) {
        String platformType = adapter.getPlatformType();
        adapterMap.put(platformType, adapter);
        log.info("注册 LangChain4j 平台适配器：{} -> {}", platformType, adapter.getClass().getSimpleName());
    }
    
    /**
     * 注销平台适配器
     * @param platformType 平台类型
     */
    public void unregisterAdapter(String platformType) {
        Lc4jPlatformAdapter removed = adapterMap.remove(platformType);
        if (removed != null) {
            log.info("注销 LangChain4j 平台适配器：{}", platformType);
            removed.clearCache();
        }
    }
    
    /**
     * 获取同步聊天模型（使用默认配置）
     * @param platformType 平台类型（如：OPENROUTER, OLLAMA 等）
     * @param modelName 模型名称，为空时使用默认模型
     * @return ChatModel 实例
     */
    public ChatModel getChatModel(String platformType, String modelName) {
        Lc4jPlatformAdapter adapter = getAdapter(platformType);
        return adapter.getChatModel(modelName);
    }
    
    /**
     * 根据动态配置获取同步聊天模型
     * @param config 平台配置
     * @param modelName 模型名称，为空时使用配置中的模型
     * @return ChatModel 实例
     */
    public ChatModel getChatModel(Lc4jPlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        Lc4jPlatformAdapter adapter = getAdapter(config.getPlatformType());
        return adapter.createChatModel(config, modelName != null ? modelName : config.getModelName());
    }
    
    /**
     * 根据动态配置获取同步聊天模型（使用配置中的模型名称）
     * @param config 平台配置
     * @return ChatModel 实例
     */
    public ChatModel getChatModel(Lc4jPlatformConfig config) {
        return getChatModel(config, null);
    }
    
    /**
     * 获取流式聊天模型（使用默认配置）
     * @param platformType 平台类型
     * @param modelName 模型名称，为空时使用默认模型
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel getStreamingChatModel(String platformType, String modelName) {
        Lc4jPlatformAdapter adapter = getAdapter(platformType);
        return adapter.getStreamingChatModel(modelName);
    }
    
    /**
     * 根据动态配置获取流式聊天模型
     * @param config 平台配置
     * @param modelName 模型名称，为空时使用配置中的模型
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel getStreamingChatModel(Lc4jPlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        Lc4jPlatformAdapter adapter = getAdapter(config.getPlatformType());
        return adapter.createStreamingChatModel(config, modelName != null ? modelName : config.getModelName());
    }
    
    /**
     * 根据动态配置获取流式聊天模型（使用配置中的模型名称）
     * @param config 平台配置
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel getStreamingChatModel(Lc4jPlatformConfig config) {
        return getStreamingChatModel(config, null);
    }
    
    /**
     * 获取默认同步聊天模型
     * @param platformType 平台类型
     * @return ChatModel 实例
     */
    public ChatModel getDefaultChatModel(String platformType) {
        Lc4jPlatformAdapter adapter = getAdapter(platformType);
        return adapter.getChatModel(null);
    }
    
    /**
     * 获取默认流式聊天模型
     * @param platformType 平台类型
     * @return StreamingChatModel 实例
     */
    public StreamingChatModel getDefaultStreamingChatModel(String platformType) {
        Lc4jPlatformAdapter adapter = getAdapter(platformType);
        return adapter.getStreamingChatModel(null);
    }
    
    /**
     * 获取平台适配器
     * @param platformType 平台类型
     * @return 平台适配器实例
     */
    public Lc4jPlatformAdapter getAdapter(String platformType) {
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        if (adapter == null) {
            throw new IllegalArgumentException("未找到平台适配器：" + platformType);
        }
        return adapter;
    }
    
    /**
     * 检查平台是否可用
     * @param platformType 平台类型
     * @return true-可用，false-不可用
     */
    public boolean isPlatformAvailable(String platformType) {
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        return adapter != null && adapter.isAvailable();
    }
    
    /**
     * 获取所有可用的平台类型列表
     * @return 平台类型列表
     */
    public List<String> getAvailablePlatforms() {
        return adapterMap.values().stream()
                .filter(Lc4jPlatformAdapter::isAvailable)
                .map(Lc4jPlatformAdapter::getPlatformType)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有已注册的平台适配器
     * @return 平台适配器映射
     */
    public Map<String, Lc4jPlatformAdapter> getAllAdapters() {
        return new ConcurrentHashMap<>(adapterMap);
    }
    
    /**
     * 刷新指定平台的模型缓存
     * @param platformType 平台类型
     */
    public void refreshCache(String platformType) {
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        if (adapter != null) {
            adapter.clearCache();
            log.info("已刷新平台缓存：{}", platformType);
        }
    }
    
    /**
     * 刷新所有平台的模型缓存
     */
    public void refreshAllCache() {
        adapterMap.values().forEach(Lc4jPlatformAdapter::clearCache);
        log.info("已刷新所有平台缓存");
    }
    
    /**
     * 获取默认模型名称
     * @param platformType 平台类型
     * @return 默认模型名称
     */
    public String getDefaultModelName(String platformType) {
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        if (adapter != null) {
            return adapter.getDefaultModelName();
        }
        return null;
    }
}
