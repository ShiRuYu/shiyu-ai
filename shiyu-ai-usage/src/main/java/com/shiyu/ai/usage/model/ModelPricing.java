package com.shiyu.ai.usage.model;

/**
 * 模型定价配置
 */
public class ModelPricing {

    private final String platform;
    private final String model;
    private final double inputPricePer1K;   // 输入价格 (每1k tokens)
    private final double outputPricePer1K;  // 输出价格 (每1k tokens)

    public ModelPricing(String platform, String model,
                        double inputPricePer1K, double outputPricePer1K) {
        this.platform = platform;
        this.model = model;
        this.inputPricePer1K = inputPricePer1K;
        this.outputPricePer1K = outputPricePer1K;
    }

    public String getPlatform() { return platform; }
    public String getModel() { return model; }
    public double getInputPricePer1K() { return inputPricePer1K; }
    public double getOutputPricePer1K() { return outputPricePer1K; }

    /**
     * 计算费用
     */
    public double calculateCost(int promptTokens, int completionTokens) {
        return (promptTokens / 1000.0 * inputPricePer1K) +
               (completionTokens / 1000.0 * outputPricePer1K);
    }

    /** 默认定价配置 */
    public static ModelPricing defaultOpenAI() {
        return new ModelPricing("OPENAI", "gpt-4o", 0.005, 0.015);
    }
}
