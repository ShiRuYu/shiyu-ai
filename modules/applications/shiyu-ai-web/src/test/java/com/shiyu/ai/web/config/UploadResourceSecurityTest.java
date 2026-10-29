package com.shiyu.ai.web.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

/** 验证上传目录不能绕过业务下载接口的文件归属与空间权限检查。 */
@Tag("dev")
class UploadResourceSecurityTest {
    @TempDir Path home;

    @Test
    void doesNotExposeExistingPrivateFilesAsStaticResources() throws Exception {
        Path file = home.resolve("data/uploads/knowledge/1/10/private.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "private document");
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context, "app.home=" + home.toString().replace('\\', '/'));
            context.register(DelegatingWebMvcConfiguration.class, ResourcesConfig.class);
            context.refresh();
            MockMvcBuilders.webAppContextSetup(context).build()
                    .perform(get("/uploads/knowledge/1/10/private.txt"))
                    .andExpect(status().isNotFound());
        }
    }
}
