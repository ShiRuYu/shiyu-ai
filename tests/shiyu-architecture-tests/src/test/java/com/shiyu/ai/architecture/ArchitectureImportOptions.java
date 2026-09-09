package com.shiyu.ai.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

final class ArchitectureImportOptions {
    private ArchitectureImportOptions() {
    }

    /** Keeps stale bytecode from the retired platform tree out of scans. */
    static final class DoNotIncludeLegacyPlatformBuilds implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.asURI().toString().replace('\\', '/').contains("/platform/");
        }
    }
}
