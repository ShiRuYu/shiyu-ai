package com.shiyu.ai.knowledge.vector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "shiyu.vector-store")
public class VectorStoreProperties {

    private String type = "inmemory";

    private int dimension = 384;

    private String dataDir = "${app.home}/data/vector";

    private Qdrant qdrant = new Qdrant();

    private Hnsw hnsw = new Hnsw();

    @Data
    public static class Qdrant {
        private String host = "localhost";
        private int port = 6334;
        private String collection = "knowledge";
        private boolean useTls = false;
    }

    @Data
    public static class Hnsw {
        private String indexPath = "${app.home}/data/vector/hnsw.index";
    }
}
