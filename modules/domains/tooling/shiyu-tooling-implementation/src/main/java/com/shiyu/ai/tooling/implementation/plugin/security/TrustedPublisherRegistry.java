package com.shiyu.ai.tooling.implementation.plugin.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 维护受信任插件发布者及其签名密钥。
 */
public class TrustedPublisherRegistry {
    /**
     * trusted 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Set<String> trusted;

    /**
     * {@code TrustedPublisherRegistry} 创建并初始化当前类型实例。
     */
    public TrustedPublisherRegistry() {
        this(System.getProperty("shiyu.plugin.trusted-publishers", ""));
    }

    TrustedPublisherRegistry(String configured) {
        trusted =
                Arrays.stream((configured == null ? "" : configured).split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(String::toLowerCase)
                        .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * {@code isTrusted} 校验当前操作的输入或状态是否满足约束。
     *
     * @param publisherKeyBase64 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isTrusted(String publisherKeyBase64) {
        if (publisherKeyBase64 == null || publisherKeyBase64.isBlank()) return false;
        String normalized = publisherKeyBase64.trim().toLowerCase();
        return trusted.contains(normalized)
                || trusted.contains(PluginSignatureVerifier.fingerprint(publisherKeyBase64));
    }

    /**
     * {@code isConfigured} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isConfigured() {
        return !trusted.isEmpty();
    }
}
