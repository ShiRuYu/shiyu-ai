package com.shiyu.ai.governance.implementation.usage.model;

/**
 * 实现 模型 Pricing 相关的业务处理、协作逻辑或基础设施能力。
 */
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
     * 执行 模型 Pricing 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @param inputPricePer1K 用于完成本次业务处理的 inputPricePer1K 参数。
     * @param outputPricePer1K 用于完成本次业务处理的 outputPricePer1K 参数。
     */
    public ModelPricing(
            String platform, String model, double inputPricePer1K, double outputPricePer1K) {
        this.platform = platform;
        this.model = model;
        this.inputPricePer1K = inputPricePer1K;
        this.outputPricePer1K = outputPricePer1K;
    }

    /**
     * 查询 模型 Pricing 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Pricing 相关操作生成的结果数据。
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 查询 模型 Pricing 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Pricing 相关操作生成的结果数据。
     */
    public String getModel() {
        return model;
    }

    /**
     * 查询 模型 Pricing 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Pricing 相关操作生成的结果数据。
     */
    public double getInputPricePer1K() {
        return inputPricePer1K;
    }

    /**
     * 查询 模型 Pricing 相关业务数据，并返回处理结果。
     *
     * @return 返回 模型 Pricing 相关操作生成的结果数据。
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
