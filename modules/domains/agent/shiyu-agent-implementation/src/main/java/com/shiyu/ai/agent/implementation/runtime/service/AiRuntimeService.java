package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiRunRepository;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppPreview;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.port.AiAppRepository;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * {@code AiRuntimeService} 定义智能体模块的应用服务能力，供上层用例调用。
 */
@Service
public class AiRuntimeService implements AiRuntimePort {
    /**
     * runs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRunRepository runs;
    /**
     * apps 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiAppRepository apps;

    /**
     * {@code AiRuntimeService} 创建并初始化当前类型实例。
     */
    public AiRuntimeService() {
        this(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
    }

    /**
     * {@code AiRuntimeService} 创建并初始化当前类型实例。
     *
     * @param runs 参数值，用于执行当前操作。
     * @param apps 参数值，用于执行当前操作。
     */
    @Autowired
    public AiRuntimeService(AiRunRepository runs, AiAppRepository apps) {
        this.runs = runs;
        this.apps = apps;
    }

    /**
     * {@code createApp} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param description 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiApp createApp(TenantId tenantId, long ownerUserId, String name, String description) {
        AiApp app =
                new AiApp(
                        UUID.randomUUID().toString(),
                        tenant(tenantId),
                        new UserId(ownerUserId),
                        name,
                        description,
                        "ACTIVE",
                        null,
                        Instant.now(),
                        Instant.now());
        apps.insert(app);
        return app;
    }

    /**
     * {@code requireApp} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiApp requireApp(String id, TenantId tenantId, long ownerUserId) {
        return apps.find(id, tenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("app not found"));
    }

    /**
     * {@code listApps} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<AiApp> listApps(TenantId tenantId, long ownerUserId, int limit) {
        return apps.list(tenant(tenantId), ownerUserId, limit);
    }

    /**
     * {@code createVersion} 写入或更新当前模块中的业务数据。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     * @param configJson 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiAppVersion createVersion(
            String appId, TenantId tenantId, long ownerUserId, String version, String configJson) {
        requireApp(appId, tenantId, ownerUserId);
        AiAppVersion value =
                new AiAppVersion(
                        UUID.randomUUID().toString(),
                        appId,
                        tenant(tenantId),
                        version,
                        configJson,
                        "DRAFT",
                        Instant.now(),
                        null);
        apps.insertVersion(value);
        return value;
    }

    /**
     * {@code versions} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<AiAppVersion> versions(String appId, TenantId tenantId, long ownerUserId) {
        requireApp(appId, tenantId, ownerUserId);
        return apps.versions(appId, tenant(tenantId));
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiAppVersion publish(
            String appId, String versionId, TenantId tenantId, long ownerUserId) {
        requireApp(appId, tenantId, ownerUserId);
        AiAppVersion version =
                apps.findVersion(appId, versionId, tenant(tenantId))
                        .orElseThrow(() -> new IllegalArgumentException("app version not found"));
        if (version.published())
            throw new IllegalStateException(
                    "published app version is immutable; create a new draft version");
        if ("ARCHIVED".equals(version.status()))
            throw new IllegalStateException("archived app version cannot publish");
        validateConfig(version.configJson());
        if (apps.publishVersion(appId, versionId, tenant(tenantId)) != 1)
            throw new IllegalStateException("app version publish conflict");
        return apps.findVersion(appId, versionId, tenant(tenantId)).orElseThrow();
    }

    /**
     * {@code archive} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiAppVersion archive(
            String appId, String versionId, TenantId tenantId, long ownerUserId) {
        AiApp app = requireApp(appId, tenantId, ownerUserId);
        if (versionId.equals(app.publishedVersionId()))
            throw new IllegalStateException(
                    "published app version must be replaced before archive");
        apps.archiveVersion(appId, versionId, tenant(tenantId));
        return apps.findVersion(appId, versionId, tenant(tenantId)).orElseThrow();
    }

    /**
     * {@code preview} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param prompt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiAppPreview preview(
            String appId, String versionId, TenantId tenantId, long ownerUserId, String prompt) {
        AiApp app = requireApp(appId, tenantId, ownerUserId);
        AiAppVersion version =
                apps.findVersion(appId, versionId, tenant(tenantId))
                        .orElseThrow(() -> new IllegalArgumentException("app version not found"));
        Map<String, Object> config;
        try {
            config =
                    version.configJson() == null || version.configJson().isBlank()
                            ? Map.of()
                            : JSONUtils.parseMap(version.configJson());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("app version configuration is not valid JSON", ex);
        }
        Object model = config.get("model");
        boolean executable =
                "PUBLISHED".equals(version.status())
                        && version.id().equals(app.publishedVersionId());
        return new AiAppPreview(
                app.id(),
                version.id(),
                version.status(),
                hash(prompt),
                model == null ? null : String.valueOf(model),
                config,
                executable);
    }

    /**
     * {@code requirePublishedVersion} 执行当前类型定义的业务操作。
     *
     * @param appId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiAppVersion requirePublishedVersion(String appId, TenantId tenantId, long ownerUserId) {
        AiApp app = requireApp(appId, tenantId, ownerUserId);
        String versionId = app.publishedVersionId();
        if (versionId == null || versionId.isBlank())
            throw new IllegalStateException("app has no published version");
        AiAppVersion version =
                apps.findVersion(appId, versionId, tenant(tenantId))
                        .orElseThrow(
                                () -> new IllegalStateException("published app version not found"));
        if (!"PUBLISHED".equals(version.status()))
            throw new IllegalStateException("app version is not published");
        return version;
    }

    /**
     * {@code startRun} 执行当前类型定义的业务操作。
     *
     * @param context 参数值，用于执行当前操作。
     * @param source 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param prompt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiRun startRun(
            AiRunContext context,
            AiRunSource source,
            String sourceId,
            String model,
            String prompt) {
        if (context.appId() != null || context.appVersionId() != null) {
            if (context.appId() == null || context.appVersionId() == null) {
                throw new IllegalArgumentException("app and app version must be provided together");
            }
            AiApp app = requireApp(context.appId(), context.tenantId(), context.ownerUserId());
            AiAppVersion version =
                    apps.findVersion(context.appId(), context.appVersionId(), context.tenantId())
                            .orElseThrow(
                                    () -> new IllegalArgumentException("app version not found"));
            if (!"PUBLISHED".equals(version.status())
                    || app.publishedVersionId() == null
                    || !context.appVersionId().equals(app.publishedVersionId())) {
                throw new IllegalStateException("only the published app version can run");
            }
        }
        AiRun run =
                new AiRun(
                        UUID.randomUUID().toString(),
                        context.tenantId(),
                        new UserId(context.ownerUserId()),
                        context.appId(),
                        context.appVersionId(),
                        source,
                        sourceId,
                        null,
                        context.traceId() == null || context.traceId().isBlank()
                                ? UUID.randomUUID().toString()
                                : context.traceId(),
                        context.conversationId(),
                        context.generationId(),
                        context.executionId(),
                        model,
                        hash(prompt),
                        AiRunStatus.CREATED,
                        0,
                        0,
                        true,
                        null,
                        Instant.now(),
                        null,
                        null,
                        0);
        runs.insert(run);
        AiRun running = run.transition(AiRunStatus.RUNNING);
        if (runs.update(running, run.version()) != 1)
            throw new IllegalStateException("run admission conflict");
        try {
            append(
                    running,
                    AiRunEventType.RUN_STARTED,
                    JSONUtils.toJsonString(Map.of("schemaVersion", 1, "source", source.name())),
                    true);
        } catch (RuntimeException eventFailure) {
            try {
                finish(
                        running.id(),
                        running.tenantId(),
                        running.ownerUserId().value(),
                        AiRunStatus.FAILED,
                        "RUN_EVENT_WRITE_FAILED");
            } catch (RuntimeException ignored) {
            }
            throw eventFailure;
        }
        return running;
    }

    /**
     * {@code finish} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param terminal 参数值，用于执行当前操作。
     * @param errorCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiRun finish(
            String id,
            TenantId tenantId,
            long ownerUserId,
            AiRunStatus terminal,
            String errorCode) {
        AiRun current =
                runs.find(id, tenant(tenantId), ownerUserId)
                        .orElseThrow(() -> new IllegalArgumentException("run not found"));
        if (terminal != AiRunStatus.COMPLETED
                && terminal != AiRunStatus.FAILED
                && terminal != AiRunStatus.CANCELLED) {
            throw new IllegalArgumentException("run finish status must be terminal");
        }
        if (current.status() == AiRunStatus.COMPLETED
                || current.status() == AiRunStatus.FAILED
                || current.status() == AiRunStatus.CANCELLED) {
            return current;
        }
        AiRun next = current.transition(terminal);
        next =
                new AiRun(
                        next.id(),
                        next.tenantId(),
                        next.ownerUserId(),
                        next.appId(),
                        next.appVersionId(),
                        next.sourceType(),
                        next.sourceId(),
                        next.parentRunId(),
                        next.traceId(),
                        next.conversationId(),
                        next.generationId(),
                        next.executionId(),
                        next.model(),
                        next.promptHash(),
                        next.status(),
                        next.promptTokens(),
                        next.completionTokens(),
                        next.estimatedUsage(),
                        next.costSnapshot(),
                        next.createdAt(),
                        Instant.now(),
                        errorCode,
                        next.version(),
                        next.lastEventSeq());
        AiRunEventType terminalEvent =
                terminal == AiRunStatus.COMPLETED
                        ? AiRunEventType.RUN_COMPLETED
                        : terminal == AiRunStatus.CANCELLED
                                ? AiRunEventType.RUN_CANCELLED
                                : AiRunEventType.RUN_FAILED;
        return runs.updateTerminalAndAppend(
                next,
                current.version(),
                terminalEvent,
                errorCode == null ? "{}" : JSONUtils.toJsonString(Map.of("errorCode", errorCode)),
                true);
    }

    /**
     * {@code events} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param afterSeq 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<AiRunEvent> events(
            String id, TenantId tenantId, long ownerUserId, long afterSeq, int limit) {
        return runs.events(
                id, tenant(tenantId), ownerUserId, afterSeq, Math.max(1, Math.min(limit, 1000)));
    }

    /**
     * {@code listRuns} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<AiRun> listRuns(TenantId tenantId, long ownerUserId, int limit) {
        return runs.list(tenant(tenantId), ownerUserId, limit);
    }

    /**
     * {@code requireRun} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiRun requireRun(String id, TenantId tenantId, long ownerUserId) {
        return runs.find(id, tenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("run not found"));
    }

    /**
     * {@code requireGenerationRun} 执行当前类型定义的业务操作。
     *
     * @param generationId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiRun requireGenerationRun(String generationId, TenantId tenantId, long ownerUserId) {
        return runs.findByGeneration(generationId, tenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("runtime run not found"));
    }

    /**
     * {@code linkGeneration} 执行当前类型定义的业务操作。
     *
     * @param run 参数值，用于执行当前操作。
     * @param generationId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiRun linkGeneration(AiRun run, String generationId) {
        if (run == null || generationId == null || generationId.isBlank())
            throw new IllegalArgumentException("runtime run and generation id are required");
        if (run.generationId() != null && !run.generationId().equals(generationId))
            throw new IllegalStateException("runtime run is already linked to another generation");
        if (run.generationId() == null
                && runs.linkGeneration(
                                run.id(), run.tenantId(), run.ownerUserId().value(), generationId)
                        != 1) {
            throw new IllegalStateException("runtime generation link was modified");
        }
        return runs.find(run.id(), run.tenantId(), run.ownerUserId().value()).orElse(run);
    }

    /**
     * {@code requireExecutionRun} 执行当前类型定义的业务操作。
     *
     * @param executionId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiRun requireExecutionRun(String executionId, TenantId tenantId, long ownerUserId) {
        return runs.findByExecution(executionId, tenant(tenantId), ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("runtime run not found"));
    }

    /**
     * {@code recordUsage} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param estimated 参数值，用于执行当前操作。
     * @param costSnapshot 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AiRun recordUsage(
            String id,
            TenantId tenantId,
            long ownerUserId,
            long promptTokens,
            long completionTokens,
            boolean estimated,
            String costSnapshot) {
        AiRun current = requireRun(id, tenantId, ownerUserId);
        if (current.status() == AiRunStatus.COMPLETED
                || current.status() == AiRunStatus.FAILED
                || current.status() == AiRunStatus.CANCELLED) {
            throw new IllegalStateException("cannot record usage after run is terminal");
        }
        AiRun next =
                new AiRun(
                        current.id(),
                        current.tenantId(),
                        current.ownerUserId(),
                        current.appId(),
                        current.appVersionId(),
                        current.sourceType(),
                        current.sourceId(),
                        current.parentRunId(),
                        current.traceId(),
                        current.conversationId(),
                        current.generationId(),
                        current.executionId(),
                        current.model(),
                        current.promptHash(),
                        current.status(),
                        Math.max(0, promptTokens),
                        Math.max(0, completionTokens),
                        estimated,
                        costSnapshot,
                        current.createdAt(),
                        current.completedAt(),
                        current.errorCode(),
                        current.version() + 1,
                        current.lastEventSeq());
        if (runs.update(next, current.version()) != 1)
            throw new IllegalStateException("run usage was modified");
        return next;
    }

    /**
     * {@code append} 执行当前类型定义的业务操作。
     *
     * @param run 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     * @param payload 参数值，用于执行当前操作。
     * @param redacted 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public long append(AiRun run, AiRunEventType type, String payload, boolean redacted) {
        long seq =
                runs.appendNextEvent(
                        run.id(),
                        run.tenantId(),
                        run.ownerUserId().value(),
                        type,
                        payload == null ? "{}" : payload,
                        redacted,
                        Instant.now());
        return seq;
    }

    /**
     * {@code append} 执行当前类型定义的业务操作。
     *
     * @param run 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     * @param payload 参数值，用于执行当前操作。
     * @param redacted 参数值，用于执行当前操作。
     * @param turnId 参数值，用于执行当前操作。
     * @param stepId 参数值，用于执行当前操作。
     * @param providerRequestId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public long append(
            AiRun run,
            AiRunEventType type,
            String payload,
            boolean redacted,
            String turnId,
            String stepId,
            String providerRequestId) {
        return runs.appendNextEvent(
                run.id(),
                run.tenantId(),
                run.ownerUserId().value(),
                type,
                payload == null ? "{}" : payload,
                redacted,
                Instant.now(),
                turnId,
                stepId,
                providerRequestId);
    }

    private void validateConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) return;
        try {
            Object parsed = JSONUtils.parseObject(configJson, Object.class);
            if (!(parsed instanceof Map<?, ?> raw))
                throw new IllegalArgumentException(
                        "app version configuration must be a JSON object");
            Map<?, ?> config = raw;
            validateCollection(config, "modelRoute", "model route must contain at least one model");
            validateCollection(
                    config, "knowledgeSpaces", "knowledge space binding cannot be empty");
            validateCollection(config, "tools", "tool binding cannot be empty");
            validateExecutableBindings(config);
            validateBudget(config.get("budget"));
            validatePublishChecks(config.get("validation"));
            validateEvaluationGate(config.get("evaluation"));
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException
                    && ex.getMessage() != null
                    && !ex.getMessage().contains("configuration is not valid JSON")) throw ex;
            throw new IllegalArgumentException("app version configuration is not valid JSON", ex);
        }
    }

    /**
     * 校验executablebindings。
     *
     * @param config config 参数。
     */
    private void validateExecutableBindings(Map<?, ?> config) {
        Object executionType = config.get("executionType");
        boolean agentExecution =
                executionType != null && "AGENT".equalsIgnoreCase(String.valueOf(executionType));
        Object agentId = config.get("agentId");
        if (agentExecution && (agentId == null || String.valueOf(agentId).isBlank())) {
            throw new IllegalArgumentException("agent execution requires agentId");
        }
        if (agentId != null && String.valueOf(agentId).isBlank()) {
            throw new IllegalArgumentException("agentId cannot be blank");
        }
        Map<?, ?> checks = config.get("validation") instanceof Map<?, ?> value ? value : Map.of();
        if (agentId != null && !isPass(checks.get("graph")))
            throw new IllegalArgumentException(
                    "publish validation failed: graph is required for agent binding");
        if (config.containsKey("model") || config.containsKey("modelRoute")) {
            if (config.get("model") instanceof String model && model.isBlank())
                throw new IllegalArgumentException("model cannot be blank");
            if (config.containsKey("validation") && !isPass(checks.get("model")))
                throw new IllegalArgumentException("publish validation failed: model");
        }
        if (config.containsKey("knowledgeSpaces") && !isPass(checks.get("knowledge")))
            throw new IllegalArgumentException("publish validation failed: knowledge");
        if (config.containsKey("tools") && !isPass(checks.get("tools")))
            throw new IllegalArgumentException("publish validation failed: tools");
    }

    private boolean isPass(Object value) {
        return value != null && "PASS".equalsIgnoreCase(String.valueOf(value));
    }

    private void validateCollection(Map<?, ?> config, String key, String message) {
        if (!config.containsKey(key)) return;
        Object value = config.get(key);
        if (!(value instanceof java.util.Collection<?> collection)
                || collection.isEmpty()
                || collection.stream()
                        .anyMatch(item -> item == null || String.valueOf(item).isBlank())) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateBudget(Object raw) {
        if (raw == null) return;
        if (!(raw instanceof Map<?, ?> budget))
            throw new IllegalArgumentException("budget must be an object");
        for (String key : List.of("maxTokens", "maxCost")) {
            Object value = budget.get(key);
            if (value instanceof Number number && number.doubleValue() < 0)
                throw new IllegalArgumentException("budget values cannot be negative");
        }
    }

    private void validatePublishChecks(Object raw) {
        if (raw == null) return;
        if (!(raw instanceof Map<?, ?> checks))
            throw new IllegalArgumentException("validation must be an object");
        for (String key : List.of("graph", "model", "knowledge", "tools", "budget")) {
            Object value = checks.get(key);
            if (value != null && !"PASS".equalsIgnoreCase(String.valueOf(value))) {
                throw new IllegalArgumentException("publish validation failed: " + key);
            }
        }
    }

    private void validateEvaluationGate(Object raw) {
        if (raw == null) return;
        if (!(raw instanceof Map<?, ?> evaluation))
            throw new IllegalArgumentException("evaluation must be an object");
        Object thresholdValue = evaluation.get("requiredPassRate");
        Object actualValue = evaluation.get("passRate");
        if (thresholdValue == null) return;
        if (!(thresholdValue instanceof Number threshold)
                || threshold.doubleValue() < 0
                || threshold.doubleValue() > 1) {
            throw new IllegalArgumentException(
                    "evaluation requiredPassRate must be between 0 and 1");
        }
        if (!(actualValue instanceof Number actual)
                || actual.doubleValue() < threshold.doubleValue()) {
            throw new IllegalArgumentException("evaluation pass rate is below publish threshold");
        }
    }

    private String hash(String value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(
                                            (value == null ? "" : value)
                                                    .getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static TenantId tenant(TenantId tenantId) {
        return Objects.requireNonNull(tenantId, "tenantId must not be null");
    }
}
