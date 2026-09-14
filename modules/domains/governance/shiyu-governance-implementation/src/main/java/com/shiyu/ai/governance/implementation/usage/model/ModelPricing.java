package com.shiyu.ai.governance.implementation.usage.model;

/** 模型定价配置 */
public class ModelPricing {

    /**
     * platform 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String platform;
    /**
     * model 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String model;
    /**
     * inputPricePer1K 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final double inputPricePer1K; // 输入价格 (每1k tokens)
    /**
     * outputPricePer1K 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final double outputPricePer1K; // 输出价格 (每1k tokens)

    /**
     * {@code ModelPricing} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param inputPricePer1K 参数值，用于执行当前操作。
     * @param outputPricePer1K 参数值，用于执行当前操作。
     */
    public ModelPricing(
            String platform, String model, double inputPricePer1K, double outputPricePer1K) {
        this.platform = platform;
        this.model = model;
        this.inputPricePer1K = inputPricePer1K;
        this.outputPricePer1K = outputPricePer1K;
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
     * {@code getInputPricePer1K} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public double getInputPricePer1K() {
        return inputPricePer1K;
    }

    /**
     * {@code getOutputPricePer1K} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public double getOutputPricePer1K() {
        return outputPricePer1K;
    }

    /** 计算费用 */
    public double calculateCost(int promptTokens, int completionTokens) {
        return (promptTokens / 1000.0 * inputPricePer1K)
                + (completionTokens / 1000.0 * outputPricePer1K);
    }

    /** 默认定价配置 */
    public static ModelPricing defaultOpenAI() {
        return new ModelPricing("OPENAI", "gpt-4o", 0.005, 0.015);
    }
}
