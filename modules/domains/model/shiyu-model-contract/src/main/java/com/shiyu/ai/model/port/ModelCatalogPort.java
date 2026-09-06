package com.shiyu.ai.model.port;

/**
 * Read-only model catalog facts needed by other bounded contexts.
 * Implementations stay inside Model; consumers must not query model tables.
 */
public interface ModelCatalogPort {
    long countEnabledPlatforms();

    long countEnabledModels();
}
