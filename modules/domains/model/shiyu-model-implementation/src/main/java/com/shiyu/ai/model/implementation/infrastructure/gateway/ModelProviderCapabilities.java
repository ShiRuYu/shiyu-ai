package com.shiyu.ai.model.implementation.infrastructure.gateway;

import java.util.List;
import java.util.Set;

/**
 * {@code ModelProviderCapabilities} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param provider 提供方，表示该记录组件承载的数据。
 * @param model 模型，表示该记录组件承载的数据。
 * @param features features 属性，表示该记录组件承载的数据。
 * @param contextWindow contextWindow 属性，表示该记录组件承载的数据。
 * @param streaming streaming 属性，表示该记录组件承载的数据。
 * @param tools tools 属性，表示该记录组件承载的数据。
 * @param parallelTools parallelTools 属性，表示该记录组件承载的数据。
 * @param multimodal multimodal 属性，表示该记录组件承载的数据。
 * @param jsonSchema jsonSchema 属性，表示该记录组件承载的数据。
 * @param reasoningLevels reasoningLevels 属性，表示该记录组件承载的数据。
 * @param maxOutputTokens maxOutputTokens 属性，表示该记录组件承载的数据。
 * @param streamUsage streamUsage 属性，表示该记录组件承载的数据。
 * @param cacheUsage cacheUsage 属性，表示该记录组件承载的数据。
 * @param cancellation cancellation 属性，表示该记录组件承载的数据。
 */
public record ModelProviderCapabilities(
        String provider,
        String model,
        Set<String> features,
        int contextWindow,
        boolean streaming,
        boolean tools,
        boolean parallelTools,
        boolean multimodal,
        boolean jsonSchema,
        List<String> reasoningLevels,
        int maxOutputTokens,
        boolean streamUsage,
        boolean cacheUsage,
        boolean cancellation) {
    public ModelProviderCapabilities(
            String provider, String model, Set<String> features, int contextWindow) {
        this(
                provider,
                model,
                features,
                contextWindow,
                true,
                features != null && features.contains("tool_calls"),
                features != null && features.contains("parallel_tool_calls"),
                false,
                features != null && features.contains("structured"),
                List.of(),
                4096,
                true,
                false,
                true);
    }

    public ModelProviderCapabilities {
        if (provider == null || provider.isBlank() || model == null || model.isBlank())
            throw new IllegalArgumentException("provider and model are required");
        features = features == null ? Set.of("chat") : Set.copyOf(features);
        contextWindow = contextWindow <= 0 ? 8192 : contextWindow;
        reasoningLevels = reasoningLevels == null ? List.of() : List.copyOf(reasoningLevels);
        maxOutputTokens = maxOutputTokens <= 0 ? 4096 : maxOutputTokens;
    }

    public boolean supports(String feature) {
        return feature != null && features.contains(feature);
    }
}
