package com.shiyu.ai.common.storage.api;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

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
