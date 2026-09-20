package com.shiyu.ai.model.implementation.infrastructure.media.port;

import java.util.List;

/**
 * 创建或提供 Media 相关的业务组件和运行时能力。
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
     * 执行 Media 相关业务数据，并返回处理结果。
     *
     * @param text 用于完成本次业务处理的 text 参数。
     * @param voice 用于完成本次业务处理的 voice 参数。
     * @param format 用于完成本次业务处理的 format 参数。
     * @return 返回 Media 相关操作生成的结果数据。
     */
    byte[] textToSpeech(String text, String voice, String format);

    /**
     * 执行 Media 相关业务数据，并返回处理结果。
     *
     * @param text 用于完成本次业务处理的 text 参数。
     * @param sourceLanguage 用于完成本次业务处理的 sourceLanguage 参数。
     * @param targetLanguage 用于完成本次业务处理的 targetLanguage 参数。
     * @return 返回 Media 相关操作生成的结果数据。
     */
    String translate(String text, String sourceLanguage, String targetLanguage);

    /**
     * 执行 Media 相关业务数据，并返回处理结果。
     *
     * @param image 用于完成本次业务处理的 image 参数。
     * @param mimeType 用于完成本次业务处理的 mimeType 参数。
     * @param instruction 用于完成本次业务处理的 instruction 参数。
     * @return 返回 Media 相关操作生成的结果数据。
     */
    VisionResult understandImage(byte[] image, String mimeType, String instruction);

    /**
     * 执行 Media 相关业务数据，并返回处理结果。
     *
     * @param prompt 用于完成本次业务处理的 prompt 参数。
     * @param format 用于完成本次业务处理的 format 参数。
     * @return 返回 Media 相关操作生成的结果数据。
     */
    ImageResult generateImage(String prompt, String format);

    /**
     * 封装 Vision 相关的不可变数据及其字段约束。
     */
    record VisionResult(String text, List<String> labels) {}

    /**
     * 封装 Image 相关的不可变数据及其字段约束。
     */
    record ImageResult(String objectKey, String mimeType, int width, int height) {}
}
