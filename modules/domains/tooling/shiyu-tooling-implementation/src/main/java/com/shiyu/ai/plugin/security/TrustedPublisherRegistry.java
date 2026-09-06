package com.shiyu.ai.plugin.security;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Production allow-list for plugin publishers. Configuration is deliberately
 * process-local for the single-instance P0-P2 deployment; P3 can back it with
 * the plugin catalog without changing the verification contract.
 */
public class TrustedPublisherRegistry {
    private final Set<String> trusted;

    public TrustedPublisherRegistry() {
        this(System.getProperty("shiyu.plugin.trusted-publishers", ""));
    }

    TrustedPublisherRegistry(String configured) {
        trusted = Arrays.stream((configured == null ? "" : configured).split(","))
                .map(String::trim).filter(value -> !value.isBlank())
                .map(String::toLowerCase).collect(Collectors.toUnmodifiableSet());
    }

    public boolean isTrusted(String publisherKeyBase64) {
        if (publisherKeyBase64 == null || publisherKeyBase64.isBlank()) return false;
        String normalized = publisherKeyBase64.trim().toLowerCase();
        return trusted.contains(normalized) || trusted.contains(PluginSignatureVerifier.fingerprint(publisherKeyBase64));
    }

    public boolean isConfigured() { return !trusted.isEmpty(); }
}
