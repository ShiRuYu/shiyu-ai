package com.shiyu.ai.model.implementation.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.model.implementation.infrastructure.media.MediaProvider;
import com.shiyu.ai.model.implementation.infrastructure.media.MediaProviderRegistry;

import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

/**
 * {@code MediaController} 是模型模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@RestController("modelMediaController")
@RequestMapping("/api/model/media")
public class MediaController {
    /**
     * registry 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MediaProviderRegistry registry;

    /**
     * {@code MediaController} 创建并初始化当前类型实例。
     *
     * @param registry 参数值，用于执行当前操作。
     */
    public MediaController(MediaProviderRegistry registry) {
        this.registry = registry;
    }

    /**
     * {@code tts} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/tts")
    public Result<Map<String, String>> tts(@RequestBody TtsRequest request) {
        return Result.success(
                Map.of(
                        "format",
                        request.format == null ? "wav" : request.format,
                        "audioBase64",
                        Base64.getEncoder()
                                .encodeToString(
                                        registry.require(request.provider)
                                                .textToSpeech(
                                                        request.text,
                                                        request.voice,
                                                        request.format))));
    }

    /**
     * {@code translate} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/translate")
    public Result<String> translate(@RequestBody TranslateRequest request) {
        return Result.success(
                registry.require(request.provider)
                        .translate(request.text, request.sourceLanguage, request.targetLanguage));
    }

    /**
     * {@code understand} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/image/understand")
    public Result<MediaProvider.VisionResult> understand(@RequestBody ImageRequest request) {
        return Result.success(
                registry.require(request.provider)
                        .understandImage(
                                Base64.getDecoder().decode(request.imageBase64),
                                request.mimeType,
                                request.instruction));
    }

    /**
     * {@code generate} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/image/generate")
    public Result<MediaProvider.ImageResult> generate(@RequestBody GenerateRequest request) {
        return Result.success(
                registry.require(request.provider).generateImage(request.prompt, request.format));
    }

    /**
     * {@code TtsRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
     */
    public static class TtsRequest {
        /**
         * 提供者，表示当前对象中的对应属性。
         */
        public String provider;
        /**
         * text 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String text;
        /**
         * voice 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String voice;
        /**
         * format 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String format = "wav";
    }

    /**
     * {@code TranslateRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
     */
    public static class TranslateRequest {
        public String provider;
        /**
         * text 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String text;
        /**
         * sourceLanguage 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String sourceLanguage;
        /**
         * targetLanguage 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String targetLanguage;
    }

    /**
     * {@code ImageRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
     */
    public static class ImageRequest {
        public String provider;
        /**
         * imageBase64 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String imageBase64;
        /**
         * mimeType 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String mimeType;
        /**
         * instruction 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String instruction;
    }

    /**
     * {@code GenerateRequest} 表示模型模块的请求参数，承载调用方提交的输入数据。
     */
    public static class GenerateRequest {
        public String provider;
        /**
         * 提示词，表示当前对象中的对应属性。
         */
        public String prompt;
        /**
         * format 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String format = "png";
    }
}
