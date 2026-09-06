package com.shiyu.ai.common.storage.web;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.storage.web.FileController;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("dev")
class FileControllerSecurityTest {

    @Test
    void protectsFileWriteOperationsWithDedicatedPermissions() throws NoSuchMethodException {
        SaCheckPermission uploadPermission = FileController.class
                .getMethod("upload", MultipartFile.class)
                .getAnnotation(SaCheckPermission.class);
        SaCheckPermission deletePermission = FileController.class
                .getMethod("delete", String.class)
                .getAnnotation(SaCheckPermission.class);

        assertThat(uploadPermission).isNotNull();
        assertThat(uploadPermission.value()).containsExactly("file:upload");
        assertThat(deletePermission).isNotNull();
        assertThat(deletePermission.value()).containsExactly("file:delete");
    }

    @Test
    void protectsFileReadOperationsWithDedicatedPermission() throws NoSuchMethodException {
        SaCheckPermission configPermission = FileController.class.getMethod("config").getAnnotation(SaCheckPermission.class);
        SaCheckPermission listPermission = FileController.class.getMethod("list").getAnnotation(SaCheckPermission.class);

        assertThat(configPermission).isNotNull();
        assertThat(configPermission.value()).containsExactly("file:list");
        assertThat(listPermission).isNotNull();
        assertThat(listPermission.value()).containsExactly("file:list");
    }
}
