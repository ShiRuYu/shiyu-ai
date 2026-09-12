package com.shiyu.ai.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

final class ArchitectureImportOptions {
    private ArchitectureImportOptions() {}

    /**
     * 为架构测试排除旧平台构建目录，避免扫描已迁移模块。
     */
    static final class DoNotIncludeLegacyPlatformBuilds implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.asURI().toString().replace('\\', '/').contains("/platform/");
        }
    }
}
