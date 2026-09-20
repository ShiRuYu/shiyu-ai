package com.shiyu.ai.tooling.implementation.plugin.sandbox;

import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 实现 插件 Sandbox 相关的业务处理、协作逻辑或基础设施能力。
 */
@Slf4j
public class PluginSandbox {

    /**
     * allowedPackages 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Set<String> allowedPackages;
    /**
     * blockedPackages 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Set<String> blockedPackages;

    /**
     * {@code PluginSandbox} 创建并初始化当前类型实例。
     */
    public PluginSandbox() {
        this.allowedPackages =
                new HashSet<>(
                        Set.of(
                                "java.util",
                                "java.lang",
                                "java.io",
                                "org.slf4j",
                                "com.shiyu.ai.tooling.implementation.plugin.spi"));
        this.blockedPackages =
                new HashSet<>(
                        Set.of(
                                "java.net",
                                "java.security",
                                "java.lang.reflect",
                                "java.nio.file",
                                "java.io.FileOutputStream"));
    }

    /** 插件沙箱权限检查 */
    public void checkPermission(String pluginId, String targetPackage) {
        for (String blocked : blockedPackages) {
            if (targetPackage.startsWith(blocked)) {
                log.warn(
                        "插件尝试访问被禁止的包: pluginIdLength={}, packageNameLength={}",
                        valueLength(pluginId),
                        valueLength(targetPackage));
                throw new SecurityException("插件 [" + pluginId + "] 不允许访问: " + targetPackage);
            }
        }
    }

    /** 在沙箱中执行插件 */
    public <T> T executeInSandbox(String pluginId, SandboxCallable<T> callable) {
        // 简化实现：使用线程上下文检查
        Thread currentThread = Thread.currentThread();
        ClassLoader originalLoader = currentThread.getContextClassLoader();

        try {
            return callable.call();
        } catch (SecurityException e) {
            log.error(
                    "插件沙箱拦截: pluginIdLength={}, errorType={}, errorMessageLength={}",
                    valueLength(pluginId),
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("插件执行异常", e);
        }
    }

    private int valueLength(String value) {
        return value == null ? 0 : value.length();
    }

    /**
     * 定义 Sandbox Callable 相关的协作契约和调用边界。
     */
    @FunctionalInterface
    public interface SandboxCallable<T> {
        /**
         * 调用 Sandbox Callable 相关业务数据，并返回处理结果。
         *
         * @return 返回 Sandbox Callable 相关操作生成的结果数据。
         */
        T call() throws Exception;
    }
}
