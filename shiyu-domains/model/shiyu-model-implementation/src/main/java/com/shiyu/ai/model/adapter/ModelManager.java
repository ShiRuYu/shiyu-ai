package com.shiyu.ai.model.adapter;

import com.shiyu.ai.model.config.PlatformProperties;
import com.shiyu.ai.model.port.repository.AiModelRepository;
import com.shiyu.ai.model.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.port.ModelRoutingPort;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.model.domain.model.PlatformAdapterType;
import com.shiyu.ai.model.adapter.config.PlatformConfig;
import com.shiyu.ai.model.adapter.impl.GenericPlatformAdapter;
import com.shiyu.ai.model.adapter.impl.OllamaPlatformAdapter;
import com.shiyu.ai.model.adapter.impl.DeepSeekHttpProvider;
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
import com.shiyu.ai.kernel.context.TenantId;

@Slf4j
@Service
public class ModelManager implements ApplicationRunner, ModelRoutingPort {

    private final Map<String, ModelAdapter> adapterMap = new ConcurrentHashMap<>();

    private volatile boolean dbLoaded = false;

    private final AiPlatformRepository platformRepository;
    private final AiModelRepository modelRepository;
    private final PlatformProperties platformProperties;
    private final DeepSeekHttpProvider deepSeekProvider;

    public ModelManager(AiPlatformRepository platformRepository,
                        AiModelRepository modelRepository,
                        PlatformProperties platformProperties) {
        this.platformRepository = platformRepository;
        this.modelRepository = modelRepository;
        this.platformProperties = platformProperties;
        this.deepSeekProvider = new DeepSeekHttpProvider(platformProperties.getDeepseek().getBaseUrl(),
                StringUtils.getIfEmpty(platformProperties.getDeepseek().getApiKey(), () -> getExternalApiKey("DEEPSEEK")),
                platformProperties.getDeepseek().getModel());
        log.info("模型管理器已创建，等待启动后加载适配器");
    }

    @Override
    public void run(ApplicationArguments args) {
        reloadFromDb();
    }

    public void reloadFromDb() {
        log.info("=== 开始加载平台适配器 ===");

        adapterMap.values().forEach(ModelAdapter::clearCache);
        adapterMap.clear();

        try {
            TenantId tenantId = configuredTenant();
            List<AiPlatformBO> platforms = platformRepository.selectAllEnabled(tenantId);
            if (platforms != null && !platforms.isEmpty()) {
                for (AiPlatformBO platform : platforms) {
                    try {
                        ModelAdapter adapter = createAdapterFromDb(tenantId, platform);
                        if (adapter != null) {
                            adapterMap.put(platform.getCode(), adapter);
                            log.info("从数据库注册适配器: {} ({})", platform.getCode(), platform.getName());
                        }
                    } catch (IllegalArgumentException e) {
                        // A malformed platform must not discard every other tenant platform.
                        log.error("跳过协议配置无效的平台 {} ({}): {}", platform.getCode(), platform.getName(), e.getMessage());
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

        log.info("=== 平台适配器加载结果 ===");
        adapterMap.forEach((code, adapter) -> {
            log.info("  {} | {} | 默认模型: {}",
                    code,
                    adapter.isAvailable() ? "可用" : "不可用",
                    adapter.getDefaultModelName() != null ? adapter.getDefaultModelName() : "未配置");
        });
        log.info("==========================");
    }

    private ModelAdapter createAdapterFromDb(TenantId tenantId, AiPlatformBO platform) {
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

        String defaultModelName = null;
        try {
            AiModelBO defaultModel = modelRepository.selectDefaultByPlatformId(tenantId, platform.getId());
            if (defaultModel != null) {
                defaultModelName = defaultModel.getModelName();
            }
        } catch (Exception e) {
            log.debug("查询平台 {} 默认模型失败: {}", code, e.getMessage());
        }

        if (PlatformAdapterType.OLLAMA == PlatformAdapterType.parse(platform.getAdapterType())) {
            return new OllamaPlatformAdapter(baseUrl, defaultModelName, temperature, maxRetries);
        }

        return new GenericPlatformAdapter(code, baseUrl, apiKey, defaultModelName, maxRetries);
    }

    private String getExternalApiKey(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return platformProperties.getApiKey(code);
    }

    private void loadHardcodedDefaults() {
        adapterMap.put("OPENAI", new GenericPlatformAdapter(
                "OPENAI", "https://api.openai.com/v1",
                StringUtils.getIfEmpty(getExternalApiKey("OPENAI"), () -> ""), "gpt-4o", 3));

        adapterMap.put("DEEPSEEK", new GenericPlatformAdapter(
                "DEEPSEEK", platformProperties.getDeepseek().getBaseUrl(),
                StringUtils.getIfEmpty(getExternalApiKey("DEEPSEEK"), () -> ""),
                platformProperties.getDeepseek().getModel(), 3));

        adapterMap.put("OPENROUTER", new GenericPlatformAdapter(
                "OPENROUTER", "https://openrouter.ai/api",
                StringUtils.getIfEmpty(getExternalApiKey("OPENROUTER"), () -> ""), "x-ai/grok-4.1-fast", 3));

        adapterMap.put("SILICON_FLOW", new GenericPlatformAdapter(
                "SILICON_FLOW", "https://api.siliconflow.cn",
                StringUtils.getIfEmpty(getExternalApiKey("SILICON_FLOW"), () -> ""), "THUDM/GLM-Z1-9B-0414", 3));

        adapterMap.put("OLLAMA", new OllamaPlatformAdapter(
                "http://localhost:11434", "gemma3:4b", 0.7, 3));

        dbLoaded = false;
        log.info("已加载 {} 个硬编码默认平台适配器", adapterMap.size());
    }

    public void registerAdapter(ModelAdapter adapter) {
        String platformType = adapter.getPlatformType();
        adapterMap.put(platformType, adapter);
        log.info("注册平台适配器：{}", platformType);
    }

    public void unregisterAdapter(String platformType) {
        ModelAdapter removed = adapterMap.remove(platformType);
        if (removed != null) {
            removed.clearCache();
            log.info("注销平台适配器：{}", platformType);
        }
    }

    public ChatModel getChatModel(String platformType, String modelName) {
        return getAdapter(platformType).getChatModel(modelName);
    }

    /** Dedicated structured DeepSeek transport; generic providers remain available for other platforms. */
    public DeepSeekHttpProvider getDeepSeekProvider() {
        return deepSeekProvider;
    }

    public ChatModel getChatModel(PlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        ModelAdapter adapter = adapterForConfig(config);
        return adapter.createChatModel(config, modelName != null ? modelName : config.getModelName());
    }

    public ChatModel getChatModel(PlatformConfig config) {
        return getChatModel(config, null);
    }

    public StreamingChatModel getStreamingChatModel(String platformType, String modelName) {
        return getAdapter(platformType).getStreamingChatModel(modelName);
    }

    public StreamingChatModel getStreamingChatModel(PlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        ModelAdapter adapter = adapterForConfig(config);
        return adapter.createStreamingChatModel(config, modelName != null ? modelName : config.getModelName());
    }

    public StreamingChatModel getStreamingChatModel(PlatformConfig config) {
        return getStreamingChatModel(config, null);
    }

    private ModelAdapter adapterForConfig(PlatformConfig config) {
        PlatformAdapterType adapterType = PlatformAdapterType.parse(config.getAdapterType());
        if (adapterType == PlatformAdapterType.OLLAMA) {
            return new OllamaPlatformAdapter(config.getBaseUrl(), config.getModelName(),
                    config.getTemperature(), config.getMaxRetries());
        }

        String platformType = StringUtils.defaultIfBlank(config.getPlatformType(), "OPENAI_COMPATIBLE");
        ModelAdapter registered = adapterMap.get(platformType);
        // An explicit OpenAI-compatible configuration must not accidentally
        // reuse an Ollama adapter registered under the same platform code.
        if (registered != null && !(registered instanceof OllamaPlatformAdapter)) {
            return registered;
        }
        return new GenericPlatformAdapter(platformType, config.getBaseUrl(), config.getApiKey(),
                config.getModelName(), config.getMaxRetries() == null ? 3 : config.getMaxRetries());
    }

    public ChatModel getDefaultChatModel(String platformType) {
        return getAdapter(platformType).getChatModel(null);
    }

    public StreamingChatModel getDefaultStreamingChatModel(String platformType) {
        return getAdapter(platformType).getStreamingChatModel(null);
    }

    public ModelAdapter getAdapter(String platformType) {
        if (!dbLoaded) {
            synchronized (this) {
                if (!dbLoaded) {
                    reloadFromDb();
                }
            }
        }
        ModelAdapter adapter = adapterMap.get(platformType);
        if (adapter == null) {
            throw new IllegalArgumentException("未找到平台适配器：" + platformType);
        }
        return adapter;
    }

    public boolean isPlatformAvailable(String platformType) {
        ModelAdapter adapter = adapterMap.get(platformType);
        return adapter != null && adapter.isAvailable();
    }

    public List<String> getAvailablePlatforms() {
        return adapterMap.values().stream()
                .filter(ModelAdapter::isAvailable)
                .map(ModelAdapter::getPlatformType)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelDescriptor> availableModels() {
        return adapterMap.entrySet().stream()
                .map(entry -> new ModelDescriptor(
                        StringUtils.isBlank(entry.getValue().getDefaultModelName())
                                ? entry.getKey() : entry.getValue().getDefaultModelName(),
                        entry.getKey()))
                .toList();
    }

    @Override
    public String resolvePlatform(String model) {
        if (StringUtils.isNotBlank(model)) {
            for (Map.Entry<String, ModelAdapter> entry : getAllAdapters().entrySet()) {
                if (model.equals(entry.getValue().getDefaultModelName())) {
                    return entry.getKey();
                }
            }
        }
        return getDefaultPlatform();
    }

    @Override
    public String defaultPlatform() {
        return getDefaultPlatform();
    }

    public Map<String, ModelAdapter> getAllAdapters() {
        return new ConcurrentHashMap<>(adapterMap);
    }

    public String getDefaultModelName(String platformType) {
        ModelAdapter adapter = adapterMap.get(platformType);
        return adapter != null ? adapter.getDefaultModelName() : null;
    }

    public void markDirty() {
        this.dbLoaded = false;
        log.info("平台适配器已标记为脏，将在下次访问时懒加载");
    }

    public String getDefaultPlatform() {
        try {
            AiPlatformBO defaultPlatform = platformRepository.selectDefault(configuredTenant());
            if (defaultPlatform != null && StringUtils.isNotBlank(defaultPlatform.getCode())) {
                return defaultPlatform.getCode();
            }
        } catch (Exception e) {
            log.debug("查询 DB 默认平台失败: {}", e.getMessage());
        }

        if (!adapterMap.isEmpty()) {
            return adapterMap.keySet().iterator().next();
        }

        return "SILICON_FLOW";
    }

    private TenantId configuredTenant() {
        Long value = platformProperties.getTenantId();
        if (value == null || value <= 0) {
            throw new IllegalStateException("shiyu.ai.tenant-id is required for database-backed model loading");
        }
        return new TenantId(value);
    }

    public boolean isDbLoaded() {
        return dbLoaded;
    }

    public void refreshCache(String platformType) {
        ModelAdapter adapter = adapterMap.get(platformType);
        if (adapter != null) {
            adapter.clearCache();
            log.info("已刷新平台缓存：{}", platformType);
        }
    }

    public void refreshAllCache() {
        adapterMap.values().forEach(ModelAdapter::clearCache);
        log.info("已刷新所有平台缓存");
    }
}
