package com.shiyu.ai.agent.contract.node;

/**
 * {@code InputSource} 表示智能体模块中的一组受控业务状态或分类。
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
     * {@code getCode} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getCode() {
        return code;
    }

    /**
     * {@code getLabel} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getLabel() {
        return label;
    }

    /**
     * {@code getDescription} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getDescription() {
        return description;
    }
}
