package com.shiyu.ai.model.implementation.infrastructure.media;

import java.util.List;

/**
 * MediaProvider 边界接口，负责向外部组件提供模型领域相关能力。
 */
public interface MediaProvider {
    /**
     * 处理标识。
     *
     * @return 处理结果。
     */
    default String id() {
        return getClass().getSimpleName();
    }

    /**
     * 执行 {@code textToSpeech} 定义的接口操作。
     *
     * @param text 方法参数。
     * @param voice 方法参数。
     * @param format 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    byte[] textToSpeech(String text, String voice, String format);

    /**
     * 执行 {@code translate} 定义的接口操作。
     *
     * @param text 方法参数。
     * @param sourceLanguage 方法参数。
     * @param targetLanguage 方法参数。
     *
     * @return 操作结果。
     */
    String translate(String text, String sourceLanguage, String targetLanguage);

    /**
     * 执行 {@code understandImage} 定义的接口操作。
     *
     * @param image 方法参数。
     * @param mimeType 方法参数。
     * @param instruction 方法参数。
     *
     * @return 操作结果。
     */
    VisionResult understandImage(byte[] image, String mimeType, String instruction);

    /**
     * 执行 {@code generateImage} 定义的接口操作。
     *
     * @param prompt 方法参数。
     * @param format 方法参数。
     *
     * @return 操作结果。
     */
    ImageResult generateImage(String prompt, String format);

    /**
     * {@code VisionResult} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param text text 属性，表示该记录组件承载的数据。
     * @param labels labels 属性，表示该记录组件承载的数据。
     */
    record VisionResult(String text, List<String> labels) {}

    /**
     * {@code ImageResult} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param mimeType mimeType 属性，表示该记录组件承载的数据。
     * @param width width 属性，表示该记录组件承载的数据。
     * @param height height 属性，表示该记录组件承载的数据。
     */
    record ImageResult(String objectKey, String mimeType, int width, int height) {}
}
