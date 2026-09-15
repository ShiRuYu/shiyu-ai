package com.shiyu.ai.architecture;

import com.shiyu.ai.bootstrap.ShiyuBootstrapApplication;
import com.shiyu.ai.composition.config.PlatformCompositionAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositionScanBoundaryTest {

    @Test
    void bootstrapScansOnlyBootstrapAndCompositionPackages() {
        SpringBootApplication application = ShiyuBootstrapApplication.class
                .getAnnotation(SpringBootApplication.class);

        String[] packages = application.scanBasePackages();
        Arrays.sort(packages);
        assertArrayEquals(
                new String[]{"com.shiyu.ai.bootstrap", "com.shiyu.ai.composition"}, packages);
    }

    @Test
    void platformCompositionDoesNotScanOptionalBusinessModules() {
        ComponentScan scan = PlatformCompositionAutoConfiguration.class
                .getAnnotation(ComponentScan.class);

        assertTrue(Arrays.asList(scan.basePackages())
                .containsAll(Arrays.asList(
                        "com.shiyu.ai.iam.implementation",
                        "com.shiyu.ai.governance.implementation")));
        assertFalse(Arrays.stream(scan.basePackages())
                .anyMatch(packageName -> packageName.startsWith("com.shiyu.ai.education")));
    }

    @Test
    void platformCompositionIsPublishedAsBootAutoConfiguration() throws IOException {
        String resourceName = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";
        Enumeration<URL> resources = PlatformCompositionAutoConfiguration.class.getClassLoader()
                .getResources(resourceName);
        boolean found = false;
        while (resources.hasMoreElements()) {
            try (InputStream stream = resources.nextElement().openStream()) {
                if (new String(stream.readAllBytes())
                        .contains(PlatformCompositionAutoConfiguration.class.getName())) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }
}
