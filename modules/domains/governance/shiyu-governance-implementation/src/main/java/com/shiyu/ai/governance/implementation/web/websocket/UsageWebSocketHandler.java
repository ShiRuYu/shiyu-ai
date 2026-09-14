package com.shiyu.ai.governance.implementation.web.websocket;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 用量 WebSocket 处理器
 *
 * <p>维护与前端页面的 WebSocket 连接，当用量数据更新时推送实时数据。
 */
@Slf4j
public class UsageWebSocketHandler extends TextWebSocketHandler {

    /** 活跃会话集合 */
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    /**
     * {@code afterConnectionEstablished} 执行当前类型定义的业务操作。
     *
     * @param session 参数值，用于执行当前操作。
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info(
                "WebSocket 连接已建立: sessionPresent={}, 当前连接数={}",
                session.getId() != null,
                sessions.size());
    }

    /**
     * {@code afterConnectionClosed} 执行当前类型定义的业务操作。
     *
     * @param session 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info(
                "WebSocket 连接已关闭: sessionPresent={}, statusPresent={}, 当前连接数={}",
                session.getId() != null,
                status != null,
                sessions.size());
    }

    /**
     * {@code handleTextMessage} 执行当前模块定义的业务流程。
     *
     * @param session 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 目前只做推送，不做接收消息处理
        log.debug(
                "WebSocket 收到消息: sessionPresent={}, payloadLength={}",
                session.getId() != null,
                message.getPayload().length());
    }

    /**
     * {@code handleTransportError} 执行当前模块定义的业务流程。
     *
     * @param session 参数值，用于执行当前操作。
     * @param exception 参数值，用于执行当前操作。
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn(
                "WebSocket 传输错误: sessionPresent={}, errorType={}, errorMessageLength={}",
                session.getId() != null,
                exception.getClass().getSimpleName(),
                exception.getMessage() == null ? 0 : exception.getMessage().length());
        sessions.remove(session);
    }

    /**
     * 向所有连接的客户端广播消息
     *
     * @param message JSON 格式的消息
     */
    public void broadcast(String message) {
        if (sessions.isEmpty()) {
            return;
        }
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.warn(
                            "WebSocket 发送消息失败: sessionPresent={}, errorType={},"
                                    + " errorMessageLength={}",
                            session.getId() != null,
                            e.getClass().getSimpleName(),
                            e.getMessage() == null ? 0 : e.getMessage().length());
                }
            }
        }
    }

    /** 获取当前连接数 */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
