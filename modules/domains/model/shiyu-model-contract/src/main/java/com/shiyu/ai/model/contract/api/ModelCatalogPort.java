package com.shiyu.ai.model.contract.api;

/**
 * Read-only model catalog facts needed by other bounded contexts. Implementations stay inside
 * Model; consumers must not query model tables.
 */
public interface ModelCatalogPort {
    /** Counts enabled provider configurations visible to the current catalog. */
    long countEnabledPlatforms();

    /** Counts enabled model entries across the catalog. */
    long countEnabledModels();
}
