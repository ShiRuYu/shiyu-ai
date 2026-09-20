package com.shiyu.ai.agent.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 封装 Node Type Meta 操作向调用方返回的传输数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeTypeMetaVO {

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private String code;

    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;

    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;

    /**
     * icon 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String icon;

    /**
     * color 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String color;

    /**
     * fields 属性，保存当前对象中的业务数据或协作依赖。
     */
    private List<FieldMeta> fields;

    /**
     * 实现 Field Meta 相关的业务处理、协作逻辑或基础设施能力。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldMeta {
        /**
         * 键，表示当前对象中的对应属性。
         */
        private String key;
        /**
         * label 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String label;
        /**
         * 类型，表示当前对象中的对应属性。
         */
        private String type;
        /**
         * defaultValue 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Object defaultValue;
        /**
         * required 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Boolean required;
        /**
         * 选项，表示当前对象中的对应属性。
         */
        private Map<String, Object> options;
        /**
         * 描述，表示当前对象中的对应属性。
         */
        private String description;
        /**
         * 来源，表示当前对象中的对应属性。
         */
        private DataSourceConfig source;
    }

    /**
     * 定义 Data Source 基础设施或应用能力的配置项及装配规则。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceConfig {
        private String type;
        /**
         * 地址，表示当前对象中的对应属性。
         */
        private String url;
        /**
         * dictType 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String dictType;
        /**
         * labelKey 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String labelKey;
        /**
         * 值键，表示当前对象中的对应属性。
         */
        private String valueKey;
        /**
         * dependsOn 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String dependsOn;
    }
}
