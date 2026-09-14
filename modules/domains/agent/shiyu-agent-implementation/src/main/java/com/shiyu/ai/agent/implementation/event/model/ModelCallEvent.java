package com.shiyu.ai.agent.implementation.event.model;

/**
 * {@code ModelCallEvent} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public class ModelCallEvent extends DomainEvent {

    /**
     * platform 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String platform;
    /**
     * model 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String model;
    /**
     * promptTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int promptTokens;
    /**
     * completionTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int completionTokens;
    /**
     * latencyMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long latencyMs;

    /**
     * {@code ModelCallEvent} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     */
    public ModelCallEvent(
            String platform, String model, int promptTokens, int completionTokens, long latencyMs) {
        super("MODEL_CALL");
        this.platform = platform;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
    }

    /**
     * {@code getPlatform} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * {@code getModel} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getModel() {
        return model;
    }

    /**
     * {@code getPromptTokens} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * {@code getCompletionTokens} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * {@code getLatencyMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getLatencyMs() {
        return latencyMs;
    }
}
