package com.shiyu.ai.vector.factory;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.spi.VectorStore;
import com.shiyu.ai.vector.spi.VectorStoreProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * VectorStore 工厂 — 通过 SPI 发现实现
 *
 * <p>使用 {@link ServiceLoader} 加载所有 {@link VectorStoreProvider}，
 * 按 type 匹配创建对应实例。新增 VectorStore 实现时只需：</p>
 * <ol>
 *   <li>创建 Provider 类实现 {@code VectorStoreProvider}</li>
 *   <li>在 {@code META-INF/services/com.shiyu.ai.vector.spi.VectorStoreProvider} 中注册</li>
 * </ol>
 */
@Slf4j
public class VectorStoreFactory {

    private static final Map<String, VectorStoreProvider> providers = new LinkedHashMap<>();

    static {
        // 1. ServiceLoader 发现
        ServiceLoader<VectorStoreProvider> loader = ServiceLoader.load(VectorStoreProvider.class);
        for (VectorStoreProvider provider : loader) {
            providers.put(provider.type().toLowerCase(), provider);
            log.debug("VectorStore 提供者已注册: type={}, class={}", provider.type(), provider.getClass().getName());
        }
        // 2. 反射发现（作为 ServiceLoader 的补充，确保核心实现始终可用）
        registerIfAbsent("inmemory", "com.shiyu.ai.vector.spi.impl.InMemoryVectorStoreProvider");
        registerIfAbsent("jvector", "com.shiyu.ai.vector.spi.impl.JVectorStoreProvider");
    }

    public static VectorStore create(String type, VectorStoreProperties properties) {
        VectorStoreProvider provider = providers.get(type.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("未知的 VectorStore 类型: " + type
                    + "，可用类型: " + String.join(", ", providers.keySet()));
        }
        return provider.create(properties);
    }

    private static void registerIfAbsent(String type, String className) {
        if (providers.containsKey(type)) return;
        try {
            Class<?> clazz = Class.forName(className);
            VectorStoreProvider provider = (VectorStoreProvider) clazz.getDeclaredConstructor().newInstance();
            providers.put(type, provider);
            log.info("VectorStore 提供者已注册（反射）: type={}", type);
        } catch (Exception e) {
            log.warn("VectorStore 提供者注册失败: type={}, class={}, {}", type, className, e.getMessage());
        }
    }
}
