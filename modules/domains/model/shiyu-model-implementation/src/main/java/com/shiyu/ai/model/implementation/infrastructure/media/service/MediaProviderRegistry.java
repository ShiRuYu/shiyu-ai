package com.shiyu.ai.model.implementation.infrastructure.media.service;

import com.shiyu.ai.model.implementation.infrastructure.media.port.MediaProvider;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理 Media Provider 相关的运行时状态、注册信息或临时数据。
 */
@Service
public class MediaProviderRegistry {
    /**
     * providers 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<MediaProvider> providers;

    /**
     * 执行 Media Provider 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param providers 用于完成本次业务处理的 providers 参数。
     */
    public MediaProviderRegistry(List<MediaProvider> providers) {
        this.providers = providers == null ? List.of() : List.copyOf(providers);
    }

    /**
     * 执行 Media Provider 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<MediaProvider> providers() {
        return providers;
    }

    /**
     * 获取并校验 Media Provider 相关业务数据，并返回处理结果。
     *
     * @return 返回 Media Provider 相关操作生成的结果数据。
     */
    public MediaProvider require() {
        return providers.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no media provider configured"));
    }

    /**
     * 获取并校验 Media Provider 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Media Provider 相关操作生成的结果数据。
     */
    public MediaProvider require(String id) {
        if (id == null || id.isBlank()) return require();
        return providers.stream()
                .filter(provider -> id.equals(provider.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("media provider not found: " + id));
    }
}
