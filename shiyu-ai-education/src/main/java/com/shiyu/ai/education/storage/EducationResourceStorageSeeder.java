package com.shiyu.ai.education.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.shiyu.ai.common.storage.FileStorageManager;
import com.shiyu.ai.common.storage.StoredFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Registers the bundled education demo files in Storage. The database metadata
 * store remains the source of truth; the classpath is only the initial seed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EducationResourceStorageSeeder implements ApplicationRunner {

    private static final String RESOURCE_ROOT = "seed/education-resources/";

    private static final List<ResourceSpec> RESOURCES = List.of(
            new ResourceSpec("openstax-integers-number-line.pdf", "application/pdf"),
            new ResourceSpec("openstax-absolute-value-exercises.pdf", "application/pdf"),
            new ResourceSpec("openstax-integers-cover.png", "image/png"),
            new ResourceSpec("number-line-integers-phet-1.0.0.html", "text/html;charset=UTF-8"));

    private final FileStorageManager storageManager;
    private final StorageSeedProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        StorageSeedProperties.Education education = properties.getEducation();
        if (!education.isEnabled()) {
            return;
        }
        String namespace = "tenant/" + education.getTenantId() + "/" + education.getNamespace();
        try {
            List<StoredFile> existing = storageManager.list(namespace);
            for (ResourceSpec resource : RESOURCES) {
                if (existing.stream().anyMatch(file -> resource.fileName().equals(file.name()))) {
                    continue;
                }
                ClassPathResource source = new ClassPathResource(RESOURCE_ROOT + resource.fileName());
                if (!source.exists()) {
                    log.warn("Education seed resource is missing from the storage module: {}", resource.fileName());
                    continue;
                }
                long size = source.contentLength();
                try (InputStream input = source.getInputStream()) {
                    StoredFile stored = storageManager.upload(
                            namespace, resource.fileName(), resource.contentType(), size, input);
                    log.info("Registered education seed resource in storage: {}", stored.key());
                }
            }
        } catch (IOException exception) {
            // A missing optional seed must not prevent the application from starting.
            log.warn("Unable to register education seed resources in storage", exception);
        }
    }

    private record ResourceSpec(String fileName, String contentType) {
    }
}
