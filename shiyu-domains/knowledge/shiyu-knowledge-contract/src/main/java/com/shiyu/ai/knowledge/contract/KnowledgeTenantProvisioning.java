package com.shiyu.ai.knowledge.contract;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * IAM-facing provisioning boundary. Knowledge owns the default-space data and
 * exposes only this command so IAM never reaches into Knowledge repositories.
 */
public interface KnowledgeTenantProvisioning {
    void initializeTenantDefaults(TenantId tenantId);
}
