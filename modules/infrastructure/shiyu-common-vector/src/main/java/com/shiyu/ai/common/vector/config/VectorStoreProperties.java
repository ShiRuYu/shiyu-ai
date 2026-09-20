package com.shiyu.ai.common.vector.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定义 向量 Store 基础设施或应用能力的配置项及装配规则。
 */
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
     * 查询 向量 Store 相关业务数据，并返回处理结果。
     *
     * @return 返回 向量 Store 相关操作生成的结果数据。
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
