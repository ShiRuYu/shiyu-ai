package com.shiyu.ai.runtimeconsole.web;

public record RuntimeStatus(
        String version,
        String mode,
        long pid,
        int port,
        long uptimeMillis,
        String appHome,
        String health,
        String healthDetail,
        boolean managed) {}
