package com.shiyu.ai.model.implementation.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.model.implementation.infrastructure.media.port.MediaProvider;
import com.shiyu.ai.model.implementation.infrastructure.media.service.MediaProviderRegistry;

import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

/**
 * 处理 Media 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController("modelMediaController")
@RequestMapping("/api/model/media")
public class MediaController {
    /**
     * registry 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MediaProviderRegistry registry;

    /**
     * 执行 Media 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param registry 用于完成本次业务处理的 registry 参数。
     */
    public MediaController(MediaProviderRegistry registry) {
        this.registry = registry;
    }

    /**
     * 执行 Media 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tts 用于完成本次业务处理的 tts 参数。
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
     * 执行 Media 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param translate 用于完成本次业务处理的 translate 参数。
     */
    @PostMapping("/translate")
    public Result<String> translate(@RequestBody TranslateRequest request) {
        return Result.success(
                registry.require(request.provider)
                        .translate(request.text, request.sourceLanguage, request.targetLanguage));
    }

    /**
     * 执行 Media 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param understand 用于完成本次业务处理的 understand 参数。
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
     * 执行 Media 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param generate 用于完成本次业务处理的 generate 参数。
     */
    @PostMapping("/image/generate")
    public Result<MediaProvider.ImageResult> generate(@RequestBody GenerateRequest request) {
        return Result.success(
                registry.require(request.provider).generateImage(request.prompt, request.format));
    }

    /**
     * 封装 Tts 操作所需的请求条件和输入数据。
     */
    @lombok.Getter
    @lombok.Setter
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
     * 封装 Translate 操作所需的请求条件和输入数据。
     */
    @lombok.Getter
    @lombok.Setter
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
     * 封装 Image 操作所需的请求条件和输入数据。
     */
    @lombok.Getter
    @lombok.Setter
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
     * 封装 Generate 操作所需的请求条件和输入数据。
     */
    @lombok.Getter
    @lombok.Setter
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
