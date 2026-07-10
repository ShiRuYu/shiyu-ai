package com.shiyu.ai.agent.node;

public enum InputSource {
    API_REQUEST("api", "接口入参", "由 HTTP 请求调用者提供"),
    CONFIG_VALUE("config", "配置值", "在 graph_config 节点 config 中静态配置"),
    PREVIOUS_NODE("previous", "前节点传入", "由上游节点输出自动传递到 State"),
    DEFAULT_VALUE("default", "默认值", "节点内部有默认值，调用方可不传");
    private final String code;
    private final String label;
    private final String description;
    InputSource(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
