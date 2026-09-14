package com.shiyu.ai.model.implementation.infrastructure.media;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@code MediaProviderRegistry} 承载模型模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Service
public class MediaProviderRegistry {
    /**
     * providers 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<MediaProvider> providers;

    /**
     * {@code MediaProviderRegistry} 创建并初始化当前类型实例。
     *
     * @param providers 参数值，用于执行当前操作。
     */
    public MediaProviderRegistry(List<MediaProvider> providers) {
        this.providers = providers == null ? List.of() : List.copyOf(providers);
    }

    /**
     * {@code providers} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<MediaProvider> providers() {
        return providers;
    }

    /**
     * {@code require} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public MediaProvider require() {
        return providers.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no media provider configured"));
    }

    /**
     * {@code require} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public MediaProvider require(String id) {
        if (id == null || id.isBlank()) return require();
        return providers.stream()
                .filter(provider -> id.equals(provider.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("media provider not found: " + id));
    }
}
