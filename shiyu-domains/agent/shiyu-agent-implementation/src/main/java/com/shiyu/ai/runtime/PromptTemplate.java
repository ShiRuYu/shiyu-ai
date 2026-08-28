package com.shiyu.ai.runtime;

import java.time.Instant;
import java.util.List;

public record PromptTemplate(String id, long tenantId, long ownerUserId, String name,
                             String template, List<String> variables, String status,
                             Instant createdAt, Instant updatedAt) { }
