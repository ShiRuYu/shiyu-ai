package com.shiyu.ai.web.common;

import cn.dev33.satoken.annotation.SaCheckPermission;
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
}
