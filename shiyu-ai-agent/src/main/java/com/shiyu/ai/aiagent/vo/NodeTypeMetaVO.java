package com.shiyu.ai.aiagent.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeTypeMetaVO {

    private String code;

    private String name;

    private String description;

    private String icon;

    private String color;

    private List<FieldMeta> fields;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldMeta {
        private String key;
        private String label;
        private String type;
        private Object defaultValue;
        private Boolean required;
        private Map<String, Object> options;
        private String description;
        private DataSourceConfig source;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceConfig {
        private String type;
        private String url;
        private String dictType;
        private String labelKey;
        private String valueKey;
        private String dependsOn;
    }
}
