package com.shiyu.ai.knowledge.vector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "shiyu.vector-store")
public class VectorStoreProperties {

    private String type = "inmemory";

    private int dimension = 512;

    private String dataDir = "${app.home}/data/vector";

    private Hnsw hnsw = new Hnsw();

    @Data
    public static class Hnsw {
        private String indexPath = "${app.home}/data/vector/hnsw.index";
    }
}
