package com.shiyu.ai.runtimeconsole.launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OfflineConfigRecoveryTest {

    @TempDir Path appHome;

    @Test
    void restoresCiphertextAsANewSnapshotWithoutDecryptingOrChangingLastSuccessfulPointer() throws Exception {
        Path directory = appHome.resolve("data/runtime-console/config");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("active-version"), "4");
        Files.writeString(directory.resolve("last-applied-version"), "2");
        Files.writeString(directory.resolve("snapshot-3.json"), "{}");
        Files.writeString(directory.resolve("snapshot-4.json"), "{}");
        ObjectNode successful = new ObjectMapper().createObjectNode();
        successful.put("schemaVersion", 1);
        successful.put("revision", 2);
        successful.put("createdAtEpochMillis", 123L);
        successful.putObject("values").put("server.port", "9000");
        successful.putObject("encryptedSecrets").put("spring.datasource.password", "dpapi-current-user:opaque-ciphertext");
        new ObjectMapper().writeValue(directory.resolve("snapshot-2.json").toFile(), successful);
        ObjectMapper mapper = new ObjectMapper();

        OfflineConfigRecovery.RestoreOutcome outcome = new OfflineConfigRecovery(mapper).restoreLastApplied(appHome);

        assertEquals(new OfflineConfigRecovery.RestoreOutcome(2, 5), outcome);
        assertEquals("5", Files.readString(directory.resolve("active-version")));
        assertEquals("2", Files.readString(directory.resolve("last-applied-version")));
        ObjectNode restored = (ObjectNode) mapper.readTree(directory.resolve("snapshot-5.json").toFile());
        assertEquals(2, restored.path("restoredFromRevision").asLong());
        assertEquals("dpapi-current-user:opaque-ciphertext",
                restored.path("encryptedSecrets").path("spring.datasource.password").asText());
    }

    @Test
    void doesNotCreateOrOverwriteAConfigWhenNoSuccessfulVersionExists() {
        assertThrows(java.io.IOException.class,
                () -> new OfflineConfigRecovery(new ObjectMapper()).restoreLastApplied(appHome));
    }
}
