package com.shiyu.ai.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "shiyu.storage")
public class StorageProperties {

    private String type = "local";

    private Local local = new Local();

    private Map<String, S3Provider> providers = new LinkedHashMap<>();

    @Data
    public static class Local {
        private String path = "${app.home}/data/uploads";
    }

    @Data
    public static class S3Provider {
        private String endpoint;
        private String region = "us-east-1";
        private String bucket;
        private String accessKey;
        private String secretKey;
        private boolean pathStyleAccess;
        private String publicBaseUrl;
    }
}
