package com.shiyu.ai.common.storage;

/**
 * Optional domain contribution to an embedded backup manifest.
 * Storage owns the backup archive; domain modules only describe their
 * versioned metadata without making Storage depend on a business schema.
 */
@FunctionalInterface
public interface BackupManifestContributor {

    /** Returns newline-delimited manifest entries, or an empty string. */
    String contribute();
}
