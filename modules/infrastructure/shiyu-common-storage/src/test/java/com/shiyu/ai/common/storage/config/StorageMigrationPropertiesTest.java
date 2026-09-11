package com.shiyu.ai.common.storage.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class StorageMigrationPropertiesTest {

    @Test
    void disabledMigrationNeedsNoOperationalSettings() {
        new StorageMigrationProperties().validate();
    }

    @Test
    void enabledMigrationRequiresLocalSourceAndS3CompatibleTarget() {
        StorageMigrationProperties properties = new StorageMigrationProperties();
        properties.setEnabled(true);
        assertThatThrownBy(properties::validate).isInstanceOf(IllegalStateException.class);

        properties.setSourcePath("/data/files");
        properties.setDestinationProvider("s3");
        assertThatThrownBy(properties::validate).isInstanceOf(IllegalStateException.class);

        properties.setNamespace("tenant/1");
        properties.validate();
    }
}
