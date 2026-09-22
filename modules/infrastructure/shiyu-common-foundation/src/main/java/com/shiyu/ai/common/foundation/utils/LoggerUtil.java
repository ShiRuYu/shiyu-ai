package com.shiyu.ai.common.foundation.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * 定义 Logger 可用的枚举值及其业务语义。
 */
@Getter
@AllArgsConstructor
public enum LoggerUtil {
    COMMON_LOGGER(LoggerFactory.getLogger("COMMON_LOGGER")),
    WEB_LOGGER(LoggerFactory.getLogger("WEB_LOGGER")),
    SYSTEM_LOGGER(LoggerFactory.getLogger("SYSTEM_LOGGER")),
    BUSINESS_LOGGER(LoggerFactory.getLogger("BUSINESS_LOGGER")),
    AUDIT_LOGGER(LoggerFactory.getLogger("AUDIT_LOGGER")),
    SECURITY_LOGGER(LoggerFactory.getLogger("SECURITY_LOGGER")),
    AI_LOGGER(LoggerFactory.getLogger("AI_LOGGER")),
    TOOL_LOGGER(LoggerFactory.getLogger("TOOL_LOGGER")),
    KNOWLEDGE_LOGGER(LoggerFactory.getLogger("KNOWLEDGE_LOGGER")),
    TASK_LOGGER(LoggerFactory.getLogger("TASK_LOGGER")),
    INTEGRATION_LOGGER(LoggerFactory.getLogger("INTEGRATION_LOGGER")),
    PERFORMANCE_LOGGER(LoggerFactory.getLogger("PERFORMANCE_LOGGER")),
    DATABASE_LOGGER(LoggerFactory.getLogger("DATABASE_LOGGER")),
    ERROR_LOGGER(LoggerFactory.getLogger("ERROR_LOGGER")),
    ;

    /**
     * normalLogger 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Logger normalLogger;

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    public void info(String message) {
        normalLogger.info(message);
    }

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @param objects 用于完成本次业务处理的 objects 参数。
     */
    public void info(String message, Object... objects) {
        normalLogger.info(msgHandle(message, objects));
    }

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    public void warn(String message) {
        normalLogger.warn(message);
    }

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @param objects 用于完成本次业务处理的 objects 参数。
     */
    public void warn(String message, Object... objects) {
        normalLogger.warn(msgHandle(message, objects));
    }

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @param objects 用于完成本次业务处理的 objects 参数。
     */
    public void debug(String message, Object... objects) {
        normalLogger.debug(msgHandle(message, objects));
    }

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    public void error(String message) {
        normalLogger.error(message);
    }

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @param objects 用于完成本次业务处理的 objects 参数。
     */
    public void error(String message, Object... objects) {
        Throwable throwable = getThrowable(objects);
        normalLogger.error(msgHandle(message, objects), throwable);
    }

    /**
     * 执行 Logger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param throwable 用于完成本次业务处理的 throwable 参数。
     * @param message 本次流程携带的事件或业务数据。
     * @param objects 用于完成本次业务处理的 objects 参数。
     */
    public void error(Throwable throwable, String message, Object... objects) {
        normalLogger.error(msgHandle(message, objects), throwable);
    }

    private Throwable getThrowable(Object[] objects) {
        Throwable throwable = null;
        for (Object object : objects) {
            if (object instanceof Throwable ofThrowable) {
                throwable = ofThrowable;
            }
        }
        return throwable;
    }

    private String msgHandle(String message, Object[] objects) {
        return MessageFormatter.arrayFormat(message, objects).getMessage();
    }
}
