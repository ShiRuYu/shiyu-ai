package com.shiyu.ai.tts.websocket;

import com.shiyu.ai.tts.utils.SsmlBuilder;
import jakarta.websocket.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class TtsWebSocketClient {
    private Session session;
    private byte[] audioData = new byte[0];
    private final CountDownLatch latch = new CountDownLatch(1);

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(ByteBuffer buffer) {
        byte[] newData = new byte[buffer.remaining()];
        buffer.get(newData);
        // 拼接
        byte[] combined = new byte[audioData.length + newData.length];
        System.arraycopy(audioData, 0, combined, 0, audioData.length);
        System.arraycopy(newData, 0, combined, audioData.length, newData.length);
        audioData = combined;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        latch.countDown();
    }

    public byte[] synthesize(URI endpoint, String text, String voice, float rate) throws Exception {
        String ssml = SsmlBuilder.buildRequest(text, voice, rate);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, endpoint);

        // 发送 SSML
        session.getBasicRemote().sendText(ssml);

        // 等待关闭（超时 30s）
        latch.await(30, TimeUnit.SECONDS);

        return audioData;
    }
}

