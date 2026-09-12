package com.shiyu.ai.model.implementation.infrastructure.adapter;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.contract.api.ModelRoutingPort;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.domain.model.PlatformAdapterType;
import com.shiyu.ai.model.implementation.domain.port.repository.AiModelRepository;
import com.shiyu.ai.model.implementation.domain.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.implementation.infrastructure.adapter.config.PlatformConfig;
import com.shiyu.ai.model.implementation.infrastructure.adapter.impl.DeepSeekHttpProvider;
import com.shiyu.ai.model.implementation.infrastructure.adapter.impl.GenericPlatformAdapter;
import com.shiyu.ai.model.implementation.infrastructure.adapter.impl.OllamaPlatformAdapter;
import com.shiyu.ai.model.implementation.infrastructure.config.PlatformProperties;

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
 * {@code ModelManager} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Slf4j
@Service
public class ModelManager implements ApplicationRunner, ModelRoutingPort {

    private final Map<String, ModelAdapter> adapterMap = new ConcurrentHashMap<>();

    /**
     * dbLoaded 属性，保存当前对象中的业务数据或协作依赖。
     */
    private volatile boolean dbLoaded = false;
    /**
     * initialized 属性，保存当前对象中的业务数据或协作依赖。
     */
    private volatile boolean initialized = false;

    /**
     * platformRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiPlatformRepository platformRepository;
    /**
     * modelRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiModelRepository modelRepository;
    /**
     * platformProperties 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PlatformProperties platformProperties;
    /**
     * deepSeekProvider 属性，保存当前对象中的业务数据或协作依赖。
     */
    private volatile DeepSeekHttpProvider deepSeekProvider;

    /**
     * {@code ModelManager} 创建并初始化当前类型实例。
     *
     * @param platformRepository 参数值，用于执行当前操作。
     * @param modelRepository 参数值，用于执行当前操作。
     * @param platformProperties 参数值，用于执行当前操作。
     */
    public ModelManager(
            AiPlatformRepository platformRepository,
            AiModelRepository modelRepository,
            PlatformProperties platformProperties) {
        this.platformRepository = platformRepository;
        this.modelRepository = modelRepository;
        this.platformProperties = platformProperties;
        this.deepSeekProvider =
                new DeepSeekHttpProvider(
                        platformProperties.getDeepseek().getBaseUrl(),
                        StringUtils.getIfEmpty(
                                platformProperties.getDeepseek().getApiKey(),
                                () -> getExternalApiKey("DEEPSEEK")),
                        platformProperties.getDeepseek().getModel());
        log.info("模型管理器已创建，等待启动后加载适配器");
    }

    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param args 参数值，用于执行当前操作。
     */
    @Override
    public void run(ApplicationArguments args) {
        reloadFromDb();
    }

    /**
     * {@code reloadFromDb} 执行当前类型定义的业务操作。
     */
    public synchronized void reloadFromDb() {
        log.info("=== 开始加载平台适配器 ===");
        initialized = false;

        adapterMap.values().forEach(ModelAdapter::clearCache);
        adapterMap.clear();
        deepSeekProvider =
                new DeepSeekHttpProvider(
                        platformProperties.getDeepseek().getBaseUrl(),
                        "",
                        platformProperties.getDeepseek().getModel());

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
                        log.error(
                                "跳过协议配置无效的平台: codePresent={}, namePresent={}, errorType={},"
                                        + " errorMessageLength={}",
                                platform.getCode() != null,
                                platform.getName() != null,
                                e.getClass().getSimpleName(),
                                e.getMessage() == null ? 0 : e.getMessage().length());
                    }
                }
                dbLoaded = true;
                log.info("数据库加载完成，共注册 {} 个平台适配器", adapterMap.size());
            } else {
                log.warn("数据库中无启用的平台配置，使用硬编码默认值");
                loadHardcodedDefaults();
            }
        } catch (Exception e) {
            log.warn(
                    "从数据库加载平台配置失败，使用硬编码默认值: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            loadHardcodedDefaults();
        }

        log.info("=== 平台适配器加载结果 ===");
        adapterMap.forEach(
                (code, adapter) -> {
                    log.info(
                            "  {} | {} | 默认模型: {}",
                            code,
                            adapter.isAvailable() ? "可用" : "不可用",
                            adapter.getDefaultModelName() != null
                                    ? adapter.getDefaultModelName()
                                    : "未配置");
                });
        log.info("==========================");
        initialized = true;
    }

    private ModelAdapter createAdapterFromDb(TenantId tenantId, AiPlatformBO platform) {
        if (platform == null || StringUtils.isBlank(platform.getCode())) {
            throw new IllegalArgumentException("平台编码不能为空");
        }
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
            AiModelBO defaultModel =
                    modelRepository.selectDefaultByPlatformId(tenantId, platform.getId());
            if (defaultModel != null) {
                defaultModelName = defaultModel.getModelName();
            }
        } catch (Exception e) {
            log.debug(
                    "查询平台默认模型失败: codePresent={}, errorType={}, errorMessageLength={}",
                    code != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }

        if (PlatformAdapterType.OLLAMA == PlatformAdapterType.parse(platform.getAdapterType())) {
            return new OllamaPlatformAdapter(baseUrl, defaultModelName, temperature, maxRetries);
        }

        if ("DEEPSEEK".equals(code)) {
            deepSeekProvider =
                    new DeepSeekHttpProvider(
                            baseUrl,
                            apiKey,
                            StringUtils.defaultIfBlank(
                                    defaultModelName, platformProperties.getDeepseek().getModel()));
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
        deepSeekProvider =
                new DeepSeekHttpProvider(
                        platformProperties.getDeepseek().getBaseUrl(),
                        getExternalApiKey("DEEPSEEK"),
                        platformProperties.getDeepseek().getModel());
        adapterMap.put(
                "OPENAI",
                new GenericPlatformAdapter(
                        "OPENAI",
                        "https://api.openai.com/v1",
                        StringUtils.getIfEmpty(getExternalApiKey("OPENAI"), () -> ""),
                        "gpt-4o",
                        3));

        adapterMap.put(
                "DEEPSEEK",
                new GenericPlatformAdapter(
                        "DEEPSEEK",
                        platformProperties.getDeepseek().getBaseUrl(),
                        StringUtils.getIfEmpty(getExternalApiKey("DEEPSEEK"), () -> ""),
                        platformProperties.getDeepseek().getModel(),
                        3));

        adapterMap.put(
                "OPENROUTER",
                new GenericPlatformAdapter(
                        "OPENROUTER",
                        "https://openrouter.ai/api",
                        StringUtils.getIfEmpty(getExternalApiKey("OPENROUTER"), () -> ""),
                        "x-ai/grok-4.1-fast",
                        3));

        adapterMap.put(
                "SILICON_FLOW",
                new GenericPlatformAdapter(
                        "SILICON_FLOW",
                        "https://api.siliconflow.cn",
                        StringUtils.getIfEmpty(getExternalApiKey("SILICON_FLOW"), () -> ""),
                        "THUDM/GLM-Z1-9B-0414",
                        3));

        adapterMap.put(
                "OLLAMA", new OllamaPlatformAdapter("http://localhost:11434", "gemma3:4b", 0.7, 3));

        dbLoaded = false;
        log.info("已加载 {} 个硬编码默认平台适配器", adapterMap.size());
    }

    /**
     * {@code registerAdapter} 写入或更新当前模块中的业务数据。
     *
     * @param adapter 参数值，用于执行当前操作。
     */
    public void registerAdapter(ModelAdapter adapter) {
        String platformType = adapter.getPlatformType();
        adapterMap.put(platformType, adapter);
        log.info("注册平台适配器：{}", platformType);
    }

    /**
     * {@code unregisterAdapter} 执行当前类型定义的业务操作。
     *
     * @param platformType 参数值，用于执行当前操作。
     */
    public void unregisterAdapter(String platformType) {
        ModelAdapter removed = adapterMap.remove(platformType);
        if (removed != null) {
            removed.clearCache();
            log.info("注销平台适配器：{}", platformType);
        }
    }

    /**
     * {@code getChatModel} 查询并返回当前操作所需的数据。
     *
     * @param platformType 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ChatModel getChatModel(String platformType, String modelName) {
        return getAdapter(platformType).getChatModel(modelName);
    }

    /**
     * 获取 DeepSeek 模型提供者。
     *
     * @return 处理结果。
     */
    public DeepSeekHttpProvider getDeepSeekProvider() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) reloadFromDb();
            }
        }
        return deepSeekProvider;
    }

    /**
     * {@code getChatModel} 查询并返回当前操作所需的数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ChatModel getChatModel(PlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        ModelAdapter adapter = adapterForConfig(config);
        return adapter.createChatModel(
                config, modelName != null ? modelName : config.getModelName());
    }

    /**
     * {@code getChatModel} 查询并返回当前操作所需的数据。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ChatModel getChatModel(PlatformConfig config) {
        return getChatModel(config, null);
    }

    /**
     * {@code getStreamingChatModel} 查询并返回当前操作所需的数据。
     *
     * @param platformType 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public StreamingChatModel getStreamingChatModel(String platformType, String modelName) {
        return getAdapter(platformType).getStreamingChatModel(modelName);
    }

    /**
     * {@code getStreamingChatModel} 查询并返回当前操作所需的数据。
     *
     * @param config 参数值，用于执行当前操作。
     * @param modelName 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public StreamingChatModel getStreamingChatModel(PlatformConfig config, String modelName) {
        if (config == null) {
            throw new IllegalArgumentException("平台配置不能为空");
        }
        ModelAdapter adapter = adapterForConfig(config);
        return adapter.createStreamingChatModel(
                config, modelName != null ? modelName : config.getModelName());
    }

    /**
     * {@code getStreamingChatModel} 查询并返回当前操作所需的数据。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public StreamingChatModel getStreamingChatModel(PlatformConfig config) {
        return getStreamingChatModel(config, null);
    }

    private ModelAdapter adapterForConfig(PlatformConfig config) {
        PlatformAdapterType adapterType = PlatformAdapterType.parse(config.getAdapterType());
        if (adapterType == PlatformAdapterType.OLLAMA) {
            return new OllamaPlatformAdapter(
                    config.getBaseUrl(),
                    config.getModelName(),
                    config.getTemperature(),
                    config.getMaxRetries());
        }

        String platformType =
                StringUtils.defaultIfBlank(config.getPlatformType(), "OPENAI_COMPATIBLE");
        ModelAdapter registered = adapterMap.get(platformType);
        if (registered != null && !(registered instanceof OllamaPlatformAdapter)) {
            return registered;
        }
        return new GenericPlatformAdapter(
                platformType,
                config.getBaseUrl(),
                config.getApiKey(),
                config.getModelName(),
                config.getMaxRetries() == null ? 3 : config.getMaxRetries());
    }

    /**
     * {@code getDefaultChatModel} 查询并返回当前操作所需的数据。
     *
     * @param platformType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ChatModel getDefaultChatModel(String platformType) {
        return getAdapter(platformType).getChatModel(null);
    }

    /**
     * {@code getDefaultStreamingChatModel} 查询并返回当前操作所需的数据。
     *
     * @param platformType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public StreamingChatModel getDefaultStreamingChatModel(String platformType) {
        return getAdapter(platformType).getStreamingChatModel(null);
    }

    /**
     * {@code getAdapter} 查询并返回当前操作所需的数据。
     *
     * @param platformType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ModelAdapter getAdapter(String platformType) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
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

    /**
     * {@code isPlatformAvailable} 校验当前操作的输入或状态是否满足约束。
     *
     * @param platformType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isPlatformAvailable(String platformType) {
        ModelAdapter adapter = adapterMap.get(platformType);
        return adapter != null && adapter.isAvailable();
    }

    /**
     * {@code getAvailablePlatforms} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<String> getAvailablePlatforms() {
        return adapterMap.values().stream()
                .filter(ModelAdapter::isAvailable)
                .map(ModelAdapter::getPlatformType)
                .collect(Collectors.toList());
    }

    /**
     * {@code availableModels} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ModelDescriptor> availableModels() {
        return adapterMap.entrySet().stream()
                .map(
                        entry ->
                                new ModelDescriptor(
                                        StringUtils.isBlank(entry.getValue().getDefaultModelName())
                                                ? entry.getKey()
                                                : entry.getValue().getDefaultModelName(),
                                        entry.getKey()))
                .toList();
    }

    /**
     * {@code resolvePlatform} 查询并返回当前操作所需的数据。
     *
     * @param model 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code defaultPlatform} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String defaultPlatform() {
        return getDefaultPlatform();
    }

    /**
     * {@code getAllAdapters} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, ModelAdapter> getAllAdapters() {
        return new ConcurrentHashMap<>(adapterMap);
    }

    /**
     * {@code getDefaultModelName} 查询并返回当前操作所需的数据。
     *
     * @param platformType 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getDefaultModelName(String platformType) {
        ModelAdapter adapter = adapterMap.get(platformType);
        return adapter != null ? adapter.getDefaultModelName() : null;
    }

    /**
     * {@code markDirty} 执行当前类型定义的业务操作。
     */
    public void markDirty() {
        this.dbLoaded = false;
        this.initialized = false;
        log.info("平台适配器已标记为脏，将在下次访问时懒加载");
    }

    /**
     * {@code getDefaultPlatform} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getDefaultPlatform() {
        try {
            AiPlatformBO defaultPlatform = platformRepository.selectDefault(configuredTenant());
            if (defaultPlatform != null && StringUtils.isNotBlank(defaultPlatform.getCode())) {
                return defaultPlatform.getCode();
            }
        } catch (Exception e) {
            log.debug(
                    "查询 DB 默认平台失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }

        if (!adapterMap.isEmpty()) {
            return adapterMap.keySet().iterator().next();
        }

        return "SILICON_FLOW";
    }

    private TenantId configuredTenant() {
        Long value = platformProperties.getTenantId();
        if (value == null || value <= 0) {
            throw new IllegalStateException(
                    "shiyu.ai.tenant-id is required for database-backed model loading");
        }
        return new TenantId(value);
    }

    /**
     * {@code isDbLoaded} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isDbLoaded() {
        return dbLoaded;
    }

    /**
     * {@code refreshCache} 执行当前类型定义的业务操作。
     *
     * @param platformType 参数值，用于执行当前操作。
     */
    public void refreshCache(String platformType) {
        ModelAdapter adapter = adapterMap.get(platformType);
        if (adapter != null) {
            adapter.clearCache();
            log.info("已刷新平台缓存：{}", platformType);
        }
    }

    /**
     * {@code refreshAllCache} 执行当前类型定义的业务操作。
     */
    public void refreshAllCache() {
        adapterMap.values().forEach(ModelAdapter::clearCache);
        log.info("已刷新所有平台缓存");
    }
}
