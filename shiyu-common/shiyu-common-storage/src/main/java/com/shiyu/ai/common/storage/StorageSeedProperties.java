package com.shiyu.ai.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Optional classpath resources that are copied into the configured storage backend. */
@Data
@ConfigurationProperties(prefix = "shiyu.storage.seed")
public class StorageSeedProperties {

    private Education education = new Education();

    @Data
    public static class Education {
        private boolean enabled = true;
        private long tenantId = 1L;
        private String namespace = "education-resources";
    }
}
