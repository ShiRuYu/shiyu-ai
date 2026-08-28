package com.shiyu.ai.model.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.model.media.MediaProvider;
import com.shiyu.ai.model.media.MediaProviderRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@RestController("modelMediaController")
@RequestMapping("/api/record/media")
public class MediaController {
    private final MediaProviderRegistry registry;
    public MediaController(MediaProviderRegistry registry) { this.registry = registry; }
    @PostMapping("/tts") public Result<Map<String, String>> tts(@RequestBody TtsRequest request) { return Result.success(Map.of("format", request.format == null ? "wav" : request.format, "audioBase64", Base64.getEncoder().encodeToString(registry.require(request.provider).textToSpeech(request.text, request.voice, request.format)))); }
    @PostMapping("/translate") public Result<String> translate(@RequestBody TranslateRequest request) { return Result.success(registry.require(request.provider).translate(request.text, request.sourceLanguage, request.targetLanguage)); }
    @PostMapping("/image/understand") public Result<MediaProvider.VisionResult> understand(@RequestBody ImageRequest request) { return Result.success(registry.require(request.provider).understandImage(Base64.getDecoder().decode(request.imageBase64), request.mimeType, request.instruction)); }
    @PostMapping("/image/generate") public Result<MediaProvider.ImageResult> generate(@RequestBody GenerateRequest request) { return Result.success(registry.require(request.provider).generateImage(request.prompt, request.format)); }
    public static class TtsRequest { public String provider; public String text; public String voice; public String format = "wav"; }
    public static class TranslateRequest { public String provider; public String text; public String sourceLanguage; public String targetLanguage; }
    public static class ImageRequest { public String provider; public String imageBase64; public String mimeType; public String instruction; }
    public static class GenerateRequest { public String provider; public String prompt; public String format = "png"; }
}
