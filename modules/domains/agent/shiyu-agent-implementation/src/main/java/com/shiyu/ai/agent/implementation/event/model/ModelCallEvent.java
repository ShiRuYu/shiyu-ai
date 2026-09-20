package com.shiyu.ai.agent.implementation.event.model;

/**
 * 表示 模型 Call 相关的领域事件或异常信息。
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
     * 执行 模型 Call 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @param promptTokens 用于完成本次业务处理的 promptTokens 参数。
     * @param completionTokens 用于完成本次业务处理的 completionTokens 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
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
     * 查询 模型 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Call 相关操作生成的结果数据。
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 查询 模型 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Call 相关操作生成的结果数据。
     */
    public String getModel() {
        return model;
    }

    /**
     * 查询 模型 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Call 相关操作生成的结果数据。
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * 查询 模型 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Call 相关操作生成的结果数据。
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * 查询 模型 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Call 相关操作生成的结果数据。
     */
    public long getLatencyMs() {
        return latencyMs;
    }
}
