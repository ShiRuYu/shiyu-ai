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
import java.util.concurrent.ConcurrentHashMap;
import com.shiyu.ai.common.core.utils.JSONUtils;

@Service
public class AiRuntimeService {
    private final AiRunRepository runs;
    private final AiAppRepository apps;
    private final Map<String, Object> eventLocks = new ConcurrentHashMap<>();

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
        validateConfig(version.configJson());
        apps.publishVersion(appId, versionId, tenantId);
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
        append(run, AiRunEventType.RUN_STARTED, "{}", true);
        AiRun running = run.transition(AiRunStatus.RUNNING);
        if (runs.update(running, run.version()) != 1) throw new IllegalStateException("run admission conflict");
        return running;
    }

    public AiRun finish(String id, long tenantId, long ownerUserId, AiRunStatus terminal, String errorCode) {
        AiRun current = runs.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found"));
        if (current.status() == AiRunStatus.COMPLETED || current.status() == AiRunStatus.FAILED || current.status() == AiRunStatus.CANCELLED) return current;
        AiRun next = current.transition(terminal);
        next = new AiRun(next.id(), next.tenantId(), next.ownerUserId(), next.appId(), next.appVersionId(), next.sourceType(), next.sourceId(), next.parentRunId(), next.traceId(), next.conversationId(), next.generationId(), next.executionId(), next.model(), next.promptHash(), next.status(), next.promptTokens(), next.completionTokens(), next.estimatedUsage(), next.costSnapshot(), next.createdAt(), Instant.now(), errorCode, next.version());
        if (runs.update(next, current.version()) != 1) throw new IllegalStateException("run was modified");
        append(next, terminal == AiRunStatus.COMPLETED ? AiRunEventType.RUN_COMPLETED : terminal == AiRunStatus.CANCELLED ? AiRunEventType.RUN_CANCELLED : AiRunEventType.RUN_FAILED, errorCode == null ? "{}" : "{\"errorCode\":\"" + errorCode + "\"}", true);
        return next;
    }

    public List<AiRunEvent> events(String id, long tenantId, long ownerUserId, long afterSeq, int limit) { return runs.events(id, tenantId, ownerUserId, afterSeq, Math.max(1, Math.min(limit, 1000))); }
    public AiRun requireRun(String id, long tenantId, long ownerUserId) { return runs.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("run not found")); }
    public AiRun requireGenerationRun(String generationId, long tenantId, long ownerUserId) { return runs.findByGeneration(generationId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("runtime run not found")); }
    public AiRun requireExecutionRun(String executionId, long tenantId, long ownerUserId) { return runs.findByExecution(executionId, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("runtime run not found")); }
    public AiRun recordUsage(String id, long tenantId, long ownerUserId, long promptTokens, long completionTokens, boolean estimated, String costSnapshot) {
        AiRun current = requireRun(id, tenantId, ownerUserId);
        AiRun next = new AiRun(current.id(), current.tenantId(), current.ownerUserId(), current.appId(), current.appVersionId(), current.sourceType(), current.sourceId(), current.parentRunId(), current.traceId(), current.conversationId(), current.generationId(), current.executionId(), current.model(), current.promptHash(), current.status(), Math.max(0, promptTokens), Math.max(0, completionTokens), estimated, costSnapshot, current.createdAt(), current.completedAt(), current.errorCode(), current.version() + 1);
        if (runs.update(next, current.version()) != 1) throw new IllegalStateException("run usage was modified");
        return next;
    }
    public long append(AiRun run, AiRunEventType type, String payload, boolean redacted) {
        Object lock = eventLocks.computeIfAbsent(run.id(), ignored -> new Object());
        synchronized (lock) {
            List<AiRunEvent> existing = runs.events(run.id(), run.tenantId(), run.ownerUserId(), 0, Integer.MAX_VALUE);
            long nextSeq = existing.stream().mapToLong(AiRunEvent::seq).max().orElse(0L) + 1L;
            return runs.appendEvent(new AiRunEvent(run.id(), run.tenantId(), nextSeq, type, payload == null ? "{}" : payload, redacted, Instant.now()));
        }
    }

    private void validateConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) return;
        try {
            Object parsed = JSONUtils.parseObject(configJson, Object.class);
            if (!(parsed instanceof Map<?, ?>)) throw new IllegalArgumentException("app version configuration must be a JSON object");
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("app version configuration is not valid JSON", ex);
        }
    }

    private String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException(e); } }
}
