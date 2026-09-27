package com.shiyu.ai.runtimeconsole.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Loads persisted whitelisted values before Spring creates any application beans. */
public final class ConsoleBootstrapConfiguration {

    private static final Set<String> CONSOLE_LOADED_KEYS = ConcurrentHashMap.newKeySet();
    private static final Set<String> CLI_OVERRIDDEN_KEYS = ConcurrentHashMap.newKeySet();

    private ConsoleBootstrapConfiguration() {}

    public static void loadPersistedValues(String[] args) {
        Path appHome = Path.of(System.getProperty("app.home", ".")).toAbsolutePath().normalize();
        ConfigSnapshotStore store = new ConfigSnapshotStore(appHome, new ObjectMapper(), new DpapiSecretProtector());
        ConfigSnapshotStore.Snapshot current = store.current();
        Map<String, String> allValues = new LinkedHashMap<>(current.values());
        allValues.putAll(store.readSecrets(current));
        collectCommandLineOverrides(args).forEach(CLI_OVERRIDDEN_KEYS::add);
        allValues.forEach((key, value) -> {
            if (!isExternalOverride(key) && value != null) {
                System.setProperty(key, value);
                CONSOLE_LOADED_KEYS.add(key);
            }
        });
    }

    public static String sourceOf(String key) {
        if (CONSOLE_LOADED_KEYS.contains(key)) {
            return "控制台配置";
        }
        if (CLI_OVERRIDDEN_KEYS.contains(key)) {
            return "命令行";
        }
        if (System.getProperty(key) != null) {
            return "系统属性";
        }
        if (hasEnvironmentOverride(key)) {
            return "环境变量";
        }
        return "配置文件/默认值";
    }

    private static boolean isExternalOverride(String key) {
        return CLI_OVERRIDDEN_KEYS.contains(key)
                || System.getProperty(key) != null && !CONSOLE_LOADED_KEYS.contains(key)
                || hasEnvironmentOverride(key);
    }

    private static Set<String> collectCommandLineOverrides(String[] args) {
        Set<String> keys = ConcurrentHashMap.newKeySet();
        if (args == null) {
            return keys;
        }
        Arrays.stream(args)
                .filter(arg -> arg.startsWith("--") && arg.contains("="))
                .map(arg -> arg.substring(2, arg.indexOf('=')))
                .forEach(keys::add);
        return keys;
    }

    private static boolean hasEnvironmentOverride(String key) {
        String normalized = key.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
        if (System.getenv(normalized) != null) {
            return true;
        }
        String shiyuName = normalized.startsWith("SHIYU_") ? normalized : "SHIYU_" + normalized;
        if (System.getenv(shiyuName) != null) {
            return true;
        }
        return System.getenv().entrySet().stream()
                .anyMatch(entry -> isExistingAlias(key, entry.getKey()));
    }

    private static boolean isExistingAlias(String key, String envName) {
        return switch (key) {
            case "shiyu.ai.openai.base-url" -> "AI_OPENAI_BASE_URL".equals(envName);
            case "shiyu.ai.openai.api-key" -> "AI_OPENAI_API_KEY".equals(envName);
            case "shiyu.ai.openai.model" -> "AI_OPENAI_MODEL".equals(envName);
            case "shiyu.ai.deepseek.base-url" -> "AI_DEEPSEEK_BASE_URL".equals(envName);
            case "shiyu.ai.deepseek.api-key" -> "AI_DEEPSEEK_API_KEY".equals(envName);
            case "shiyu.ai.deepseek.model" -> "AI_DEEPSEEK_MODEL".equals(envName);
            case "shiyu.modules.education.enabled" -> "SHIYU_MODULE_EDUCATION_ENABLED".equals(envName);
            case "shiyu.infrastructure.database.provider" -> "SHIYU_INFRA_DATABASE_PROVIDER".equals(envName);
            case "shiyu.infrastructure.file.provider" -> "SHIYU_INFRA_FILE_PROVIDER".equals(envName);
            case "shiyu.infrastructure.vector.provider" -> "SHIYU_INFRA_VECTOR_PROVIDER".equals(envName);
            case "shiyu.infrastructure.redis.provider" -> "SHIYU_INFRA_REDIS_PROVIDER".equals(envName);
            case "shiyu.infrastructure.event.provider" -> "SHIYU_INFRA_EVENT_PROVIDER".equals(envName);
            default -> false;
        };
    }
}
