package com.shiyu.ai.agent.implementation.runtime;

import com.shiyu.ai.agent.contract.runtime.*;

import java.time.Instant;
import java.util.List;

public record PromptTemplate(String id, long tenantId, long ownerUserId, String name,
                             String template, List<String> variables, String status,
                             Instant createdAt, Instant updatedAt) { }
