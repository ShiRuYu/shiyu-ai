package com.shiyu.ai.runtimeconsole.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** 覆盖配置快照版本控制、密钥保护和恢复行为。 */
class ConfigSnapshotStoreTest {

    @TempDir Path temp;

    @Test
    void savesVersionedSnapshotWithoutWritingSecretPlaintext() throws Exception {
        ConfigSnapshotStore store = newStore();

        ConfigSnapshotStore.Snapshot snapshot =
                store.save(0, Map.of("server.port", "9001"), Map.of("shiyu.ai.openai.api-key", "secret-value"), Set.of());

        assertEquals(1, snapshot.revision());
        assertEquals("9001", store.current().values().get("server.port"));
        assertEquals("secret-value", store.readSecrets(snapshot).get("shiyu.ai.openai.api-key"));
        String disk = Files.readString(temp.resolve("data/runtime-console/config/snapshot-1.json"));
        assertTrue(!disk.contains("secret-value"));
        assertTrue(disk.contains("cipher:"));
    }

    @Test
    void rejectsConcurrentStaleEditsAndLeavesCurrentVersionUntouched() throws Exception {
        ConfigSnapshotStore store = newStore();
        store.save(0, Map.of("server.port", "9001"), Map.of(), Set.of());

        assertThrows(
                ConfigSnapshotStore.VersionConflictException.class,
                () -> store.save(0, Map.of("server.port", "9002"), Map.of(), Set.of()));
        assertEquals("9001", store.current().values().get("server.port"));
    }

    @Test
    void restoresLastSuccessfullyAppliedSnapshotAsANewVersion() throws Exception {
        ConfigSnapshotStore store = newStore();
        store.save(0, Map.of("server.port", "9001"), Map.of(), Set.of());
        store.markCurrentApplied();
        store.save(1, Map.of("server.port", "9002"), Map.of(), Set.of());

        ConfigSnapshotStore.Snapshot restored = store.restoreLastApplied(2);

        assertEquals(3, restored.revision());
        assertEquals(1L, restored.restoredFromRevision());
        assertEquals("9001", store.current().values().get("server.port"));
    }

    private ConfigSnapshotStore newStore() {
        return new ConfigSnapshotStore(
                temp,
                new ObjectMapper(),
                new ConfigSecretProtector() {
                    @Override
                    public String protect(String plaintext) {
                        return "cipher:" + Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
                    }

                    @Override
                    public String unprotect(String ciphertext) {
                        byte[] decoded = Base64.getDecoder().decode(ciphertext.substring("cipher:".length()));
                        return new String(decoded, StandardCharsets.UTF_8);
                    }
                });
    }
}
