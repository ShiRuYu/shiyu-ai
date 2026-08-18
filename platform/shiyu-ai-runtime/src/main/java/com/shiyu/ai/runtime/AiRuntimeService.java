package com.shiyu.ai.runtime;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import com.shiyu.ai.common.core.utils.JSONUtils;

@Service
public class AiRuntimeService {
    private final AiRunRepository runs;
    private final AiAppRepository apps;

    public AiRuntimeService() {
        this(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
    }

    @Autowired
    public AiRuntimeService(AiRunRepository runs, AiAppRepository apps) {
        this.runs = runs;
        this.apps = apps;
    }

    public AiApp createApp(long tenantId, long ownerUserId, String name, String description) {
        AiApp app = new AiApp(UUID.randomUUID().toString(), tenantId, ownerUserId, name, description, "ACTIVE", null, Instant.now(), Instant.now());
        apps.insert(app);
        return app;
    }

    public AiApp requireApp(String id, long tenantId, long ownerUserId) { return apps.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("app not found")); }
    public List<AiApp> listApps(long tenantId, long ownerUserId, int limit) { return apps.list(tenantId, ownerUserId, limit); }
    public AiAppVersion createVersion(String appId, long tenantId, long ownerUserId, String version, String configJson) {
        requireApp(appId, tenantId, ownerUserId);
        AiAppVersion value = new AiAppVersion(UUID.randomUUID().toString(), appId, tenantId, version, configJson, "DRAFT", Instant.now(), null);
        apps.insertVersion(value);
        return value;
    }
    public List<AiAppVersion> versions(String appId, long tenantId, long ownerUserId) { requireApp(appId, tenantId, ownerUserId); return apps.versions(appId, tenantId); }
    public AiAppVersion publish(String appId, String versionId, long tenantId, long ownerUserId) {
        requireApp(appId, tenantId, ownerUserId);
        AiAppVersion version = apps.findVersion(appId, versionId, tenantId).orElseThrow(() -> new IllegalArgumentException("app version not found"));
        if (version.published()) throw new IllegalStateException("published app version is immutable; create a new draft version");
        if ("ARCHIVED".equals(version.status())) throw new IllegalStateException("archived app version cannot publish");
        validateConfig(version.configJson());
        if (apps.publishVersion(appId, versionId, tenantId) != 1) throw new IllegalStateException("app version publish conflict");
        return apps.findVersion(appId, versionId, tenantId).orElseThrow();
    }
    public AiAppVersion archive(String appId, String versionId, long tenantId, long ownerUserId) {
        AiApp app = requireApp(appId, tenantId, ownerUserId);
        if (versionId.equals(app.publishedVersionId())) throw new IllegalStateException("published app version must be replaced before archive");
        apps.archiveVersion(appId, versionId, tenantId);
        return apps.findVersion(appId, versionId, tenantId).orElseThrow();
    }
    public AiAppPreview preview(String appId, String versionId, long tenantId, long ownerUserId, String prompt) {
        AiApp app = requireApp(appId, tenantId, ownerUserId);
        AiAppVersion version = apps.findVersion(appId, versionId, tenantId).orElseThrow(() -> new IllegalArgumentException("app version not found"));
        Map<String,Object> config;
        try { config = version.configJson() == null || version.configJson().isBlank() ? Map.of() : JSONUtils.parseObject(version.configJson(), Map.class); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("app version configuration is not valid JSON", ex); }
        Object model = config.get("model");
        boolean executable = "PUBLISHED".equals(version.status()) && version.id().equals(app.publishedVersionId());
        return new AiAppPreview(app.id(), version.id(), version.status(), hash(prompt), model == null ? null : String.valueOf(model), config, executable);
    }

    public AiAppVersion requirePublishedVersion(String appId, long tenantId, long ownerUserId) {
        AiApp app = requireApp(appId, tenantId, ownerUserId);
        String versionId = app.publishedVersionId();
        if (versionId == null || versionId.isBlank()) throw new IllegalStateException("app has no published version");
        AiAppVersion version = apps.findVersion(appId, versionId, tenantId).orElseThrow(() -> new IllegalStateException("published app version not found"));
        if (!"PUBLISHED".equals(version.status())) throw new IllegalStateException("app version is not published");
        return version;
    }

    public AiRun startRun(AiRunContext context, AiRunSource source, String sourceId, String model, String prompt) {
        if (context.appId() != null || context.appVersionId() != null) {
            if (context.appId() == null || context.appVersionId() == null) {
                throw new IllegalArgumentException("app and app version must be provided together");
            }
            AiApp app = requireApp(context.appId(), context.tenantId(), context.ownerUserId());
            AiAppVersion version = apps.findVersion(context.appId(), context.appVersionId(), context.tenantId())
                    .orElseThrow(() -> new IllegalArgumentException("app version not found"));
            if (!"PUBLISHED".equals(version.status()) || app.publishedVersionId() == null
                    || !context.appVersionId().equals(app.publishedVersionId())) {
                throw new IllegalStateException("only the published app version can run");
            }
        }
        AiRun run = new AiRun(UUID.randomUUID().toString(), context.tenantId(), context.ownerUserId(), context.appId(), context.appVersionId(), source, sourceId,
                null, context.traceId() == null || context.traceId().isBlank() ? UUID.randomUUID().toString() : context.traceId(),
                context.conversationId(), context.generationId(), context.executionId(), model, hash(prompt), AiRunStatus.CREATED,
                0, 0, true, null, Instant.now(), null, null, 0);
        runs.insert(run);
        AiRun running = run.transition(AiRunStatus.RUNNING);
        if (runs.update(running, run.version()) != 1) throw new IllegalStateException("run admission conflict");
        try {
            append(running, AiRunEventType.RUN_STARTED, JSONUtils.toJsonString(Map.of("schemaVersion", 1, "source", source.name())), true);
        } catch (RuntimeException eventFailure) {
            try { finish(running.id(), running.tenantId(), running.ownerUserId(), AiRunStatus.FAILED, "RUN_EVENT_WRITE_FAILED"); }
            catch (RuntimeException ignored) { }
            throw eventFailure;
        }
        return running;
    }

    public AiRun finish(String id, long tenantId, long ownerUserId, AiRunStatus terminal, String errorCode) {
        AiRun current = runs.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found"));
        if (terminal != AiRunStatus.COMPLETED && terminal != AiRunStatus.FAILED && terminal != AiRunStatus.CANCELLED) {
            throw new IllegalArgumentException("run finish status must be terminal");
        }
        if (current.status() == AiRunStatus.COMPLETED || current.status() == AiRunStatus.FAILED || current.status() == AiRunStatus.CANCELLED) {
            // Terminal transitions are idempotent from the API perspective.  A
            // late cancel/failure must not rewrite the persisted terminal fact
            // or append a second terminal event; callers receive the existing
            // result instead.
            return current;
        }
        AiRun next = current.transition(terminal);
        next = new AiRun(next.id(), next.tenantId(), next.ownerUserId(), next.appId(), next.appVersionId(), next.sourceType(), next.sourceId(), next.parentRunId(), next.traceId(), next.conversationId(), next.generationId(), next.executionId(), next.model(), next.promptHash(), next.status(), next.promptTokens(), next.completionTokens(), next.estimatedUsage(), next.costSnapshot(), next.createdAt(), Instant.now(), errorCode, next.version(), next.lastEventSeq());
        AiRunEventType terminalEvent = terminal == AiRunStatus.COMPLETED ? AiRunEventType.RUN_COMPLETED
                : terminal == AiRunStatus.CANCELLED ? AiRunEventType.RUN_CANCELLED : AiRunEventType.RUN_FAILED;
        return runs.updateTerminalAndAppend(next, current.version(), terminalEvent,
                errorCode == null ? "{}" : JSONUtils.toJsonString(Map.of("errorCode", errorCode)), true);
    }

    public List<AiRunEvent> events(String id, long tenantId, long ownerUserId, long afterSeq, int limit) { return runs.events(id, tenantId, ownerUserId, afterSeq, Math.max(1, Math.min(limit, 1000))); }
    public List<AiRun> listRuns(long tenantId, long ownerUserId, int limit) { return runs.list(tenantId, ownerUserId, limit); }
    public AiRun requireRun(String id, long tenantId, long ownerUserId) { return runs.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found")); }
    public AiRun requireGenerationRun(String generationId, long tenantId, long ownerUserId) { return runs.findByGeneration(generationId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("runtime run not found")); }
    public AiRun linkGeneration(AiRun run, String generationId) {
        if (run == null || generationId == null || generationId.isBlank()) throw new IllegalArgumentException("runtime run and generation id are required");
        if (run.generationId() != null && !run.generationId().equals(generationId)) throw new IllegalStateException("runtime run is already linked to another generation");
        if (run.generationId() == null && runs.linkGeneration(run.id(), run.tenantId(), run.ownerUserId(), generationId) != 1) {
            throw new IllegalStateException("runtime generation link was modified");
        }
        return runs.find(run.id(), run.tenantId(), run.ownerUserId()).orElse(run);
    }
    public AiRun requireExecutionRun(String executionId, long tenantId, long ownerUserId) { return runs.findByExecution(executionId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("runtime run not found")); }
    public AiRun recordUsage(String id, long tenantId, long ownerUserId, long promptTokens, long completionTokens, boolean estimated, String costSnapshot) {
        AiRun current = requireRun(id, tenantId, ownerUserId);
        if (current.status() == AiRunStatus.COMPLETED || current.status() == AiRunStatus.FAILED || current.status() == AiRunStatus.CANCELLED) {
            throw new IllegalStateException("cannot record usage after run is terminal");
        }
        AiRun next = new AiRun(current.id(), current.tenantId(), current.ownerUserId(), current.appId(), current.appVersionId(), current.sourceType(), current.sourceId(), current.parentRunId(), current.traceId(), current.conversationId(), current.generationId(), current.executionId(), current.model(), current.promptHash(), current.status(), Math.max(0, promptTokens), Math.max(0, completionTokens), estimated, costSnapshot, current.createdAt(), current.completedAt(), current.errorCode(), current.version() + 1, current.lastEventSeq());
        if (runs.update(next, current.version()) != 1) throw new IllegalStateException("run usage was modified");
        return next;
    }
    public long append(AiRun run, AiRunEventType type, String payload, boolean redacted) {
        long seq = runs.appendNextEvent(run.id(), run.tenantId(), run.ownerUserId(), type,
                payload == null ? "{}" : payload, redacted, Instant.now());
        return seq;
    }

    public long append(AiRun run, AiRunEventType type, String payload, boolean redacted,
                       String turnId, String stepId, String providerRequestId) {
        return runs.appendNextEvent(run.id(), run.tenantId(), run.ownerUserId(), type,
                payload == null ? "{}" : payload, redacted, Instant.now(), turnId, stepId, providerRequestId);
    }

    private void validateConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) return;
        try {
            Object parsed = JSONUtils.parseObject(configJson, Object.class);
            if (!(parsed instanceof Map<?, ?> raw)) throw new IllegalArgumentException("app version configuration must be a JSON object");
            Map<?, ?> config = raw;
            validateCollection(config, "modelRoute", "model route must contain at least one model");
            validateCollection(config, "knowledgeSpaces", "knowledge space binding cannot be empty");
            validateCollection(config, "tools", "tool binding cannot be empty");
            validateExecutableBindings(config);
            validateBudget(config.get("budget"));
            validatePublishChecks(config.get("validation"));
            validateEvaluationGate(config.get("evaluation"));
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException && ex.getMessage() != null && !ex.getMessage().contains("configuration is not valid JSON")) throw ex;
            throw new IllegalArgumentException("app version configuration is not valid JSON", ex);
        }
    }

    /**
     * A published executable app must fail closed before its first run.  The
     * light-weight runtime does not own Agent/Knowledge repositories, so it
     * validates the immutable binding contract here and leaves object-level
     * authorization to the owning platform at execution time.
     */
    private void validateExecutableBindings(Map<?, ?> config) {
        Object executionType = config.get("executionType");
        boolean agentExecution = executionType != null && "AGENT".equalsIgnoreCase(String.valueOf(executionType));
        Object agentId = config.get("agentId");
        if (agentExecution && (agentId == null || String.valueOf(agentId).isBlank())) {
            throw new IllegalArgumentException("agent execution requires agentId");
        }
        if (agentId != null && String.valueOf(agentId).isBlank()) {
            throw new IllegalArgumentException("agentId cannot be blank");
        }
        Map<?, ?> checks = config.get("validation") instanceof Map<?, ?> value ? value : Map.of();
        if (agentId != null && !isPass(checks.get("graph"))) throw new IllegalArgumentException("publish validation failed: graph is required for agent binding");
        if (config.containsKey("model") || config.containsKey("modelRoute")) {
            if (config.get("model") instanceof String model && model.isBlank()) throw new IllegalArgumentException("model cannot be blank");
            if (config.containsKey("validation") && !isPass(checks.get("model"))) throw new IllegalArgumentException("publish validation failed: model");
        }
        if (config.containsKey("knowledgeSpaces") && !isPass(checks.get("knowledge"))) throw new IllegalArgumentException("publish validation failed: knowledge");
        if (config.containsKey("tools") && !isPass(checks.get("tools"))) throw new IllegalArgumentException("publish validation failed: tools");
    }

    private boolean isPass(Object value) { return value != null && "PASS".equalsIgnoreCase(String.valueOf(value)); }

    private void validateCollection(Map<?, ?> config, String key, String message) {
        if (!config.containsKey(key)) return;
        Object value = config.get(key);
        if (!(value instanceof java.util.Collection<?> collection) || collection.isEmpty()
                || collection.stream().anyMatch(item -> item == null || String.valueOf(item).isBlank())) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateBudget(Object raw) {
        if (raw == null) return;
        if (!(raw instanceof Map<?, ?> budget)) throw new IllegalArgumentException("budget must be an object");
        for (String key : List.of("maxTokens", "maxCost")) {
            Object value = budget.get(key);
            if (value instanceof Number number && number.doubleValue() < 0) throw new IllegalArgumentException("budget values cannot be negative");
        }
    }

    private void validatePublishChecks(Object raw) {
        if (raw == null) return;
        if (!(raw instanceof Map<?, ?> checks)) throw new IllegalArgumentException("validation must be an object");
        for (String key : List.of("graph", "model", "knowledge", "tools", "budget")) {
            Object value = checks.get(key);
            if (value != null && !"PASS".equalsIgnoreCase(String.valueOf(value))) {
                throw new IllegalArgumentException("publish validation failed: " + key);
            }
        }
    }

    private void validateEvaluationGate(Object raw) {
        if (raw == null) return;
        if (!(raw instanceof Map<?, ?> evaluation)) throw new IllegalArgumentException("evaluation must be an object");
        Object thresholdValue = evaluation.get("requiredPassRate");
        Object actualValue = evaluation.get("passRate");
        if (thresholdValue == null) return;
        if (!(thresholdValue instanceof Number threshold) || threshold.doubleValue() < 0 || threshold.doubleValue() > 1) {
            throw new IllegalArgumentException("evaluation requiredPassRate must be between 0 and 1");
        }
        if (!(actualValue instanceof Number actual) || actual.doubleValue() < threshold.doubleValue()) {
            throw new IllegalArgumentException("evaluation pass rate is below publish threshold");
        }
    }

    private String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
}
