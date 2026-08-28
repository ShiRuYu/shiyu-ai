package com.shiyu.ai.governance.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsageWebSocketHandlerTest {

    @Test
    void tracksConnectionsAndBroadcastsOnlyToOpenSessions() throws Exception {
        UsageWebSocketHandler handler = new UsageWebSocketHandler();
        WebSocketSession open = mock(WebSocketSession.class);
        WebSocketSession closed = mock(WebSocketSession.class);
        when(open.getId()).thenReturn("open");
        when(closed.getId()).thenReturn("closed");
        when(open.isOpen()).thenReturn(true);
        when(closed.isOpen()).thenReturn(false);

        handler.afterConnectionEstablished(open);
        handler.afterConnectionEstablished(closed);
        assertEquals(2, handler.getActiveSessionCount());
        handler.handleTextMessage(open, new TextMessage("ping"));
        handler.broadcast("{\"ok\":true}");
        verify(open).sendMessage(any(TextMessage.class));

        handler.afterConnectionClosed(closed, CloseStatus.NORMAL);
        assertEquals(1, handler.getActiveSessionCount());
        handler.afterConnectionClosed(open, CloseStatus.NORMAL);
        assertEquals(0, handler.getActiveSessionCount());
    }

    @Test
    void removesSessionsThatFailTransportOrSend() throws Exception {
        UsageWebSocketHandler handler = new UsageWebSocketHandler();
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("broken");
        when(session.isOpen()).thenReturn(true);
        handler.afterConnectionEstablished(session);
        doThrow(new IOException("socket closed")).when(session).sendMessage(any(TextMessage.class));

        handler.broadcast("message");
        assertEquals(1, handler.getActiveSessionCount(), "send failures are logged but retain the session until transport closes");
        handler.handleTransportError(session, new IOException("transport"));
        assertEquals(0, handler.getActiveSessionCount());
        handler.broadcast("ignored");
    }
}
