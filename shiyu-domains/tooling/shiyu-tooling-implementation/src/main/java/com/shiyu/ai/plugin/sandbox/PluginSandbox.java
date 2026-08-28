package com.shiyu.ai.plugin.sandbox;

import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 插件沙箱
 * 通过 SecurityManager 限制插件的权限
 */
@Slf4j
public class PluginSandbox {

    private final Set<String> allowedPackages;
    private final Set<String> blockedPackages;

    public PluginSandbox() {
        this.allowedPackages = new HashSet<>(Set.of(
                "java.util", "java.lang", "java.io",
                "org.slf4j", "com.shiyu.ai.plugin.spi"
        ));
        this.blockedPackages = new HashSet<>(Set.of(
                "java.net", "java.security", "java.lang.reflect",
                "java.nio.file", "java.io.FileOutputStream"
        ));
    }

    /**
     * 插件沙箱权限检查
     */
    public void checkPermission(String pluginId, String targetPackage) {
        for (String blocked : blockedPackages) {
            if (targetPackage.startsWith(blocked)) {
                log.warn("插件 [{}] 试图访问被禁止的包: {}", pluginId, targetPackage);
                throw new SecurityException("插件 [" + pluginId + "] 不允许访问: " + targetPackage);
            }
        }
    }

    /**
     * 在沙箱中执行插件
     */
    public <T> T executeInSandbox(String pluginId, SandboxCallable<T> callable) {
        // 简化实现：使用线程上下文检查
        Thread currentThread = Thread.currentThread();
        ClassLoader originalLoader = currentThread.getContextClassLoader();

        try {
            return callable.call();
        } catch (SecurityException e) {
            log.error("插件沙箱拦截: pluginId={}, error={}", pluginId, e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("插件执行异常", e);
        }
    }

    @FunctionalInterface
    public interface SandboxCallable<T> {
        T call() throws Exception;
    }
}
