package com.shiyu.ai.runtimeconsole.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 在 Spring 创建应用 Bean 前加载已持久化且获准的配置值。 */
public final class ConsoleBootstrapConfiguration {

    private static final Set<String> CONSOLE_LOADED_KEYS = ConcurrentHashMap.newKeySet();
    private static final Set<String> CLI_OVERRIDDEN_KEYS = ConcurrentHashMap.newKeySet();
    private static volatile String DATABASE_PROFILE_SOURCE;

    private ConsoleBootstrapConfiguration() {}

    static void resetForTests() {
        CONSOLE_LOADED_KEYS.clear();
        CLI_OVERRIDDEN_KEYS.clear();
        DATABASE_PROFILE_SOURCE = null;
    }

    public static String[] loadPersistedValues(String[] args) {
        Path appHome = Path.of(System.getProperty("app.home", ".")).toAbsolutePath().normalize();
        ConfigSnapshotStore store = new ConfigSnapshotStore(appHome, new ObjectMapper(), new DpapiSecretProtector());
        ConfigSnapshotStore.Snapshot current = store.current();
        Map<String, String> allValues = new LinkedHashMap<>(current.values());
        allValues.putAll(store.readSecrets(current));
        collectCommandLineOverrides(args).forEach(CLI_OVERRIDDEN_KEYS::add);
        String profileProvider = activeDatabaseProfile(args);
        boolean externalProvider = isExternalDatabaseProvider(args);
        if (profileProvider != null && !externalProvider) {
            System.setProperty("shiyu.infrastructure.database.provider", profileProvider);
            DATABASE_PROFILE_SOURCE = profileSource(args);
        }
        allValues.forEach((key, value) -> {
            if (key.equals("shiyu.infrastructure.database.provider")
                    && profileProvider != null && !externalProvider) {
                return;
            }
            if (!isExternalOverride(key) && value != null) {
                System.setProperty(key, value);
                CONSOLE_LOADED_KEYS.add(key);
            }
        });
        String selectedProvider = selectedDatabaseProvider(args, current, profileProvider);
        if (Set.of("mysql", "postgresql").contains(selectedProvider) && profileProvider == null) {
            ArrayList<String> prepared = new ArrayList<>(args == null ? List.of() : Arrays.asList(args));
            addDatabaseProfile(prepared, selectedProvider);
            return prepared.toArray(String[]::new);
        }
        return args == null ? new String[0] : args.clone();
    }

    public static String sourceOf(String key) {
        if (key.equals("shiyu.infrastructure.database.provider") && DATABASE_PROFILE_SOURCE != null) {
            return DATABASE_PROFILE_SOURCE;
        }
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

    private static String selectedDatabaseProvider(String[] args, ConfigSnapshotStore.Snapshot current, String profileProvider) {
        String commandLine = commandLineValue(args, "shiyu.infrastructure.database.provider");
        if (commandLine != null) return commandLine.toLowerCase(Locale.ROOT);
        String systemValue = System.getProperty("shiyu.infrastructure.database.provider");
        if (systemValue != null && !systemValue.isBlank()) return systemValue.toLowerCase(Locale.ROOT);
        String environmentValue = firstEnvironmentValue("SHIYU_INFRA_DATABASE_PROVIDER", "SHIYU_INFRASTRUCTURE_DATABASE_PROVIDER");
        if (environmentValue != null && !environmentValue.isBlank()) return environmentValue.toLowerCase(Locale.ROOT);
        if (profileProvider != null) return profileProvider;
        String saved = current.values().get("shiyu.infrastructure.database.provider");
        return saved == null || saved.isBlank() ? "h2" : saved.toLowerCase(Locale.ROOT);
    }

    private static String activeDatabaseProfile(String[] args) {
        List<String> configuredProfiles = new ArrayList<>(profileValues(args, "spring.profiles.active"));
        configuredProfiles.addAll(profileValues(args, "spring.profiles.include"));
        String profiles = String.join(",", configuredProfiles);
        for (String profile : profiles.split("[,;\\s]+")) {
            if (profile.equalsIgnoreCase("mysql") || profile.equalsIgnoreCase("postgresql")) {
                return profile.toLowerCase(Locale.ROOT);
            }
        }
        return null;
    }

    private static String profileSource(String[] args) {
        if (commandLineValue(args, "spring.profiles.active") != null
                || commandLineValue(args, "profiles.active") != null
                || commandLineValue(args, "spring.profiles.include") != null) return "命令行";
        if (System.getProperty("spring.profiles.active") != null
                || System.getProperty("profiles.active") != null
                || System.getProperty("spring.profiles.include") != null) return "系统属性";
        if (firstEnvironmentValue("SPRING_PROFILES_ACTIVE", "PROFILES_ACTIVE", "SPRING_PROFILES_INCLUDE") != null) return "环境变量";
        return "配置文件/默认值";
    }

    private static List<String> profileValues(String[] args, String key) {
        String commandLine = commandLineValue(args, key);
        if (commandLine == null && key.equals("spring.profiles.active")) {
            commandLine = commandLineValue(args, "profiles.active");
        }
        if (commandLine != null) return List.of(commandLine);
        String system = System.getProperty(key);
        if (system == null && key.equals("spring.profiles.active")) {
            system = System.getProperty("profiles.active");
        }
        if (system != null) return List.of(system);
        String envName = key.equals("spring.profiles.active") ? "SPRING_PROFILES_ACTIVE" : "SPRING_PROFILES_INCLUDE";
        String environment = firstEnvironmentValue(envName, key.equals("spring.profiles.active") ? "PROFILES_ACTIVE" : "SPRING_PROFILES_INCLUDE");
        return environment == null ? List.of() : List.of(environment);
    }

    private static boolean isExternalDatabaseProvider(String[] args) {
        return commandLineValue(args, "shiyu.infrastructure.database.provider") != null
                || System.getProperty("shiyu.infrastructure.database.provider") != null
                || hasEnvironmentOverride("shiyu.infrastructure.database.provider");
    }

    private static void addDatabaseProfile(List<String> args, String databaseProfile) {
        String include = commandLineValue(args.toArray(String[]::new), "spring.profiles.include");
        String prefix = "--spring.profiles.include=";
        if (include == null) {
            include = System.getProperty("spring.profiles.include");
        }
        if (include == null) {
            include = System.getProperty("profiles.include");
        }
        if (include == null) {
            include = firstEnvironmentValue("SPRING_PROFILES_INCLUDE");
        }
        List<String> profiles = new ArrayList<>();
        if (include != null) {
            Arrays.stream(include.split("[,;\\s]+"))
                    .filter(profile -> !profile.isBlank())
                    .forEach(profiles::add);
        }
        if (profiles.stream().noneMatch(databaseProfile::equalsIgnoreCase)) {
            profiles.add(databaseProfile);
        }
        String merged = String.join(",", profiles);
        for (int index = 0; index < args.size(); index++) {
            if (args.get(index).startsWith(prefix)) {
                args.set(index, prefix + merged);
                return;
            }
        }
        args.add(prefix + merged);
    }

    private static String commandLineValue(String[] args, String key) {
        if (args == null) return null;
        String prefix = "--" + key + "=";
        return Arrays.stream(args).filter(arg -> arg.startsWith(prefix))
                .map(arg -> arg.substring(prefix.length())).findFirst().orElse(null);
    }

    private static String firstEnvironmentValue(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (value != null) return value;
        }
        return null;
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
        if (key.startsWith("shiyu.storage.providers.")) {
            String[] parts = key.substring("shiyu.storage.providers.".length()).split("\\.", 2);
            if (parts.length == 2) {
                String provider = switch (parts[0]) {
                    case "s3" -> "S3";
                    case "minio" -> "MINIO";
                    case "aliyun-oss" -> "OSS";
                    case "tencent-cos" -> "COS";
                    default -> "";
                };
                if (!provider.isEmpty()) {
                    String property = parts[1].toUpperCase(Locale.ROOT).replace('-', '_');
                    if ("tencent-cos".equals(parts[0]) && "access_key".equals(property)) {
                        property = "SECRET_ID";
                    }
                    return ("SHIYU_STORAGE_" + provider + "_" + property).equals(envName);
                }
            }
        }
        return switch (key) {
            case "shiyu.ai.openai.base-url" -> "AI_OPENAI_BASE_URL".equals(envName);
            case "shiyu.ai.openai.api-key" -> "AI_OPENAI_API_KEY".equals(envName);
            case "shiyu.ai.openai.model" -> "AI_OPENAI_MODEL".equals(envName);
            case "shiyu.ai.deepseek.base-url" -> "AI_DEEPSEEK_BASE_URL".equals(envName);
            case "shiyu.ai.deepseek.api-key" -> "AI_DEEPSEEK_API_KEY".equals(envName);
            case "shiyu.ai.deepseek.model" -> "AI_DEEPSEEK_MODEL".equals(envName);
            case "shiyu.modules.education.enabled" -> "SHIYU_MODULE_EDUCATION_ENABLED".equals(envName);
            case "shiyu.infrastructure.database.provider" -> "SHIYU_INFRA_DATABASE_PROVIDER".equals(envName);
            case "mybatis-flex.datasource.agent.url" -> "SHIYU_DB_URL".equals(envName);
            case "mybatis-flex.datasource.agent.username" -> "SHIYU_DB_USERNAME".equals(envName);
            case "mybatis-flex.datasource.agent.password" -> "SHIYU_DB_PASSWORD".equals(envName);
            case "shiyu.infrastructure.file.provider" -> "SHIYU_INFRA_FILE_PROVIDER".equals(envName);
            case "shiyu.storage.type" -> "SHIYU_STORAGE_TYPE".equals(envName);
            case "shiyu.storage.local.path" -> "SHIYU_STORAGE_LOCAL_PATH".equals(envName);
            case "shiyu.infrastructure.vector.provider" -> "SHIYU_INFRA_VECTOR_PROVIDER".equals(envName);
            case "shiyu.vector-store.type" -> "SHIYU_VECTOR_STORE_TYPE".equals(envName);
            case "shiyu.infrastructure.redis.provider" -> "SHIYU_INFRA_REDIS_PROVIDER".equals(envName);
            case "shiyu.infrastructure.redis.url" -> "SHIYU_REDIS_URL".equals(envName);
            case "shiyu.infrastructure.redis.password" -> "SHIYU_REDIS_PASSWORD".equals(envName);
            case "shiyu.infrastructure.redis.key-prefix" -> "SHIYU_REDIS_KEY_PREFIX".equals(envName);
            case "shiyu.infrastructure.event.provider" -> "SHIYU_INFRA_EVENT_PROVIDER".equals(envName);
            case "shiyu.infrastructure.event.topic" -> "SHIYU_KAFKA_TOPIC".equals(envName);
            case "shiyu.infrastructure.event.bootstrap-servers" -> "SHIYU_KAFKA_BOOTSTRAP_SERVERS".equals(envName);
            case "shiyu.infrastructure.event.dead-letter-topic" -> "SHIYU_KAFKA_DEAD_LETTER_TOPIC".equals(envName);
            case "shiyu.infrastructure.event.relay-interval-ms" -> "SHIYU_KAFKA_RELAY_INTERVAL_MS".equals(envName);
            case "shiyu.infrastructure.event.relay-batch-size" -> "SHIYU_KAFKA_RELAY_BATCH_SIZE".equals(envName);
            case "shiyu.infrastructure.event.max-attempts" -> "SHIYU_KAFKA_MAX_ATTEMPTS".equals(envName);
            case "server.port" -> "SHIYU_SERVER_PORT".equals(envName);
            default -> false;
        };
    }
}
