package com.shiyu.ai.education.web;

import com.shiyu.ai.common.storage.FileStorageManager;
import com.shiyu.ai.common.storage.StorageObject;
import com.shiyu.ai.common.storage.StoredFile;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/** Focused HTTP-facade tests; no reflective endpoint simulation. */
class EducationResourceContentControllerTest {
    @Test
    void rejectsUnsafePathsAndUsesSafeMediaTypeFallback() throws Exception {
        FileStorageManager storage = mock(FileStorageManager.class);
        EducationResourceContentController controller = new EducationResourceContentController(storage);
        try (MockedStatic<ActorContextHttpAdapter> actor = mockStatic(ActorContextHttpAdapter.class)) {
            actor.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);

            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.open(""));
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.open("../secret"));
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.open("a/b"));
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.open("a\\b"));

            when(storage.list("tenant/7/education-resources")).thenReturn(List.of());
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.open("missing.txt"));

            StoredFile unknownType = new StoredFile("bad-key", "bad.txt", 1, "not-a-media-type", Instant.now(), null, "memory");
            when(storage.list("tenant/7/education-resources")).thenReturn(List.of(unknownType));
            when(storage.open("bad-key")).thenReturn(new StorageObject(
                    new ByteArrayInputStream(new byte[]{1}), "bad.txt", "not-a-media-type", 1));
            assertDoesNotThrow(() -> controller.open("bad.txt"));

            StoredFile noType = new StoredFile("no-type", "no-type.txt", 1, null, Instant.now(), null, "memory");
            when(storage.list("tenant/7/education-resources")).thenReturn(List.of(noType));
            when(storage.open("no-type")).thenReturn(new StorageObject(
                    new ByteArrayInputStream(new byte[]{1}), "no-type.txt", null, 1));
            assertDoesNotThrow(() -> controller.open("no-type.txt"));
        }
    }
}
