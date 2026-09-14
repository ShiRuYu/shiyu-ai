package com.shiyu.ai.agent.implementation.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * {@code NodeTypeMetaVO} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
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
     * {@code FieldMeta} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
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
     * {@code DataSourceConfig} 提供智能体模块的配置项，并集中声明其默认值和运行约束。
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
