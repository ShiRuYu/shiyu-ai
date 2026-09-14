package com.shiyu.ai.agent.contract.node;

/**
 * {@code NodeInputParam} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param name 名称，表示该记录组件承载的数据。
 * @param type 类型，表示该记录组件承载的数据。
 * @param source 来源，表示该记录组件承载的数据。
 * @param required 是否必填，表示该记录组件承载的数据。
 * @param description 描述，表示该记录组件承载的数据。
 * @param defaultValue defaultValue 属性，表示该记录组件承载的数据。
 */
public record NodeInputParam(
        String name,
        String type,
        InputSource source,
        boolean required,
        String description,
        Object defaultValue) {
    public static NodeInputParam apiRequired(String name, String type, String description) {
        return new NodeInputParam(name, type, InputSource.API_REQUEST, true, description, null);
    }

    public static NodeInputParam apiOptional(
            String name, String type, String description, Object defaultValue) {
        return new NodeInputParam(
                name, type, InputSource.API_REQUEST, false, description, defaultValue);
    }

    public static NodeInputParam config(String name, String type, String description) {
        return new NodeInputParam(name, type, InputSource.CONFIG_VALUE, false, description, null);
    }

    public static NodeInputParam previous(String name, String type, String description) {
        return new NodeInputParam(name, type, InputSource.PREVIOUS_NODE, false, description, null);
    }

    public static NodeInputParam defaultVal(
            String name, String type, String description, Object value) {
        return new NodeInputParam(name, type, InputSource.DEFAULT_VALUE, false, description, value);
    }
}
