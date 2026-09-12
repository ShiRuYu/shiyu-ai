package com.shiyu.ai.common.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * {@code LoggerUtil} 表示平台基础设施模块中的一组受控业务状态或分类。
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
     * {@code info} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     */
    public void info(String message) {
        normalLogger.info(message);
    }

    /**
     * {@code info} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     * @param objects 参数值，用于执行当前操作。
     */
    public void info(String message, Object... objects) {
        normalLogger.info(msgHandle(message, objects));
    }

    /**
     * {@code warn} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     */
    public void warn(String message) {
        normalLogger.warn(message);
    }

    /**
     * {@code warn} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     * @param objects 参数值，用于执行当前操作。
     */
    public void warn(String message, Object... objects) {
        normalLogger.warn(msgHandle(message, objects));
    }

    /**
     * {@code debug} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     * @param objects 参数值，用于执行当前操作。
     */
    public void debug(String message, Object... objects) {
        normalLogger.debug(msgHandle(message, objects));
    }

    /**
     * {@code error} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     */
    public void error(String message) {
        normalLogger.error(message);
    }

    /**
     * {@code error} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     * @param objects 参数值，用于执行当前操作。
     */
    public void error(String message, Object... objects) {
        Throwable throwable = getThrowable(objects);
        normalLogger.error(msgHandle(message, objects), throwable);
    }

    /**
     * {@code error} 执行当前类型定义的业务操作。
     *
     * @param throwable 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     * @param objects 参数值，用于执行当前操作。
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
