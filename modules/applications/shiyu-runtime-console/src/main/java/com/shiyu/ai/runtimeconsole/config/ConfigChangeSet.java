package com.shiyu.ai.runtimeconsole.config;

import java.util.Map;

public record ConfigChangeSet(
        long expectedVersion,
        Map<String, String> values,
        Map<String, SecretChange> secrets) {

    public record SecretChange(String action, String value) {}
}
