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
 * 处理 用量 Web Socket 相关事件或请求，并推进后续业务流程。
 */
@Slf4j
public class UsageWebSocketHandler extends TextWebSocketHandler {

    /** 活跃会话集合 */
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    /**
     * 执行 用量 Web Socket 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param session 用于完成本次业务处理的 session 参数。
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
     * 执行 用量 Web Socket 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param session 用于完成本次业务处理的 session 参数。
     * @param status 用于完成本次业务处理的 status 参数。
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
     * 处理 用量 Web Socket 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param session 用于完成本次业务处理的 session 参数。
     * @param message 本次流程携带的事件或业务数据。
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
     * 处理 用量 Web Socket 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param session 用于完成本次业务处理的 session 参数。
     * @param exception 用于完成本次业务处理的 exception 参数。
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
