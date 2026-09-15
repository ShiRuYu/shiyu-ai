package com.shiyu.ai.education.implementation.config;

import com.shiyu.ai.common.core.module.ConditionalOnBusinessModule;
import com.shiyu.ai.common.core.module.BusinessModuleDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;

import java.net.URL;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EducationModuleAutoConfigurationTest {

    @Test
    void educationModuleIsConditionalAndScansOnlyItsImplementationBoundary() {
        AutoConfiguration autoConfiguration = EducationModuleAutoConfiguration.class
                .getAnnotation(AutoConfiguration.class);
        ConditionalOnBusinessModule condition = EducationModuleAutoConfiguration.class
                .getAnnotation(ConditionalOnBusinessModule.class);
        ComponentScan scan = EducationModuleAutoConfiguration.class
                .getAnnotation(ComponentScan.class);

        assertTrue(autoConfiguration != null);
        assertEquals("education", condition.value());
        assertTrue(condition.matchIfMissing());
        assertArrayEquals(new String[]{"com.shiyu.ai.education.implementation"}, scan.basePackages());
    }

    @Test
    void educationModuleIsListedAsBootAutoConfiguration() throws Exception {
        String resourceName = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";
        Enumeration<URL> resources = EducationModuleAutoConfiguration.class.getClassLoader()
                .getResources(resourceName);
        boolean found = false;
        while (resources.hasMoreElements()) {
            try (var stream = resources.nextElement().openStream()) {
                if (new String(stream.readAllBytes())
                        .contains(EducationModuleAutoConfiguration.class.getName())) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }

    @Test
    void disabledEducationModuleDoesNotPublishItsDescriptor() {
        new ApplicationContextRunner()
                .withUserConfiguration(EducationModuleAutoConfiguration.class)
                .withPropertyValues("shiyu.modules.education.enabled=false")
                .run(context -> assertTrue(
                        context.getBeansOfType(BusinessModuleDescriptor.class).isEmpty()));
    }
}
