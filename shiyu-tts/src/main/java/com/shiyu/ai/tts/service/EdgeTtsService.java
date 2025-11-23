package com.shiyu.ai.tts.service;

import com.shiyu.ai.tts.websocket.TtsWebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class EdgeTtsService {

    @Value("${tts.websocket-url}")
    private String websocketUrl;

    public byte[] synthesize(String text, String voice, float rate) throws Exception {
        TtsWebSocketClient client = new TtsWebSocketClient();
        URI endpoint = new URI(websocketUrl);
        return client.synthesize(endpoint, text, voice, rate);
    }
}

