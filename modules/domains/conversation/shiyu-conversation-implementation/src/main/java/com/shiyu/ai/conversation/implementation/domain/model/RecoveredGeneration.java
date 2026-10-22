package com.shiyu.ai.conversation.implementation.domain.model;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.TenantId;

/** 已成功回收的生成及其配额所属主体。 */
public record RecoveredGeneration(GenerationRun run, TenantId tenantId, long ownerUserId) {}
