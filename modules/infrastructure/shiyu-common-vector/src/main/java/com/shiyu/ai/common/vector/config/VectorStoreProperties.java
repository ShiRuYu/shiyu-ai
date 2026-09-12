package com.shiyu.ai.common.vector.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 向量存储配置 */
@Data
@ConfigurationProperties(prefix = "shiyu.vector-store")
public class VectorStoreProperties {

    /** 向量存储类型: inmemory, jvector */
    private String type = "inmemory";

    /** 向量维度 */
    private int dimension = 512;

    /** JVector 持久化目录 */
    private String dataDir = "${app.home}/data/vector";

    /**
     * {@code getResolvedDataDir} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getResolvedDataDir() {
        String dir = dataDir;
        if (dir != null && dir.contains("${app.home}")) {
            String appHome = System.getProperty("app.home", ".");
            dir = dir.replace("${app.home}", appHome);
        }
        return dir;
    }
}
