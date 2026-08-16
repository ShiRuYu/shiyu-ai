package com.shiyu.ai.model.media;

import java.util.List;

/** Provider-neutral multimodal SPI; files remain behind the platform storage port. */
public interface MediaProvider {
    /** Stable provider selector kept out of Conversation and product models. */
    default String id() { return getClass().getSimpleName(); }
    byte[] textToSpeech(String text, String voice, String format);
    String translate(String text, String sourceLanguage, String targetLanguage);
    VisionResult understandImage(byte[] image, String mimeType, String instruction);
    ImageResult generateImage(String prompt, String format);

    record VisionResult(String text, List<String> labels) {}
    record ImageResult(String objectKey, String mimeType, int width, int height) {}
}
