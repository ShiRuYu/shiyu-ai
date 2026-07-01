package com.shiyu.ai.aiagent.node;

public record NodeInputParam(
        String name,
        String type,
        InputSource source,
        boolean required,
        String description,
        Object defaultValue
) {
    public static NodeInputParam apiRequired(String name, String type, String description) {
        return new NodeInputParam(name, type, InputSource.API_REQUEST, true, description, null);
    }
    public static NodeInputParam apiOptional(String name, String type, String description, Object defaultValue) {
        return new NodeInputParam(name, type, InputSource.API_REQUEST, false, description, defaultValue);
    }
    public static NodeInputParam config(String name, String type, String description) {
        return new NodeInputParam(name, type, InputSource.CONFIG_VALUE, false, description, null);
    }
    public static NodeInputParam previous(String name, String type, String description) {
        return new NodeInputParam(name, type, InputSource.PREVIOUS_NODE, false, description, null);
    }
    public static NodeInputParam defaultVal(String name, String type, String description, Object value) {
        return new NodeInputParam(name, type, InputSource.DEFAULT_VALUE, false, description, value);
    }
}
