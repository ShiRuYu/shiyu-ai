package com.shiyu.ai.model.implementation.infrastructure.media;

import com.shiyu.ai.model.implementation.infrastructure.media.port.MediaProvider;

import com.shiyu.ai.model.implementation.infrastructure.media.service.MediaProviderRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 验证 Media Provider Registry 相关功能、边界条件、异常路径和协作行为。
 */
class MediaProviderRegistryTest {

    @Test
    void selectsDefaultAndNamedProvidersAndFailsForUnknownIds() {
        MediaProvider first = new StubProvider("image");
        MediaProvider second = new StubProvider("video");
        MediaProviderRegistry registry = new MediaProviderRegistry(List.of(first, second));
        assertEquals(first, registry.require());
        assertEquals(first, registry.require(null));
        assertEquals(second, registry.require("video"));
        assertThrows(IllegalArgumentException.class, () -> registry.require("audio"));
        assertThrows(
                IllegalStateException.class, () -> new MediaProviderRegistry(List.of()).require());
    }

    /**
     * 验证 Stub 相关功能、边界条件、异常路径和协作行为。
     */
    private record StubProvider(String id) implements MediaProvider {
        @Override
        public byte[] textToSpeech(String text, String voice, String format) {
            return new byte[0];
        }

        @Override
        public String translate(String text, String sourceLanguage, String targetLanguage) {
            return text;
        }

        @Override
        public VisionResult understandImage(byte[] image, String mimeType, String instruction) {
            return new VisionResult("", List.of());
        }

        @Override
        public ImageResult generateImage(String prompt, String format) {
            return new ImageResult("", format, 0, 0);
        }
    }
}
