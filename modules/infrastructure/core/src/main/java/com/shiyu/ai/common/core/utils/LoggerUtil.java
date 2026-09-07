package com.shiyu.ai.common.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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

    private final Logger normalLogger;

    public void info(String message){
        normalLogger.info(message);
    }
    public void info(String message,Object... objects){
        normalLogger.info(msgHandle(message,objects));
    }
    public void warn(String message){
        normalLogger.warn(message);
    }
    public void warn(String message,Object... objects){
        normalLogger.warn(msgHandle(message,objects));
    }
    public void debug(String message,Object... objects){
        normalLogger.debug(msgHandle(message,objects));
    }
    public void error(String message){
        normalLogger.error(message);
    }
    public void error(String message,Object... objects){
        Throwable throwable = getThrowable(objects);
        normalLogger.error(msgHandle(message,objects),throwable);
    }
    public void error(Throwable throwable,String message,Object... objects){
        normalLogger.error(msgHandle(message,objects),throwable);
    }

    private Throwable getThrowable(Object[] objects) {
        Throwable throwable = null;
        for (Object object : objects) {
            if (object instanceof Throwable ofThrowable){
                throwable = ofThrowable;
            }
        }
        return throwable;
    }


    private String msgHandle(String message, Object[] objects) {
        return MessageFormatter.arrayFormat(message, objects).getMessage();
    }

}

