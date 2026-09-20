package com.shiyu.ai.agent.contract.node;

/**
 * 定义 Input Source 可用的枚举值及其业务语义。
 */
public enum InputSource {
    API_REQUEST("api", "接口入参", "由 HTTP 请求调用者提供"),
    CONFIG_VALUE("config", "配置值", "在 graph_config 节点 config 中静态配置"),
    PREVIOUS_NODE("previous", "前节点传入", "由上游节点输出自动传递到 State"),
    DEFAULT_VALUE("default", "默认值", "节点内部有默认值，调用方可不传");
    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * label 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String label;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private final String description;

    InputSource(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    /**
     * 查询 Input Source 相关业务数据，并返回处理结果。
     *
     * @return 返回 Input Source 相关操作生成的结果数据。
     */
    public String getCode() {
        return code;
    }

    /**
     * 查询 Input Source 相关业务数据，并返回处理结果。
     *
     * @return 返回 Input Source 相关操作生成的结果数据。
     */
    public String getLabel() {
        return label;
    }

    /**
     * 查询 Input Source 相关业务数据，并返回处理结果。
     *
     * @return 返回 Input Source 相关操作生成的结果数据。
     */
    public String getDescription() {
        return description;
    }
}
