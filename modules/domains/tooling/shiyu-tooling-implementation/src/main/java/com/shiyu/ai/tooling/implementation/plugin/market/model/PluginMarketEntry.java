package com.shiyu.ai.tooling.implementation.plugin.market.model;

import java.time.Instant;
import java.util.List;

/**
 * 表示插件市场中的插件元数据和发布信息。
 * @param id 标识，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
 * @param source 来源，表示该记录组件承载的数据。
 * @param manifest manifest 属性，表示该记录组件承载的数据。
 * @param signature signature 属性，表示该记录组件承载的数据。
 * @param publisherKey publisherKey 属性，表示该记录组件承载的数据。
 * @param permissions permissions 属性，表示该记录组件承载的数据。
 * @param checksum checksum 属性，表示该记录组件承载的数据。
 * @param updatePolicy updatePolicy 属性，表示该记录组件承载的数据。
 * @param publishedAt publishedAt 属性，表示该记录组件承载的数据。
 * @param enabled enabled 属性，表示该记录组件承载的数据。
 */
public record PluginMarketEntry(
        String id,
        String version,
        String source,
        String manifest,
        String signature,
        String publisherKey,
        List<String> permissions,
        String checksum,
        String updatePolicy,
        Instant publishedAt,
        boolean enabled) {
    public PluginMarketEntry(
            String id,
            String version,
            String source,
            String manifest,
            String signature,
            String publisherKey,
            List<String> permissions,
            Instant publishedAt,
            boolean enabled) {
        this(
                id,
                version,
                source,
                manifest,
                signature,
                publisherKey,
                permissions,
                null,
                "MANUAL",
                publishedAt,
                enabled);
    }

    public PluginMarketEntry {
        if (id == null || id.isBlank() || version == null || version.isBlank())
            throw new IllegalArgumentException("plugin id/version required");
        if (manifest == null || manifest.isBlank())
            throw new IllegalArgumentException("plugin manifest is required");
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
        updatePolicy = updatePolicy == null || updatePolicy.isBlank() ? "MANUAL" : updatePolicy;
    }
}
