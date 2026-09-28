package com.shiyu.ai.runtimeconsole.web;

/** 表示运行时控制台展示的应用状态和环境信息。 */
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
