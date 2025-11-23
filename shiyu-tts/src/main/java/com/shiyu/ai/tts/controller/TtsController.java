package com.shiyu.ai.tts.controller;

import com.shiyu.ai.tts.service.EdgeTtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tts")
public class TtsController {

    private final EdgeTtsService ttsService;

    @GetMapping
    public ResponseEntity<byte[]> tts(
            @RequestParam String text,
            @RequestParam(defaultValue = "zh-CN-XiaoxiaoNeural") String voice,
            @RequestParam(defaultValue = "1.0") float rate
    ) {
        try {
            byte[] audio = ttsService.synthesize(text, voice, rate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tts.mp3")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(audio);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("TTS error: " + e.getMessage()).getBytes());
        }
    }
}

