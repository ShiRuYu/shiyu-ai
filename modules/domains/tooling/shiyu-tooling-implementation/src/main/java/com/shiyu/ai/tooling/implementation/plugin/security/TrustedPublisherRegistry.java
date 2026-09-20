package com.shiyu.ai.tooling.implementation.plugin.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理 Trusted Publisher 相关的运行时状态、注册信息或临时数据。
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
     * 校验或判断 Trusted Publisher 相关业务数据，并返回处理结果。
     *
     * @param publisherKeyBase64 用于完成本次业务处理的 publisherKeyBase64 参数。
     * @return 返回本次条件判断是否成立。
     */
    public boolean isTrusted(String publisherKeyBase64) {
        if (publisherKeyBase64 == null || publisherKeyBase64.isBlank()) return false;
        String normalized = publisherKeyBase64.trim().toLowerCase();
        return trusted.contains(normalized)
                || trusted.contains(PluginSignatureVerifier.fingerprint(publisherKeyBase64));
    }

    /**
     * 校验或判断 Trusted Publisher 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public boolean isConfigured() {
        return !trusted.isEmpty();
    }
}
