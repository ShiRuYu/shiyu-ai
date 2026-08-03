package com.shiyu.ai.education.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Optional classpath resources that are copied into the configured storage backend. */
@Data
@Component
@ConfigurationProperties(prefix = "shiyu.education.storage.seed")
public class StorageSeedProperties {

    private Education education = new Education();

    @Data
    public static class Education {
        private boolean enabled = true;
        private long tenantId = 1L;
        private String namespace = "education-resources";
    }
}
