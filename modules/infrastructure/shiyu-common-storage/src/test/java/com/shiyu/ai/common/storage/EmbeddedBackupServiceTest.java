package com.shiyu.ai.common.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmbeddedBackupServiceTest {

    private static final Pattern BACKUP_PATH = Pattern.compile("BACKUP TO '(.+)'", Pattern.CASE_INSENSITIVE);

    @TempDir
    Path tempDirectory;

    @Test
    void scheduledBackupCreatesRestorableSnapshot() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        doAnswer(invocation -> {
            Matcher matcher = BACKUP_PATH.matcher(invocation.getArgument(0, String.class));
            assertTrue(matcher.matches());
            Path h2Backup = Path.of(matcher.group(1));
            Files.createDirectories(h2Backup.getParent());
            Files.write(h2Backup, "h2-backup".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(jdbcTemplate).execute(anyString());

        @SuppressWarnings("unchecked")
        ObjectProvider<BackupManifestContributor> contributors = mock(ObjectProvider.class);
        when(contributors.orderedStream()).thenReturn(Stream.empty());

        Path dataRoot = tempDirectory.resolve("data");
        Path backupRoot = dataRoot.resolve("backups");
        EmbeddedBackupService service = new EmbeddedBackupService(jdbcTemplate, dataRoot.toString(),
                backupRoot.toString(), true, 1, 1, Long.MAX_VALUE, contributors);

        service.scheduledBackup();

        try (Stream<Path> paths = Files.list(backupRoot)) {
            Path snapshot = paths.filter(path -> path.getFileName().toString().startsWith("shiyu-backup-"))
                    .findFirst().orElseThrow();
            EmbeddedBackupService.RestoreCheckResult check = service.restoreCheck(
                    snapshot.getFileName().toString());
            assertTrue(check.valid());
            assertEquals(2, check.entries());
        }
    }
}
