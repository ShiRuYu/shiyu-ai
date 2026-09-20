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
 * 提供 提示词 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Service
public class PromptService {
    private static final Pattern VARIABLE =
            Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}\\s*");
    private final Map<String, PromptTemplate> templates = new LinkedHashMap<>();

    /**
     * 查询 提示词 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public synchronized List<PromptTemplate> list(TenantId tenantId, long ownerUserId) {
        long value = requireTenant(tenantId);
        return templates.values().stream()
                .filter(v -> v.tenantId() == value && v.ownerUserId() == ownerUserId)
                .toList();
    }

    /**
     * 创建或保存 提示词 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param template 用于完成本次业务处理的 template 参数。
     * @param variables 用于完成本次业务处理的 variables 参数。
     * @return 返回 提示词 相关操作生成的结果数据。
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
     * 发布或发送 提示词 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 提示词 相关操作生成的结果数据。
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
     * 执行 提示词 相关业务数据，并返回处理结果。
     *
     * @param template 用于完成本次业务处理的 template 参数。
     * @param variables 用于完成本次业务处理的 variables 参数。
     * @return 返回 提示词 相关操作生成的结果数据。
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
     * 封装 提示词 Preview 相关的不可变数据及其字段约束。
     */
    public record PromptPreview(String content, long estimatedTokens, List<String> variables) {}
}
