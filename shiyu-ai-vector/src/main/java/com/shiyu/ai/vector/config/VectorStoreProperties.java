package com.shiyu.ai.vector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 向量存储配置
 */
@Data
@ConfigurationProperties(prefix = "shiyu.vector")
public class VectorStoreProperties {

    /** 向量存储类型: inmemory, jvector */
    private String type = "inmemory";

    /** 向量维度 */
    private int dimension = 512;

    /** JVector 持久化目录 */
    private String dataDir = "${app.home}/data/vector";

    public String getResolvedDataDir() {
        String dir = dataDir;
        if (dir != null && dir.contains("${app.home}")) {
            String appHome = System.getProperty("app.home", ".");
            dir = dir.replace("${app.home}", appHome);
        }
        return dir;
    }
}
