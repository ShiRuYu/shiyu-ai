package com.shiyu.ai.common.storage.config;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 定义 Storage 基础设施或应用能力的配置项及装配规则。
 */
@Data
@ConfigurationProperties(prefix = "shiyu.storage")
public class StorageProperties {

    /**
     * 类型，表示当前对象中的对应属性。
     */
    private String type = "local";

    private Local local = new Local();

    private Map<String, S3Provider> providers = new LinkedHashMap<>();

    /**
     * 实现 Local 相关的业务处理、协作逻辑或基础设施能力。
     */
    @Data
    public static class Local {
        /**
         * 路径，表示当前对象中的对应属性。
         */
        private String path = "${app.home}/data/uploads";
    }

    /**
     * 创建或提供 S 3 相关的业务组件和运行时能力。
     */
    @Data
    public static class S3Provider {
        private String endpoint;
        /**
         * region 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String region = "us-east-1";
        /**
         * bucket 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String bucket;
        /**
         * accessKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String accessKey;
        /**
         * secretKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String secretKey;
        /**
         * pathStyleAccess 属性，保存当前对象中的业务数据或协作依赖。
         */
        private boolean pathStyleAccess;
        /**
         * publicBaseUrl 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String publicBaseUrl;
    }
}
