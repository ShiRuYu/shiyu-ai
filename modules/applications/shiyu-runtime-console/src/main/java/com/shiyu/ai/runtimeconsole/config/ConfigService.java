package com.shiyu.ai.runtimeconsole.config;

import com.shiyu.ai.runtimeconsole.config.ConfigSnapshotStore.Snapshot;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.Environment;

/** Whitelist registry, source reporting, validation and versioned configuration changes. */
public final class ConfigService {

    private static final List<Field> FIELDS = List.of(
            new Field("logging.level.com.shiyu", "ShiYu 日志级别", "level", "IMMEDIATE", false),
            new Field("shiyu.console.sample-interval-ms", "控制台采样周期（毫秒）", "number", "IMMEDIATE", false),
            new Field("server.port", "HTTP 端口", "number", "RESTART", false),
            new Field("shiyu.modules.education.enabled", "教育模块", "boolean", "RESTART", false),
            new Field("shiyu.ai.openai.base-url", "启动模型地址 · OpenAI", "text", "RESTART", false),
            new Field("shiyu.ai.openai.model", "启动模型名称 · OpenAI", "text", "RESTART", false),
            new Field("shiyu.ai.openai.api-key", "启动模型密钥 · OpenAI", "secret", "RESTART", true),
            new Field("shiyu.ai.deepseek.base-url", "启动模型地址 · DeepSeek", "text", "RESTART", false),
            new Field("shiyu.ai.deepseek.model", "启动模型名称 · DeepSeek", "text", "RESTART", false),
            new Field("shiyu.ai.deepseek.api-key", "启动模型密钥 · DeepSeek", "secret", "RESTART", true),
            new Field("shiyu.infrastructure.database.provider", "数据库 Provider", "select", "RESTART", false),
            new Field("spring.datasource.url", "数据库连接 URL", "text", "RESTART", false),
            new Field("spring.datasource.username", "数据库用户名", "text", "RESTART", false),
            new Field("spring.datasource.password", "数据库密码", "secret", "RESTART", true),
            new Field("shiyu.storage.type", "文件存储 Provider", "select", "RESTART", false),
            new Field("shiyu.vector-store.type", "向量存储 Provider", "select", "RESTART", false),
            new Field("shiyu.infrastructure.redis.provider", "Redis Provider", "select", "RESTART", false),
            new Field("shiyu.infrastructure.redis.url", "Redis 连接 URL", "text", "RESTART", false),
            new Field("shiyu.infrastructure.redis.password", "Redis 密码", "secret", "RESTART", true),
            new Field("shiyu.infrastructure.event.provider", "事件 Provider", "select", "RESTART", false),
            new Field("shiyu.infrastructure.event.bootstrap-servers", "事件服务地址", "text", "RESTART", false),
            new Field("shiyu.infrastructure.event.topic", "事件主题", "text", "RESTART", false));

    private static final Set<String> FIELD_KEYS = FIELDS.stream().map(Field::key).collect(java.util.stream.Collectors.toUnmodifiableSet());
    private static final Set<String> IMMEDIATE_KEYS = FIELDS.stream().filter(field -> field.applyMode().equals("IMMEDIATE")).map(Field::key).collect(java.util.stream.Collectors.toUnmodifiableSet());

    private final ConfigSnapshotStore store;
    private final Environment environment;
    private final RuntimeConfigApplier applier;

    public ConfigService(ConfigSnapshotStore store, Environment environment, RuntimeConfigApplier applier) {
        this.store = store;
        this.environment = environment;
        this.applier = applier;
    }

    public ConfigResponse describe() {
        Snapshot snapshot = store.current();
        List<ConfigFieldDescriptor> descriptors = new ArrayList<>();
        for (Field field : FIELDS) {
            String effective = applier.effectiveValue(field.key(), environment.getProperty(field.key(), ""));
            String saved = snapshot.values().getOrDefault(field.key(), effective);
            boolean savedSecret = snapshot.encryptedSecrets().containsKey(field.key());
            boolean configured = field.sensitive()
                    ? savedSecret || !effective.isBlank()
                    : !saved.isBlank();
            Support support = support(field, effective);
            descriptors.add(new ConfigFieldDescriptor(
                    field.key(),
                    field.label(),
                    field.type(),
                    field.applyMode(),
                    field.sensitive(),
                    configSource(field.key(), snapshot),
                    support.supported(),
                    field.sensitive() ? (configured ? "已设置" : "未设置") : effective,
                    field.sensitive() ? "" : saved,
                    configured,
                    support.supported(),
                    support.reason()));
        }
        return new ConfigResponse(snapshot.revision(), descriptors);
    }

    public List<String> validate(ConfigChangeSet change) {
        Map<String, String> values = safeValues(change.values());
        List<String> issues = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!FIELD_KEYS.contains(key)) {
                issues.add("不允许修改字段：" + key);
                continue;
            }
            validateValue(key, value, issues);
        }
        validateSecrets(change.secrets(), issues);

        String db = candidateValue("shiyu.infrastructure.database.provider", values);
        String vector = candidateValue("shiyu.vector-store.type", values);
        if ("pgvector".equalsIgnoreCase(vector) && !"postgresql".equalsIgnoreCase(db)) {
            issues.add("pgvector 必须与 PostgreSQL 数据库组合使用");
        }
        String eventProvider = candidateValue("shiyu.infrastructure.event.provider", values);
        if ("kafka".equalsIgnoreCase(eventProvider) && !isProfileActive("external-infra")) {
            issues.add("Kafka 未装配：请使用 external-infra 发行配置后再提交");
        }
        String fileProvider = candidateValue("shiyu.storage.type", values);
        if (!Set.of("local", "s3", "minio", "aliyun-oss", "tencent-cos").contains(fileProvider.toLowerCase(java.util.Locale.ROOT))) {
            issues.add("不支持的文件存储 Provider：" + fileProvider);
        }
        if (!"local".equalsIgnoreCase(fileProvider) && !isProfileActive("s3")) {
            issues.add("当前发行包未装配该文件存储 Provider");
        }
        return List.copyOf(new LinkedHashSet<>(issues));
    }

    public ConfigApplyResult save(ConfigChangeSet change) {
        List<String> issues = validate(change);
        if (!issues.isEmpty()) {
            throw new ConfigValidationException(issues);
        }
        Snapshot previous = store.current();
        Map<String, String> submittedValues = safeValues(change.values());
        Map<String, String> immediate = new LinkedHashMap<>();
        for (String key : IMMEDIATE_KEYS) {
            if (submittedValues.containsKey(key)) {
                immediate.put(key, submittedValues.get(key));
            }
        }
        Map<String, String> priorImmediate = new LinkedHashMap<>();
        for (String key : IMMEDIATE_KEYS) {
            priorImmediate.put(key, environment.getProperty(key, key.equals("shiyu.console.sample-interval-ms") ? "5000" : "INFO"));
        }
        if (!immediate.isEmpty()) {
            applier.apply(immediate);
        }

        try {
            SecretEdits edits = secretEdits(change.secrets());
            Snapshot saved = store.save(change.expectedVersion(), submittedValues, edits.replacements(), edits.clears());
            boolean restartRequired = hasRestartChange(previous, saved);
            return new ConfigApplyResult(
                    restartRequired ? "PENDING_RESTART" : "APPLIED",
                    saved.revision(),
                    restartRequired,
                    restartRequired ? "已保存为 v" + saved.revision() + "；启动项待重启后生效。" : "即时配置已生效并保存。",
                    List.of());
        } catch (RuntimeException failure) {
            if (!immediate.isEmpty()) {
                applier.apply(priorImmediate);
            }
            throw failure;
        }
    }

    public ConfigApplyResult restoreLastApplied(long expectedVersion) {
        Snapshot restored = store.restoreLastApplied(expectedVersion);
        return new ConfigApplyResult(
                "PENDING_RESTART",
                restored.revision(),
                true,
                "已从上次成功版本恢复为 v" + restored.revision() + "；重启后应用。",
                List.of());
    }

    private void validateSecrets(Map<String, ConfigChangeSet.SecretChange> secrets, List<String> issues) {
        if (secrets == null) return;
        for (Map.Entry<String, ConfigChangeSet.SecretChange> entry : secrets.entrySet()) {
            Field field = FIELDS.stream().filter(item -> item.key().equals(entry.getKey())).findFirst().orElse(null);
            if (field == null || !field.sensitive()) {
                issues.add("不允许修改敏感字段：" + entry.getKey());
                continue;
            }
            ConfigChangeSet.SecretChange change = entry.getValue();
            if (change == null || change.action() == null
                    || !Set.of("keep", "replace", "clear").contains(change.action().toLowerCase(java.util.Locale.ROOT))) {
                issues.add("敏感字段操作必须为 keep、replace 或 clear：" + entry.getKey());
            } else if ("replace".equalsIgnoreCase(change.action())
                    && (change.value() == null || change.value().isBlank() || change.value().length() > 4096)) {
                issues.add("新密钥不能为空且不能超过 4096 个字符：" + field.label());
            }
        }
    }

    private void validateValue(String key, String value, List<String> issues) {
        Field field = FIELDS.stream().filter(item -> item.key().equals(key)).findFirst().orElse(null);
        if (field == null || field.sensitive()) {
            if (field != null) issues.add("敏感字段请通过替换/清除操作提交：" + field.label());
            return;
        }
        if (value == null || value.length() > 4096) {
            issues.add("字段值不能为空或超过 4096 个字符：" + field.label());
            return;
        }
        try {
            switch (key) {
                case "server.port" -> {
                    int port = Integer.parseInt(value);
                    if (port < 1 || port > 65535) issues.add("HTTP 端口必须为 1–65535");
                }
                case "shiyu.console.sample-interval-ms" -> {
                    long interval = Long.parseLong(value);
                    if (interval < 1_000 || interval > 60_000) issues.add("控制台采样周期必须为 1000–60000 毫秒");
                }
                case "logging.level.com.shiyu" -> {
                    if (!Set.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR").contains(value.toUpperCase(java.util.Locale.ROOT))) {
                        issues.add("日志级别必须为 TRACE、DEBUG、INFO、WARN 或 ERROR");
                    }
                }
                case "shiyu.modules.education.enabled" -> {
                    if (!Set.of("true", "false").contains(value.toLowerCase(java.util.Locale.ROOT))) issues.add("教育模块开关必须为 true 或 false");
                }
                case "shiyu.infrastructure.database.provider" -> {
                    if (!Set.of("h2", "mysql", "postgresql").contains(value.toLowerCase(java.util.Locale.ROOT))) issues.add("不支持的数据库 Provider");
                }
                case "shiyu.vector-store.type" -> {
                    if (!Set.of("jvector", "pgvector", "inmemory").contains(value.toLowerCase(java.util.Locale.ROOT))) issues.add("不支持的向量 Provider");
                }
                case "shiyu.infrastructure.event.provider" -> {
                    if (!Set.of("in-process", "kafka").contains(value.toLowerCase(java.util.Locale.ROOT))) issues.add("不支持的事件 Provider");
                }
                case "shiyu.infrastructure.redis.provider" -> {
                    if (!Set.of("disabled", "redis").contains(value.toLowerCase(java.util.Locale.ROOT))) issues.add("不支持的 Redis Provider");
                }
                case "shiyu.storage.type" -> {
                    if (value.isBlank()) issues.add("文件存储 Provider 不能为空");
                }
                case "shiyu.ai.openai.base-url", "shiyu.ai.deepseek.base-url" -> validateHttpUrl(field.label(), value, issues);
                case "spring.datasource.url" -> {
                    if (value.isBlank()) issues.add("数据库连接 URL 不能为空");
                }
                case "shiyu.infrastructure.redis.url" -> {
                    URI uri = new URI(value);
                    if (!Set.of("redis", "rediss").contains(String.valueOf(uri.getScheme()).toLowerCase(java.util.Locale.ROOT)) || uri.getHost() == null) {
                        issues.add("Redis URL 必须为 redis:// 或 rediss:// 地址");
                    }
                }
                default -> { }
            }
        } catch (NumberFormatException exception) {
            issues.add("数值格式无效：" + field.label());
        } catch (URISyntaxException exception) {
            issues.add("地址格式无效：" + field.label());
        }
    }

    private static void validateHttpUrl(String label, String value, List<String> issues) {
        try {
            URI uri = new URI(value);
            if (!Set.of("http", "https").contains(String.valueOf(uri.getScheme()).toLowerCase(java.util.Locale.ROOT))
                    || uri.getHost() == null) {
                issues.add(label + " 必须为有效的 HTTP(S) URL");
            }
        } catch (URISyntaxException exception) {
            issues.add(label + " 必须为有效的 HTTP(S) URL");
        }
    }

    private String candidateValue(String key, Map<String, String> values) {
        return values.getOrDefault(key, store.current().values().getOrDefault(key, environment.getProperty(key, "")));
    }

    private boolean isProfileActive(String name) {
        return java.util.Arrays.asList(environment.getActiveProfiles()).contains(name);
    }

    private String configSource(String key, Snapshot snapshot) {
        String source = ConsoleBootstrapConfiguration.sourceOf(key);
        if (source.equals("配置文件/默认值") && snapshot.values().containsKey(key)) {
            return "控制台配置";
        }
        if (source.equals("配置文件/默认值") && snapshot.encryptedSecrets().containsKey(key)) {
            return "控制台配置";
        }
        return source;
    }

    private Support support(Field field, String currentValue) {
        if (field.key().equals("shiyu.infrastructure.event.provider")
                && "kafka".equalsIgnoreCase(currentValue)
                && !isProfileActive("external-infra")) {
            return new Support(false, "当前发行包未装配 Kafka");
        }
        if (field.key().equals("shiyu.storage.type")
                && !"local".equalsIgnoreCase(currentValue)
                && !isProfileActive("s3")) {
            return new Support(false, "当前发行包仅包含本地文件存储");
        }
        if (field.key().equals("shiyu.vector-store.type")
                && "pgvector".equalsIgnoreCase(currentValue)
                && !"postgresql".equalsIgnoreCase(environment.getProperty("shiyu.infrastructure.database.provider", "h2"))) {
            return new Support(false, "pgvector 需要 PostgreSQL Provider");
        }
        return new Support(true, "");
    }

    private boolean hasRestartChange(Snapshot previous, Snapshot saved) {
        for (Field field : FIELDS) {
            if (!field.applyMode().equals("RESTART")) continue;
            if (field.sensitive()) {
                if (!java.util.Objects.equals(previous.encryptedSecrets().get(field.key()), saved.encryptedSecrets().get(field.key()))) return true;
            } else if (!java.util.Objects.equals(
                    previous.values().getOrDefault(field.key(), environment.getProperty(field.key(), "")),
                    saved.values().getOrDefault(field.key(), environment.getProperty(field.key(), "")))) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> safeValues(Map<String, String> values) {
        return values == null ? Map.of() : new LinkedHashMap<>(values);
    }

    private static SecretEdits secretEdits(Map<String, ConfigChangeSet.SecretChange> secrets) {
        Map<String, String> replacements = new LinkedHashMap<>();
        Set<String> clears = new LinkedHashSet<>();
        if (secrets != null) {
            secrets.forEach((key, edit) -> {
                if (edit == null || edit.action() == null) return;
                if ("replace".equalsIgnoreCase(edit.action())) replacements.put(key, edit.value());
                else if ("clear".equalsIgnoreCase(edit.action())) clears.add(key);
            });
        }
        return new SecretEdits(replacements, clears);
    }

    private record Field(String key, String label, String type, String applyMode, boolean sensitive) {}
    private record Support(boolean supported, String reason) {}
    private record SecretEdits(Map<String, String> replacements, Set<String> clears) {}
    public record ConfigResponse(long version, List<ConfigFieldDescriptor> fields) {}

    public static final class ConfigValidationException extends RuntimeException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        private final java.util.ArrayList<String> issues;
        public ConfigValidationException(List<String> issues) {
            super(String.join("; ", issues));
            this.issues = new java.util.ArrayList<>(issues);
        }
        public List<String> issues() { return List.copyOf(issues); }
    }
}
