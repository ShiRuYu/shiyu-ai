package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.model.PromptTemplate;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PromptService 服务接口，负责执行智能体领域相关业务操作。
 */
@Service
public class PromptService {
    private static final Pattern VARIABLE =
            Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}\\s*");
    private final Map<String, PromptTemplate> templates = new LinkedHashMap<>();

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public synchronized List<PromptTemplate> list(TenantId tenantId, long ownerUserId) {
        long value = requireTenant(tenantId);
        return templates.values().stream()
                .filter(v -> v.tenantId() == value && v.ownerUserId() == ownerUserId)
                .toList();
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     * @param template 参数值，用于执行当前操作。
     * @param variables 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public synchronized PromptTemplate create(
            TenantId tenantId,
            long ownerUserId,
            String name,
            String template,
            List<String> variables) {
        long value = requireTenant(tenantId);
        Instant now = Instant.now();
        List<String> resolved =
                variables == null || variables.isEmpty()
                        ? extract(template)
                        : List.copyOf(variables);
        PromptTemplate prompt =
                new PromptTemplate(
                        UUID.randomUUID().toString(),
                        value,
                        ownerUserId,
                        name,
                        template,
                        resolved,
                        "DRAFT",
                        now,
                        now);
        templates.put(prompt.id(), prompt);
        return prompt;
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public synchronized PromptTemplate publish(String id, TenantId tenantId, long ownerUserId) {
        PromptTemplate current = require(id, tenantId, ownerUserId);
        PromptTemplate value =
                new PromptTemplate(
                        current.id(),
                        current.tenantId(),
                        current.ownerUserId(),
                        current.name(),
                        current.template(),
                        current.variables(),
                        "PUBLISHED",
                        current.createdAt(),
                        Instant.now());
        templates.put(id, value);
        return value;
    }

    /**
     * {@code preview} 执行当前类型定义的业务操作。
     *
     * @param template 参数值，用于执行当前操作。
     * @param variables 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public PromptPreview preview(String template, Map<String, Object> variables) {
        String rendered = template == null ? "" : template;
        Matcher matcher = VARIABLE.matcher(rendered);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            Object value = variables == null ? null : variables.get(matcher.group(1));
            matcher.appendReplacement(
                    output, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(output);
        return new PromptPreview(
                output.toString(), estimateTokens(output.toString()), extract(template));
    }

    private synchronized PromptTemplate require(String id, TenantId tenantId, long ownerUserId) {
        long tenantValue = requireTenant(tenantId);
        PromptTemplate prompt = templates.get(id);
        if (prompt == null
                || prompt.tenantId() != tenantValue
                || prompt.ownerUserId() != ownerUserId)
            throw new IllegalArgumentException("prompt not found");
        return prompt;
    }

    private static long requireTenant(TenantId tenantId) {
        return Objects.requireNonNull(tenantId, "tenantId must not be null").value();
    }

    private List<String> extract(String template) {
        List<String> result = new ArrayList<>();
        if (template == null) return result;
        Matcher matcher = VARIABLE.matcher(template);
        while (matcher.find()) {
            String variable = matcher.group(1);
            if (!result.contains(variable)) {
                result.add(variable);
            }
        }
        return result;
    }

    private long estimateTokens(String value) {
        return Math.max(0, (value == null ? 0 : value.length() + 3) / 4);
    }

    /**
     * {@code PromptPreview} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param content 内容，表示该记录组件承载的数据。
     * @param estimatedTokens 预计令牌数，表示该记录组件承载的数据。
     * @param variables 变量集合，表示该记录组件承载的数据。
     */
    public record PromptPreview(String content, long estimatedTokens, List<String> variables) {}
}
