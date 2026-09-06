package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Prompt Studio's platform contract. Persistence can be swapped for the v3 repository without changing the API. */
@Service
public class PromptService {
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}\\s*");
    private final Map<String, PromptTemplate> templates = new LinkedHashMap<>();

    public synchronized List<PromptTemplate> list(TenantId tenantId, long ownerUserId) {
        long value = requireTenant(tenantId);
        return templates.values().stream().filter(v -> v.tenantId() == value && v.ownerUserId() == ownerUserId).toList();
    }

    public synchronized PromptTemplate create(TenantId tenantId, long ownerUserId, String name, String template, List<String> variables) {
        long value = requireTenant(tenantId);
        Instant now = Instant.now();
        List<String> resolved = variables == null || variables.isEmpty() ? extract(template) : List.copyOf(variables);
        PromptTemplate prompt = new PromptTemplate(UUID.randomUUID().toString(), value, ownerUserId, name, template, resolved, "DRAFT", now, now);
        templates.put(prompt.id(), prompt);
        return prompt;
    }

    public synchronized PromptTemplate publish(String id, TenantId tenantId, long ownerUserId) {
        PromptTemplate current = require(id, tenantId, ownerUserId);
        PromptTemplate value = new PromptTemplate(current.id(), current.tenantId(), current.ownerUserId(), current.name(), current.template(), current.variables(), "PUBLISHED", current.createdAt(), Instant.now());
        templates.put(id, value);
        return value;
    }

    public PromptPreview preview(String template, Map<String, Object> variables) {
        String rendered = template == null ? "" : template;
        Matcher matcher = VARIABLE.matcher(rendered);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            Object value = variables == null ? null : variables.get(matcher.group(1));
            matcher.appendReplacement(output, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(output);
        return new PromptPreview(output.toString(), estimateTokens(output.toString()), extract(template));
    }

    private synchronized PromptTemplate require(String id, TenantId tenantId, long ownerUserId) {
        long tenantValue = requireTenant(tenantId);
        PromptTemplate prompt = templates.get(id);
        if (prompt == null || prompt.tenantId() != tenantValue || prompt.ownerUserId() != ownerUserId) throw new IllegalArgumentException("prompt not found");
        return prompt;
    }
    private static long requireTenant(TenantId tenantId) { return Objects.requireNonNull(tenantId, "tenantId must not be null").value(); }
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
    private long estimateTokens(String value) { return Math.max(0, (value == null ? 0 : value.length() + 3) / 4); }
    public record PromptPreview(String content, long estimatedTokens, List<String> variables) { }
}
