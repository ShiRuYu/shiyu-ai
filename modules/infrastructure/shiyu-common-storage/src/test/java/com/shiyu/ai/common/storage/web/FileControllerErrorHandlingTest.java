package com.shiyu.ai.common.storage.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class FileControllerErrorHandlingTest {

    @Test
    void hidesStorageDetailsWhenListingFilesFails() throws Exception {
        FileStorageManager storage = mock(FileStorageManager.class);
        when(storage.list("tenant/7")).thenThrow(new IOException("/var/lib/shiyu/secret.db"));
        FileController controller = new FileController(storage);

        try (MockedStatic<ActorContextHttpAdapter> context =
                mockStatic(ActorContextHttpAdapter.class)) {
            context.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);

            Result<?> result = controller.list();

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("读取文件列表失败");
            assertThat(result.getMessage()).doesNotContain("secret.db");
        }
    }

    @Test
    void hidesStorageDetailsWhenUploadingFileFails() throws Exception {
        FileStorageManager storage = mock(FileStorageManager.class);
        when(storage.upload(eq("tenant/7"), anyString(), anyString(), anyLong(), any()))
                .thenThrow(new IOException("s3://private-bucket/access-token"));
        FileController controller = new FileController(storage);
        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "notes.txt",
                        "text/plain",
                        new ByteArrayInputStream("notes".getBytes()));

        try (MockedStatic<ActorContextHttpAdapter> context =
                mockStatic(ActorContextHttpAdapter.class)) {
            context.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);

            Result<?> result = controller.upload(file);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("文件上传失败");
            assertThat(result.getMessage()).doesNotContain("access-token");
        }
    }

    @Test
    void hidesStorageDetailsWhenDeletingFileFails() throws Exception {
        FileStorageManager storage = mock(FileStorageManager.class);
        doThrow(new IOException("/var/lib/shiyu/secret.db"))
                .when(storage)
                .delete("tenant/7/secret.txt");
        FileController controller = new FileController(storage);

        try (MockedStatic<ActorContextHttpAdapter> context =
                mockStatic(ActorContextHttpAdapter.class)) {
            context.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);

            Result<?> result = controller.delete("tenant/7/secret.txt");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("文件删除失败");
            assertThat(result.getMessage()).doesNotContain("secret.db");
        }
    }
}
