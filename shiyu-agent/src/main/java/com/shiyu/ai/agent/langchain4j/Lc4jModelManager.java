package com.shiyu.ai.agent.langchain4j;

import com.shiyu.ai.agent.biz.agent.config.PlatformProperties;
import com.shiyu.ai.agent.biz.agent.repository.AiModelRepository;
import com.shiyu.ai.agent.biz.agent.repository.AiPlatformRepository;
import com.shiyu.ai.agent.domain.bo.AiModelBO;
import com.shiyu.ai.agent.domain.bo.AiPlatformBO;
import com.shiyu.ai.agent.langchain4j.config.Lc4jPlatformConfig;
import com.shiyu.ai.agent.langchain4j.impl.GenericLc4jAdapter;
import com.shiyu.ai.agent.langchain4j.impl.Lc4jOllamaAdapter;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * LangChain4j 模型管理器
 * <p>
 * 启动时优先从数据库 ai_platform / ai_model 表加载适配器，
 * 若数据库不可用（表不存在 / 为空），则 fallback 到硬编码默认配置。
 * <p>
 * 实现 ApplicationRunner，保证所有 Bean（含 Repository）注入完成后再初始化。
 */
@Slf4j
@Service
public class Lc4jModelManager implements ApplicationRunner {

    private static final String OLLAMA = "OLLAMA";

    /**
     * 平台适配器映射（按平台 code 索引）
     */
    private final Map<String, Lc4jPlatformAdapter> adapterMap = new ConcurrentHashMap<>();

    /**
     * 是否已从数据库成功加载
     */
    private volatile boolean dbLoaded = false;

    private final AiPlatformRepository platformRepository;

    private final AiModelRepository modelRepository;

    private final PlatformProperties platformProperties;

    public Lc4jModelManager(AiPlatformRepository platformRepository,
                            AiModelRepository modelRepository,
                            PlatformProperties platformProperties) {
        this.platformRepository = platformRepository;
        this.modelRepository = modelRepository;
        this.platformProperties = platformProperties;
        log.info("LangChain4j 模型管理器已创建，等待启动后加载适配器");
    }

    // ======================== 启动初始化 ========================

    @Override
    public void run(ApplicationArguments args) {
        reloadFromDb();
    }

    /**
     * 从数据库重新加载所有平台适配器。
     * 若数据库不可用，fallback 到硬编码默认值。
     * 可在运行时被 Controller 调用以响应配置变更。
     */
    public void reloadFromDb() {
        log.info("=== 开始加载平台适配器 ===");

        // 1. 清理旧适配器缓存
        adapterMap.values().forEach(Lc4jPlatformAdapter::clearCache);
        adapterMap.clear();

        // 2. 尝试从数据库加载
        try {
            List<AiPlatformBO> platforms = platformRepository.selectAllEnabled();
            if (platforms != null && !platforms.isEmpty()) {
                for (AiPlatformBO platform : platforms) {
                    Lc4jPlatformAdapter adapter = createAdapterFromDb(platform);
                    if (adapter != null) {
                        adapterMap.put(platform.getCode(), adapter);
                        log.info("从数据库注册适配器: {} ({})", platform.getCode(), platform.getName());
                    }
                }
                dbLoaded = true;
                log.info("数据库加载完成，共注册 {} 个平台适配器", adapterMap.size());
            } else {
                log.warn("数据库中无启用的平台配置，使用硬编码默认值");
                loadHardcodedDefaults();
            }
        } catch (Exception e) {
            log.warn("从数据库加载平台配置失败（{}），使用硬编码默认值", e.getMessage());
            loadHardcodedDefaults();
        }

        // 3. 打印摘要
        log.info("=== 平台适配器加载结果 ===");
        adapterMap.forEach((code, adapter) -> {
            log.info("  {} | {} | 默认模型: {}",
                    code,
                    adapter.isAvailable() ? "可用" : "不可用",
                    adapter.getDefaultModelName() != null ? adapter.getDefaultModelName() : "未配置");
        });
        log.info("==========================");
    }

    // ======================== 从 DB 创建适配器 ========================

    private Lc4jPlatformAdapter createAdapterFromDb(AiPlatformBO platform) {
        String code = platform.getCode();
        String baseUrl = platform.getBaseUrl();
        String apiKey = platform.getApiKey();
        
        String externalApiKey = getExternalApiKey(code);
        if (StringUtils.isNotBlank(externalApiKey)) {
            apiKey = externalApiKey;
            log.debug("平台 {} 使用外部配置 apiKey", code);
        }
        
        double temperature = platform.getTemperature() != null ? platform.getTemperature() : 0.7;
        int maxTokens = platform.getMaxTokens() != null ? platform.getMaxTokens() : 4096;
        int maxRetries = platform.getMaxRetries() != null ? platform.getMaxRetries() : 3;

        // 查询该平台的默认模型
        String defaultModelName = null;
        try {
            AiModelBO defaultModel = modelRepository.selectDefaultByPlatformId(platform.getId());
            if (defaultModel != null) {
                defaultModelName = defaultModel.getModelName();
            }
        } catch (Exception e) {
            log.debug("查询平台 {} 默认模型失败: {}", code, e.getMessage());
        }

        // Ollama 使用专用适配器
        if (OLLAMA.equalsIgnoreCase(code)) {
            return new Lc4jOllamaAdapter(baseUrl, defaultModelName, temperature, maxRetries);
        }

        // 其他平台统一使用 OpenAI 兼容适配器
        return new GenericLc4jAdapter(code, baseUrl, apiKey, defaultModelName);
    }

    private String getExternalApiKey(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        // ???? PlatformProperties ? Map ????? switch-case ???
        return platformProperties.getApiKey(code);
    }

    // ======================== 硬编码 Fallback ========================

    private void loadHardcodedDefaults() {
        adapterMap.put("OPENAI", new GenericLc4jAdapter(
                "OPENAI", "https://api.openai.com/v1",
                StringUtils.defaultString(getExternalApiKey("OPENAI"), ""), "gpt-4o"));

        adapterMap.put("DEEPSEEK", new GenericLc4jAdapter(
                "DEEPSEEK", "https://api.deepseek.com",
                StringUtils.defaultString(getExternalApiKey("DEEPSEEK"), ""), "deepseek-chat"));

        adapterMap.put("OPENROUTER", new GenericLc4jAdapter(
                "OPENROUTER", "https://openrouter.ai/api",
                StringUtils.defaultString(getExternalApiKey("OPENROUTER"), ""), "x-ai/grok-4.1-fast"));

        adapterMap.put("SILICON_FLOW", new GenericLc4jAdapter(
                "SILICON_FLOW", "https://api.siliconflow.cn",
                StringUtils.defaultString(getExternalApiKey("SILICON_FLOW"), ""), "THUDM/GLM-Z1-9B-0414"));

        adapterMap.put("OLLAMA", new Lc4jOllamaAdapter(
                "http://localhost:11434", "gemma3:4b", 0.7, 3));

        dbLoaded = false;
        log.info("已加载 {} 个硬编码默认平台适配器", adapterMap.size());
    }

    // ======================== 注册 / 注销 ========================

    /**
     * 注册平台适配器
     */
    public void registerAdapter(Lc4jPlatformAdapter adapter) {
        String platformType = adapter.getPlatformType();
        adapterMap.put(platformType, adapter);
        log.info("注册平台适配器：{}", platformType);
    }

    /**
     * 注销平台适配器
     */
    public void unregisterAdapter(String platformType) {
        Lc4jPlatformAdapter removed = adapterMap.remove(platformType);
        if (removed != null) {
            removed.clearCache();
            log.info("注销平台适配器：{}", platformType);
        }
    }

    // ======================== 获取模型 ========================

    public ChatModel getChatModel(String platformType, String modelName) {
        return getAdapter(platformType).getChatModel(modelName);
    }

    public ChatModel getChatModel(Lc4jPlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        Lc4jPlatformAdapter adapter = getAdapter(config.getPlatformType());
        return adapter.createChatModel(config, modelName != null ? modelName : config.getModelName());
    }

    public ChatModel getChatModel(Lc4jPlatformConfig config) {
        return getChatModel(config, null);
    }

    public StreamingChatModel getStreamingChatModel(String platformType, String modelName) {
        return getAdapter(platformType).getStreamingChatModel(modelName);
    }

    public StreamingChatModel getStreamingChatModel(Lc4jPlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        Lc4jPlatformAdapter adapter = getAdapter(config.getPlatformType());
        return adapter.createStreamingChatModel(config, modelName != null ? modelName : config.getModelName());
    }

    public StreamingChatModel getStreamingChatModel(Lc4jPlatformConfig config) {
        return getStreamingChatModel(config, null);
    }

    public ChatModel getDefaultChatModel(String platformType) {
        return getAdapter(platformType).getChatModel(null);
    }

    public StreamingChatModel getDefaultStreamingChatModel(String platformType) {
        return getAdapter(platformType).getStreamingChatModel(null);
    }

    // ======================== 查询 ========================

    public Lc4jPlatformAdapter getAdapter(String platformType) {
        if (!dbLoaded) {
            synchronized (this) {
                if (!dbLoaded) {
                    reloadFromDb();
                }
            }
        }
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        if (adapter == null) {
            throw new IllegalArgumentException("未找到平台适配器：" + platformType);
        }
        return adapter;
    }

    public boolean isPlatformAvailable(String platformType) {
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        return adapter != null && adapter.isAvailable();
    }

    public List<String> getAvailablePlatforms() {
        return adapterMap.values().stream()
                .filter(Lc4jPlatformAdapter::isAvailable)
                .map(Lc4jPlatformAdapter::getPlatformType)
                .collect(Collectors.toList());
    }

    public Map<String, Lc4jPlatformAdapter> getAllAdapters() {
        return new ConcurrentHashMap<>(adapterMap);
    }

    public String getDefaultModelName(String platformType) {
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        return adapter != null ? adapter.getDefaultModelName() : null;
    }

    /**
     * 标记数据源为脏，下次获取适配器时自动从 DB 重载。
     * 替代原先每次 CRUD 后调用 reloadFromDb() 的全量重载。
     */
    public void markDirty() {
        this.dbLoaded = false;
        log.info("平台适配器已标记为脏，将在下次访问时懒加载");
    }

    /**
     * 获取默认平台编码
     * 优先级：DB 默认平台（is_default='Y'）> 已注册的第一个适配器 > SILICON_FLOW
     */
    public String getDefaultPlatform() {
        // 1. DB 默认平台
        try {
            AiPlatformBO defaultPlatform = platformRepository.selectDefault();
            if (defaultPlatform != null && StringUtils.isNotBlank(defaultPlatform.getCode())) {
                return defaultPlatform.getCode();
            }
        } catch (Exception e) {
            log.debug("查询 DB 默认平台失败: {}", e.getMessage());
        }

        // 2. 已注册的第一个适配器
        if (!adapterMap.isEmpty()) {
            return adapterMap.keySet().iterator().next();
        }

        // 3. 最终 fallback
        return "SILICON_FLOW";
    }

    public boolean isDbLoaded() {
        return dbLoaded;
    }

    // ======================== 缓存管理 ========================

    public void refreshCache(String platformType) {
        Lc4jPlatformAdapter adapter = adapterMap.get(platformType);
        if (adapter != null) {
            adapter.clearCache();
            log.info("已刷新平台缓存：{}", platformType);
        }
    }

    public void refreshAllCache() {
        adapterMap.values().forEach(Lc4jPlatformAdapter::clearCache);
        log.info("已刷新所有平台缓存");
    }
}
